package com.zvecr.jmeter.smtp.server;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SmtpSinkPoolTest {

	@Mock
	SmtpSink sink;

	SmtpSinkPool pool;

	@Before
	public void init() throws Exception {
		pool = new SmtpSinkPool();
	}

	@Test
	public void add_only_once() {
		pool.computeIfAbsent("a", (key) -> sink);
		pool.computeIfAbsent("a", (key) -> {
			fail();
			return null;
		});
	}

	@Test
	public void shutdown_called_for_all_sinks() {
		pool.computeIfAbsent("a", (key) -> sink);
		pool.computeIfAbsent("b", (key) -> sink);
		pool.shutdown();

		Mockito.verify(sink, Mockito.times(2)).stop();
	}
}
