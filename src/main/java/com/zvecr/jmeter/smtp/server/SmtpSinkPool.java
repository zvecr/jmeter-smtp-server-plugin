package com.zvecr.jmeter.smtp.server;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keyed (Thread safe) storage of {@link SmtpSink} objects
 */
public class SmtpSinkPool {
	private static final Logger LOG = LoggerFactory.getLogger(SmtpSinkPool.class);

	private final Map<String, SmtpSink> pool = new HashMap<>();

	/**
	 * {@link java.util.HashMap#computeIfAbsent(Object, Function)}
	 */
	public synchronized SmtpSink computeIfAbsent(String key, Function<String, SmtpSink> createFunction) {
		return pool.computeIfAbsent(key, createFunction);
	}

	/**
	 * Shutdown all currently stored {@link SmtpSink} objects
	 */
	public synchronized void shutdown() {
		pool.entrySet().forEach(item -> {
			LOG.trace("shutdown - {}", item.getKey());
			item.getValue().stop();
		});
		pool.clear();
	}
}
