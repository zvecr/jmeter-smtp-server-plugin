package com.zvecr.jmeter.smtp;

import javax.mail.AuthenticationFailedException;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.MailerBuilder.MailerRegularBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;

import com.zvecr.jmeter.smtp.server.SmtpSinkPool;

public class SmtpServerIT {

    static final Email email = EmailBuilder.startingBlank().from("foo@bar.com").to("bar@foo.com").withSubject("TestMail").withPlainText("This is a test mail").buildEmail();

    SmtpSinkPool pool;

    SmtpServer server;

    static void dumpSample(SampleResult sampleResult) {
        System.err.println(sampleResult.getResponseDataAsString());
    }

    static MailerRegularBuilder getMailer() {
        return MailerBuilder.withSMTPServer("localhost", 10025).withSessionTimeout(2500);
    }

    @Before
    public void init() {
        pool = new SmtpSinkPool();
        server = new SmtpServer(pool);
        server.setServerHost("127.0.0.1");
        server.setServerPort(10025);
        server.setConnectTimeout(25000);
        server.setReadTimeout(2500);

        JMeterContext ctx = Mockito.mock(JMeterContext.class);
        server.setThreadContext(ctx);

        Mockito.when(ctx.getThreadGroup()).thenReturn(Mockito.mock(AbstractThreadGroup.class));
    }

    @After
    public void deinit() {
        pool.shutdown();
    }

    @Test
    public void test_simple() throws Exception {
        // start
        server.getOrCreateServer();

        Mailer mailer = getMailer().withTransportStrategy(TransportStrategy.SMTP).buildMailer();
        mailer.sendMail(email);
        mailer.sendMail(email);

        dumpSample(server.sample(null));
        dumpSample(server.sample(null));
    }

    @Test
    public void test_auth() throws Exception {
        // configure
        server.setAuthEnabled(true);
        server.setAuthUsername("user");
        server.setAuthPassword("password");

        // start
        server.getOrCreateServer();

        Mailer mailer = getMailer().withTransportStrategy(TransportStrategy.SMTP).withSMTPServerUsername("user").withSMTPServerPassword("password").buildMailer();
        mailer.sendMail(email);

        dumpSample(server.sample(null));
    }

    @Test
    public void test_auth_bad() throws Exception {
        // configure
        server.setAuthEnabled(true);
        server.setAuthUsername("user");
        server.setAuthPassword("password");

        // start
        server.getOrCreateServer();

        boolean authFailed = true;
        try {
            Mailer mailer2 = getMailer().withTransportStrategy(TransportStrategy.SMTP).withSMTPServerUsername("user").withSMTPServerPassword("wordpass").buildMailer();
            mailer2.sendMail(email);
        } catch (Exception e) {
            if (e.getCause() instanceof AuthenticationFailedException)
                authFailed = true;
        }

        if (!authFailed)
            throw new RuntimeException("SENT WITH BAD PASSWORD");
    }

    @Test
    public void test_ssl() throws Exception {
        // configure
        server.setSslEnabled("useSSL");

        // start
        server.getOrCreateServer();

        getMailer().withTransportStrategy(TransportStrategy.SMTPS).withProperty("mail.smtps.ssl.checkserveridentity", "false").buildMailer().sendMail(email);

        dumpSample(server.sample(null));
    }

    @Test
    public void test_starttls() throws Exception {
        // configure
        server.setSslEnabled("useStartTLS");

        // start
        server.getOrCreateServer();

        getMailer().withTransportStrategy(TransportStrategy.SMTP_TLS).withProperty("mail.smtp.ssl.checkserveridentity", "false").buildMailer().sendMail(email);

        dumpSample(server.sample(null));
    }
}
