package com.zvecr.jmeter.smtp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
//import java.util.function.Function;

import org.apache.james.mime4j.Charsets;
import org.apache.james.mime4j.dom.Message;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.zvecr.jmeter.smtp.server.SmtpSink;
import com.zvecr.jmeter.smtp.server.SmtpSinkPool;

@RunWith(MockitoJUnitRunner.class)
public class SmtpServerTest {

    @Mock
    SmtpSinkPool pool;

    @Mock
    SmtpSink sink;

    @Mock
    JMeterContext threadContext;

    @Mock
    AbstractThreadGroup threadGroup;

    Message message;

    SmtpServer server;

    @Before
    public void init() throws Exception {
        server = new SmtpServer(pool);
        server.setThreadContext(threadContext);

        Mockito.when(threadContext.getThreadGroup()).thenReturn(threadGroup);
        Mockito.when(pool.computeIfAbsent(Mockito.any(), Mockito.any())).thenReturn(sink);

        message = Message.Builder.of().setFrom("bob@test.com").setTo("bill@test.com").setSubject("Hello").setDate(new Date()).generateMessageId("localhost")
                .setBody("This is a message just to say hello.", Charsets.ISO_8859_1).build();
    }

    @Test
    public void timeout_produces_ignored_sample() {
        SampleResult sample = server.sample(null);
        assertTrue(sample.isIgnore());
    }

    @Test
    public void receive_produces_valid_sample() {
        Mockito.when(sink.pop()).thenReturn(message);

        SampleResult sample = server.sample(null);
        assertTrue(sample.isSuccessful());
    }

    @Test
    public void end_test_shuts_down_servers() {
        server.testEnded();
        Mockito.verify(pool).shutdown();
    }

    @Test
    public void bean_supports_server_props() {
        server.setServerHost("127.0.0.1");
        assertEquals("127.0.0.1", server.getServerHost());

        server.setServerPort(1025);
        assertEquals(Integer.valueOf(1025), server.getServerPort());
    }

    @Test
    public void bean_supports_timeout_props() {
        server.setConnectTimeout(1000);
        assertEquals(Integer.valueOf(1000), server.getConnectTimeout());

        server.setReadTimeout(5000);
        assertEquals(Integer.valueOf(5000), server.getReadTimeout());
    }

    @Test
    public void bean_supports_auth_props() {
        server.setAuthEnabled(true);
        assertEquals(true, server.getAuthEnabled());

        server.setAuthUsername("user");
        assertEquals("user", server.getAuthUsername());

        server.setAuthPassword("pass");
        assertEquals("pass", server.getAuthPassword());
    }

    // @Test
    // public void configured_with_defaults() {
    // server.setServerPort(0);
    // Mockito.when(pool.computeIfAbsent(Mockito.any(), Mockito.any())).thenAnswer((a) -> {
    // @SuppressWarnings("unchecked")
    // Function<String, SmtpSink> func = a.getArgumentAt(1, Function.class);
    // func.apply(a.getArgumentAt(0, String.class));
    // return null;
    // });
    //
    // server.getOrCreateServer();
    // }
    //
    // @Test
    // public void configured_with_auth() {
    // server.setServerPort(0);
    // server.setAuthEnabled(true);
    // server.setAuthUsername("user");
    // server.setAuthPassword("pass");
    // Mockito.when(pool.computeIfAbsent(Mockito.any(), Mockito.any())).thenAnswer((a) -> {
    // @SuppressWarnings("unchecked")
    // Function<String, SmtpSink> func = a.getArgumentAt(1, Function.class);
    // return func.apply(a.getArgumentAt(0, String.class));
    // });
    //
    // server.getOrCreateServer();
    // }
}
