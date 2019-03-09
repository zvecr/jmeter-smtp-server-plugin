package com.zvecr.jmeter.smtp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageWriter;
import org.apache.james.mime4j.message.DefaultMessageWriter;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zvecr.jmeter.smtp.server.SmtpSink;
import com.zvecr.jmeter.smtp.server.SmtpSinkPool;
import com.zvecr.jmeter.util.DefaultedTestStateListener;

/**
 * Jmeter plugin for receiving smtp messages.
 * <li>lazy start server for each thread group</li>
 * <li>stops all servers on test start</li>
 */
public class SmtpServer extends AbstractSampler implements TestBean, DefaultedTestStateListener {
	private static final long serialVersionUID = -4167583106547081311L;
	private static final Logger LOG = LoggerFactory.getLogger(SmtpServer.class);
	private static final SmtpSinkPool DEFAULT_POOL = new SmtpSinkPool();

	private final transient SmtpSinkPool pool;

	private String serverHost = "localhost";
	private Integer serverPort = 25;

	private Integer connectTimeout = 0;
	private Integer readTimeout = 0;

	private Boolean authEnabled = false;
	private String authUsername;
	private String authPassword;

	/**
	 * Unit testing constructor
	 */
	SmtpServer(SmtpSinkPool pool) {
		this.pool = pool;
	}

	/**
	 * Default construction uses static pool
	 */
	public SmtpServer() {
		this.pool = DEFAULT_POOL;
	}

	/**
	 * Lazy creation of smtp server.
	 * 
	 * @return configured and started smtp server
	 */
	SmtpSink getOrCreateServer() {
		// TODO: use something (hash of config) to allow multiple servers per thread group?
		Integer id = getThreadContext().getThreadGroup().hashCode();
		String computedKey = id.toString();

		return pool.computeIfAbsent(computedKey, key -> {
			LOG.error("creating server for thread group [{}]", key);

			SmtpSink server = new SmtpSink(getServerHost(), getServerPort());
			if (getAuthEnabled())
				server.enableAuth(getAuthUsername(), getAuthPassword());
			// TODO:tls
			// TODO:timeouts
			server.start();
			return server;
		});
	}

	@Override
	public SampleResult sample(Entry e) {
		LOG.trace("sample-{}", this.hashCode());

		SmtpSink server = getOrCreateServer();

		SampleResult sample = new SampleResult();
		sample.setSampleLabel(getName());
		sample.setSamplerData(String.format("smtp://%s:%s", getServerHost(), getServerPort()));
		sample.sampleStart();

		try {
			// copy from server to sample result
			Message message = server.pop();
			if (message == null) {
				sample.setIgnore();
				throw new IOException("failed to get message");
			}

			// sample.setSampleLabel("Message " + message.getMessageId());
			// sample.setSamplerData("Message " + message.getMessageId());

			sample.setContentType(message.getMimeType());// Store the content-type
			sample.setDataEncoding("iso-8859-1"); // RFC 822 uses ascii per default
			sample.setEncodingAndType(message.getMimeType());// Parse the content-type
			sample.setDataType(SampleResult.TEXT);

			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			MessageWriter writer = new DefaultMessageWriter();
			writer.writeMessage(message, bout);
			sample.setResponseData(bout.toByteArray()); // Save raw message

			sample.setResponseOK();
		} catch (IOException ex) {
			// log.debug("",ex);// No need to log normally, as we set the status
			sample.setResponseCode("500"); // $NON-NLS-1$
			sample.setResponseMessage(ex.toString());
			sample.setSuccessful(false);
		}

		sample.sampleEnd();
		return sample;
	}

	@Override
	public void testEnded() {
		LOG.info("testEnded - shutting down plugin");
		pool.shutdown();
	}

	public String getServerHost() {
		return serverHost;
	}

	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

	public Integer getServerPort() {
		return serverPort;
	}

	public void setServerPort(Integer serverPort) {
		this.serverPort = serverPort;
	}

	public Integer getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public Integer getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(Integer readTimeout) {
		this.readTimeout = readTimeout;
	}

	public Boolean getAuthEnabled() {
		return authEnabled;
	}

	public void setAuthEnabled(Boolean authEnabled) {
		this.authEnabled = authEnabled;
	}

	public String getAuthUsername() {
		return authUsername;
	}

	public void setAuthUsername(String authUsername) {
		this.authUsername = authUsername;
	}

	public String getAuthPassword() {
		return authPassword;
	}

	public void setAuthPassword(String authPassword) {
		this.authPassword = authPassword;
	}
}
