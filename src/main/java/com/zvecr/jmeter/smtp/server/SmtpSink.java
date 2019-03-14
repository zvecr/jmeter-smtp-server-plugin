package com.zvecr.jmeter.smtp.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageBuilder;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.auth.EasyAuthenticationHandlerFactory;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.server.SMTPServer;

/**
 * SMTP server backed with a receiving message queue
 */
public class SmtpSink {
	private static final Logger LOG = LoggerFactory.getLogger(SmtpSink.class);

	private final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
	private final SMTPServer server;

	private long readTimeout = TimeUnit.SECONDS.toMillis(5);

	/**
	 * Connection handler that receives raw SMTP, converts, and stores on the
	 * message queue
	 */
	private class SinkMessageHandler implements MessageHandler {
		private final MessageBuilder builder = new DefaultMessageBuilder();

		@Override
		public void from(String from) {
			// do we care about envolope sender?
		}

		@Override
		public void recipient(String recipient) {
			// do we care about envolope sender?
		}

		@Override
		public void data(InputStream data) throws IOException {
			try {
				Message message = builder.parseMessage(data);
				messages.add(message);

				LOG.trace("Received message:{}", message.getMessageId());
			} catch (MimeException e) {
				throw new RejectException(e.getMessage());
			}
		}

		@Override
		public void done() {
			// do nothing
		}
	}

	/**
	 * Default construction requires at least port number
	 * 
	 * @param host
	 * @param port
	 */
	public SmtpSink(String host, int port) {
		server = new SMTPServer(ctx -> new SinkMessageHandler()) {

			private SSLContext getSSLContext() throws GeneralSecurityException, IOException {
				// TODO: read user specified keystore
				KeyStore keyStore = KeyStore.getInstance("JKS");
				try (InputStream file = this.getClass().getResourceAsStream("/keystore.jks")) {
					keyStore.load(file, "password".toCharArray());
					KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
					keyManagerFactory.init(keyStore, "password".toCharArray());

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
					SSLSocket s = (SSLSocket) (factory.createSocket(socket, remoteAddress.getHostName(),
							socket.getPort(), true));

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
				if (!SmtpSink.this.server.getEnableTLS() || SmtpSink.this.server.getRequireTLS()) {
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

		};
		server.setHostName(host);
		server.setPort(port);

		// TODO: configure timeouts
		// TODO: set size of thread pool
		// TODO: tls
	}

	/**
	 * Configure server for SMTP auth
	 * 
	 * @param username
	 * @param password
	 */
	public void enableAuth(String username, String password) {
		Objects.requireNonNull(username);
		Objects.requireNonNull(password);
		if (server.isRunning())
			throw new IllegalStateException("Cannot modify already running server");

		server.setAuthenticationHandlerFactory(new EasyAuthenticationHandlerFactory((user, pass) -> {
			if (!username.equals(user) || !password.equals(pass))
				throw new LoginFailedException();
		}));
	}

	public void configureTimeouts(int connectionTimeout, int readTimeout) {
		if (server.isRunning())
			throw new IllegalStateException("Cannot modify already running server");

		if (connectionTimeout != 0)
			server.setConnectionTimeout(connectionTimeout);
		if (readTimeout != 0)
			this.readTimeout = readTimeout;
	}

	public void enableTLS(Boolean enableTLS, Boolean enableStartTLS) {
		if (server.isRunning())
			throw new IllegalStateException("Cannot modify already running server");

		server.setEnableTLS(enableTLS || enableStartTLS);
		server.setRequireTLS(enableStartTLS);
	}

	/**
	 * Start the server
	 */
	public void start() {
		server.start();
		LOG.info("starting server on port:{}", server.getPort());
	}

	/**
	 * Stop the server
	 */
	public void stop() {
		LOG.info("stopping server - pending messages:{}", messages.size());
		server.stop();
	}

	/**
	 * Remove a smtp message from the internal queue - blocks with timeout
	 * 
	 * @return {@link Message}, or {@code null} if the specified waiting time
	 *         elapses before an element is available
	 */
	public Message pop() {
		try {
			return messages.poll(readTimeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return null;
	}
}
