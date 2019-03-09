package com.zvecr.jmeter.smtp.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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

	/**
	 * Connection handler that receives raw SMTP, converts, and stores on the message queue
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
		server = new SMTPServer(ctx -> new SinkMessageHandler());
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
	 * @return {@link Message}, or {@code null} if the specified waiting time elapses before an element is available
	 */
	public Message pop() {
		try {
			return messages.poll(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return null;
	}
}
