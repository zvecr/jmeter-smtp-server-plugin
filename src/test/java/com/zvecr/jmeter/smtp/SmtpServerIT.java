package com.zvecr.jmeter.smtp;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.zvecr.jmeter.smtp.server.SmtpSinkPool;

public class SmtpServerIT {

	SmtpSinkPool pool;

	SmtpServer server;

	@Before
	public void init() {
		pool = new SmtpSinkPool();
		server = new SmtpServer(pool);
		server.setServerHost("127.0.0.1");
		server.setServerPort(10025);

		JMeterContext ctx = Mockito.mock(JMeterContext.class);
		server.setThreadContext(ctx);

		Mockito.when(ctx.getThreadGroup()).thenReturn(Mockito.mock(AbstractThreadGroup.class));
	}

	@After
	public void deinit() {
		pool.shutdown();
	}

	private void testMessage() throws Exception {
		Email email = new SimpleEmail();
		email.setHostName("localhost");
		email.setSmtpPort(10025);
		email.setFrom("user@gmail.com");
		email.setSubject("TestMail");
		email.setMsg("This is a test mail ... :-)");
		email.addTo("foo@bar.com");
		email.addTo("bar@foo.com");
		email.send();
	}

	@Test
	public void test() throws Exception {
		dumpSample(server.sample(null));

		testMessage();
		testMessage();

		dumpSample(server.sample(null));
		dumpSample(server.sample(null));
	}

	void dumpSample(SampleResult sampleResult) {
		System.err.println(sampleResult.getResponseDataAsString());
	}
}
