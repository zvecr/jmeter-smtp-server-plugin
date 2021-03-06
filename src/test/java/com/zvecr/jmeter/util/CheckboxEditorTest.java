package com.zvecr.jmeter.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.swing.JCheckBox;

import org.junit.Before;
import org.junit.Test;

public class CheckboxEditorTest {

    private CheckboxEditor editor;

    @Before
    public void init() throws Exception {
        editor = new CheckboxEditor();
    }

    @Test
    public void validEditor() {
        assertTrue(editor.supportsCustomEditor());
        assertTrue(editor.getCustomEditor() instanceof JCheckBox);
    }

    @Test
    public void asTextConversion() {
        editor.setAsText("true");
        assertEquals("true", editor.getAsText());

        editor.setAsText("false");
        assertEquals("false", editor.getAsText());
    }

    @Test
    public void asBooleanConversion() {
        editor.setValue(true);
        assertEquals(true, editor.getValue());

        editor.setValue(false);
        assertEquals(false, editor.getValue());

        editor.setValue("true");
        assertEquals(true, editor.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void asUnexpectedConversion() {
        editor.setValue(1);
    }
}
