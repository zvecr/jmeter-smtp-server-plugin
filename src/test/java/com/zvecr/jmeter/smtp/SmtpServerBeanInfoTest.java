package com.zvecr.jmeter.smtp;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.apache.jmeter.util.JMeterUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class SmtpServerBeanInfoTest {

	@BeforeClass
	public static void init() {
		// bit of a bodge instead of mocking static function
		//   however this does set global state
		JMeterUtils.setLocale(Locale.ENGLISH);
	}

	@Test
	public void expected_number_of_fields() {
		SmtpServerBeanInfo info = new SmtpServerBeanInfo();
		assertEquals(12, info.getPropertyDescriptors().length);
	}

}
