package com.zvecr.jmeter.smtp.server;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;



import org.apache.james.mime4j.dom.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.auth.EasyAuthenticationHandlerFactory;
import org.subethamail.smtp.auth.LoginFailedException;

/**
 * SMTP server backed with a receiving message queue
 */
public class SmtpSink {
	static final Logger LOG = LoggerFactory.getLogger(SmtpSink.class);

	final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
	private final SmtpsServer server;

	private long readTimeout = TimeUnit.SECONDS.toMillis(5);

	/**
	 * Default construction requires at least port number
	 * 
	 * @param host
	 * @param port
	 */
	public SmtpSink(String host, int port) {
		server = new SmtpsServer(ctx -> new SinkMessageHandler(messages));
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
