package com.zvecr.jmeter.smtp;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.mail.AuthenticationFailedException;

import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.MailerBuilder.MailerRegularBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;

import com.zvecr.jmeter.smtp.server.SmtpSinkPool;

public class SmtpServerIT {
    private static final Email email = EmailBuilder.startingBlank().from("foo@bar.com").to("bar@foo.com").withSubject("TestMail").withPlainText("This is a test mail").buildEmail();

    @Rule
    private ExpectedException expectedException = ExpectedException.none();

    private SmtpSinkPool pool;
    private SmtpServer server;

    private static MailerRegularBuilder getMailer() {
        return MailerBuilder.withSMTPServer("localhost", 10025).withSessionTimeout(2500);
    }

    @Before
    public void init() {
        pool = new SmtpSinkPool();
        server = new SmtpServer(pool);
        server.setServerHost("127.0.0.1");
        server.setServerPort(10025);
        server.setConnectTimeout(5000);
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
    public void plainTextReceive() throws Exception {
        // start
        server.getOrCreateServer();

        Mailer mailer = getMailer().withTransportStrategy(TransportStrategy.SMTP).buildMailer();
        mailer.sendMail(email);
        mailer.sendMail(email);

        // validate
        assertNotNull(server.sample(null).getResponseDataAsString());
        assertNotNull(server.sample(null).getResponseDataAsString());
        assertNull(server.sample(null).getResponseDataAsString());
    }

    @Test
    public void goodAuthentication() throws Exception {
        // configure
        server.setAuthEnabled(true);
        server.setAuthUsername("user");
        server.setAuthPassword("password");

        // start
        server.getOrCreateServer();

        Mailer mailer = getMailer().withTransportStrategy(TransportStrategy.SMTP).withSMTPServerUsername("user").withSMTPServerPassword("password").buildMailer();
        mailer.sendMail(email);

        // validate
        assertNotNull(server.sample(null).getResponseDataAsString());
        assertNull(server.sample(null).getResponseDataAsString());
    }

    @Test
    public void badAuthentication() throws Exception {
        // configure
        server.setAuthEnabled(true);
        server.setAuthUsername("user");
        server.setAuthPassword("password");
        expectedException.expectCause(isA(AuthenticationFailedException.class));

        // start
        server.getOrCreateServer();

        Mailer mailer = getMailer().withTransportStrategy(TransportStrategy.SMTP).withSMTPServerUsername("user").withSMTPServerPassword("wordpass").buildMailer();
        mailer.sendMail(email);

        // validate - done by expectedException rule
    }

    @Test
    public void selfSignedSSL() throws Exception {
        // configure
        server.setSslEnabled("useSSL");

        // start
        server.getOrCreateServer();

        getMailer().withTransportStrategy(TransportStrategy.SMTPS).withProperty("mail.smtps.ssl.checkserveridentity", "false").buildMailer().sendMail(email);

        // validate
        assertNotNull(server.sample(null).getResponseDataAsString());
        assertNull(server.sample(null).getResponseDataAsString());
    }

    @Test
    public void selfSignedSTARTTLS() throws Exception {
        // configure
        server.setSslEnabled("useStartTLS");

        // start
        server.getOrCreateServer();

        getMailer().withTransportStrategy(TransportStrategy.SMTP_TLS).withProperty("mail.smtp.ssl.checkserveridentity", "false").buildMailer().sendMail(email);

        // validate
        assertNotNull(server.sample(null).getResponseDataAsString());
        assertNull(server.sample(null).getResponseDataAsString());
    }
}
