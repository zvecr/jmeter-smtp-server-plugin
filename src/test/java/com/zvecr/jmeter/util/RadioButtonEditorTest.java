package com.zvecr.jmeter.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RadioButtonEditorTest {

    @Mock
    private PropertyDescriptor descriptor;

    private RadioButtonEditor editor;

    @Before
    public void init() throws Exception {
        // ResourceBundle has final methods
        Mockito.when(descriptor.getValue(BeanInfoSupport.RESOURCE_BUNDLE)).thenReturn(new ResourceBundle() {
            @Override
            public Enumeration<String> getKeys() {
                return Collections.enumeration(Arrays.asList("foo", "bar"));
            }

            @Override
            protected Object handleGetObject(String key) {
                return "KEY[" + key + "]";
            }
        });
        Mockito.when(descriptor.getValue(RadioButtonEditor.OPTS)).thenReturn(new String[] { "foo", "bar" });

        editor = new RadioButtonEditor();
        editor.setDescriptor(descriptor);
    }

    @Test
    public void validEditor() {
        assertTrue(editor.supportsCustomEditor());
        assertTrue(editor.getCustomEditor() instanceof JPanel);
    }

    @Test
    public void asTextConversion() {
        editor.setAsText("foo");
        assertEquals("foo", editor.getAsText());

        editor.setAsText("bar");
        assertEquals("bar", editor.getAsText());
    }

    @Test(expected = IllegalArgumentException.class)
    public void asUnexpectedConversion() {
        editor.setValue(1);
    }
}
