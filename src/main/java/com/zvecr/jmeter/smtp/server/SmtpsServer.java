package com.zvecr.jmeter.smtp.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.server.SMTPServer;

/**
 * Local extension of {@link org.subethamail.smtp.server.SMTPServer} to add
 * missing ssl keystore support.
 */
public class SmtpsServer extends SMTPServer {

	private String keystorePassword;
	private File keystore;

	public SmtpsServer(MessageHandlerFactory handlerFactory) {
		super(handlerFactory);
	}

	private InputStream getKeystoreStream() throws IOException {
		if (keystore != null) {
			return Files.newInputStream(keystore.toPath());
		}

		// using a "built in" default keystore is a bit hacky but reduces the time to
		// setup basic tests
		keystorePassword = "password";
		return this.getClass().getResourceAsStream("/keystore.jks");
	}

	private SSLContext getSSLContext() throws GeneralSecurityException, IOException {
		// TODO: honour system properties:
		// "javax.net.ssl.keyStore"
		// "javax.net.ssl.keyStorePassword"

		KeyStore keyStore = KeyStore.getInstance("JKS");
		try (InputStream file = getKeystoreStream()) {
			keyStore.load(file, keystorePassword.toCharArray());

			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore, keystorePassword.toCharArray());

			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(keyStore);

			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(keyManagerFactory.getKeyManagers(), tmf.getTrustManagers(), null);

			return ctx;
		}
	}

	@Override
	public SSLSocket createSSLSocket(Socket socket) throws IOException {
		try {
			SSLSocketFactory factory = getSSLContext().getSocketFactory();

			InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
			SSLSocket s = (SSLSocket) (factory.createSocket(socket, remoteAddress.getHostName(), socket.getPort(),
					true));

			// we are a server
			s.setUseClientMode(false);

			// allow all supported cipher suites
			s.setEnabledCipherSuites(s.getSupportedCipherSuites());

			return s;
		} catch (GeneralSecurityException e) {
			throw new IOException(e);
		}
	}

	@Override
	protected ServerSocket createServerSocket() throws IOException {
		if (!this.getEnableTLS() || this.getRequireTLS()) {
			return super.createServerSocket();
		}

		try {
			SSLServerSocketFactory factory = getSSLContext().getServerSocketFactory();

			SSLServerSocket sslserversocket = (SSLServerSocket) factory.createServerSocket(this.getPort(),
					this.getBacklog(), this.getBindAddress());

			// allow all supported cipher suites
			sslserversocket.setEnabledCipherSuites(factory.getSupportedCipherSuites());

			return sslserversocket;
		} catch (GeneralSecurityException e) {
			throw new IOException(e);
		}
	}

	public String getKeystorePassword() {
		return keystorePassword;
	}

	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

	public File getKeystore() {
		return keystore;
	}

	public void setKeystore(File keystore) {
		this.keystore = keystore;
	}
}
