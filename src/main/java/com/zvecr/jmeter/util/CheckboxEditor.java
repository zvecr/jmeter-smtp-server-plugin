package com.zvecr.jmeter.util;

import java.awt.Component;
import java.beans.PropertyEditorSupport;

import javax.swing.JCheckBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds support for {@link JCheckBox} as a {@link PropertyDescriptor} editor type
 */
public class CheckboxEditor extends PropertyEditorSupport {

    private static final Logger LOG = LoggerFactory.getLogger(CheckboxEditor.class);

    private final JCheckBox check = new JCheckBox();

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public Component getCustomEditor() {
        return check;
    }

    @Override
    public Object getValue() {
        return Boolean.valueOf(check.isSelected());
    }

    @Override
    public String getAsText() {
        return Boolean.toString((Boolean) getValue());
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            // do something with default ???
            LOG.trace("Attepted to set {} to null", this);
        } else if (value instanceof String) {
            check.setSelected(Boolean.valueOf((String) value));
        } else if (value instanceof Boolean) {
            check.setSelected((Boolean) value);
        } else {
            throw new IllegalArgumentException("Unexpected type: " + value.getClass().getName());
        }
    }

    @Override
    public void setAsText(String value) {
        this.setValue(value);
    }
}
