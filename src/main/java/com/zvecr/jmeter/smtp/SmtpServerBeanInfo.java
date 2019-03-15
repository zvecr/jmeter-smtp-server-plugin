package com.zvecr.jmeter.smtp;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.IntegerPropertyEditor;
import org.apache.jmeter.testbeans.gui.TypeEditor;

import com.zvecr.jmeter.util.CheckboxEditor;
import com.zvecr.jmeter.util.RadioButtonEditor;

/**
 * GUI descriptors for {@link SmtpServer} - enables auto generation of UI
 * elements
 */
public class SmtpServerBeanInfo extends BeanInfoSupport {

	/**
	 * Configure descriptors for {@link SmtpServer}
	 */
	public SmtpServerBeanInfo() {
		super(SmtpServer.class);

		genServerSettingsGroup();
		genTimeoutSettingsGroup();
		genAuthSettingsGroup();
		genSecuritySettingsGroup();
		genAdditionalSettingsGroup();
	}

	private void genServerSettingsGroup() {
		createPropertyGroup("serverSettingsGroup", new String[] { "serverHost", "serverPort" });

		PropertyDescriptor p = property("serverHost");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");

		p = property("serverPort");
		p.setPropertyEditorClass(IntegerPropertyEditor.class);
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, 25);
	}

	private void genTimeoutSettingsGroup() {
		createPropertyGroup("timeoutSettingsGroup", new String[] { "connectTimeout", "readTimeout" });

		PropertyDescriptor p = property("connectTimeout");
		p.setPropertyEditorClass(IntegerPropertyEditor.class);
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, 0);

		p = property("readTimeout");
		p.setPropertyEditorClass(IntegerPropertyEditor.class);
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, 0);
	}

	private void genAuthSettingsGroup() {
		createPropertyGroup("authSettingsGroup", new String[] { "authEnabled", "authUsername", "authPassword" });

		PropertyDescriptor p = property("authEnabled");
		p.setPropertyEditorClass(CheckboxEditor.class);
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, Boolean.FALSE);

		p = property("authUsername");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");

		p = property("authPassword", TypeEditor.PasswordEditor);
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");
	}

	private void genSecuritySettingsGroup() {
		createPropertyGroup("securitySettingsGroup",
				new String[] { "sslEnabled", "useKeyStore", "keyStoreLocation", "keyStorePassword" });

		PropertyDescriptor p = property("sslEnabled");
		p.setPropertyEditorClass(RadioButtonEditor.class);
		p.setValue(RadioButtonEditor.OPTS, new String[] { "useNoSecurity", "useSSL", "useStartTLS" });
		p.setValue(RESOURCE_BUNDLE, this.getBeanDescriptor().getValue(RESOURCE_BUNDLE));
		p.setValue(DEFAULT, "useNoSecurity");

		p = property("useKeyStore");
		p.setPropertyEditorClass(CheckboxEditor.class);
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, Boolean.FALSE);

		p = property("keyStoreLocation");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");
		
		p = property("keyStorePassword");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");
	}

	private void genAdditionalSettingsGroup() {
		createPropertyGroup("additionalSettingsGroup", new String[] { "debugEnabled" });

		PropertyDescriptor p = property("debugEnabled");
		p.setPropertyEditorClass(CheckboxEditor.class);
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, Boolean.FALSE);
	}
}
