package com.zvecr.jmeter.smtp;

import static org.junit.Assert.*;

import org.junit.Test;

public class SmtpServerBeanInfoTest {

	@Test
	public void expected_number_of_fields() {
		SmtpServerBeanInfo info = new SmtpServerBeanInfo();
		assertEquals(7, info.getPropertyDescriptors().length);
	}

}
