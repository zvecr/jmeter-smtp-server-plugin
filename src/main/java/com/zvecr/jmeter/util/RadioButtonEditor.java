package com.zvecr.jmeter.util;

import java.awt.Component;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TestBeanPropertyEditor;

/**
 * Adds support for multiple {@link JRadioButton} as a {@link PropertyDescriptor} editor type
 */
public class RadioButtonEditor extends PropertyEditorSupport implements TestBeanPropertyEditor {

    public static final String OPTS = "OPTS";

    private final JPanel panel = new JPanel();
    private final ButtonGroup group = new ButtonGroup();
    private final List<JRadioButton> buttons = new ArrayList<>();

    @Override
    public void setDescriptor(PropertyDescriptor descriptor) {
        ResourceBundle rb = (ResourceBundle) descriptor.getValue(BeanInfoSupport.RESOURCE_BUNDLE);
        String[] tags = (String[]) descriptor.getValue(OPTS);
        for (String tag : tags) {
            JRadioButton btn = new JRadioButton(tag);
            btn.setActionCommand(tag);

            if (rb != null) {
                btn.setToolTipText(rb.getString(tag + ".shortDescription"));
                btn.setText(rb.getString(tag + ".displayName"));
            }

            buttons.add(btn);
            group.add(btn);
            panel.add(btn);
        }
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public Component getCustomEditor() {
        return panel;
    }

    @Override
    public Object getValue() {
        return buttons.stream().filter(JRadioButton::isSelected).map(JRadioButton::getActionCommand).findFirst().orElse(null);
    }

    @Override
    public String getAsText() {
        return getValue().toString();
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            // do something with default ???
        } else if (value instanceof String) {
            group.clearSelection();
            Optional<JRadioButton> button = buttons.stream().filter(x -> x.getActionCommand().equals(value)).findFirst();
            button.ifPresent(btn -> btn.setSelected(true));
        } else {
            throw new IllegalArgumentException("Unexpected type: " + value.getClass().getName());
        }
    }

    @Override
    public void setAsText(String value) {
        this.setValue(value);
    }
}
