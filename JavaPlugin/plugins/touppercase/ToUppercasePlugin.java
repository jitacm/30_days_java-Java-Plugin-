package plugins.touppercase;

import editor.api.Plugin;
import javax.swing.JTextArea;

public class ToUppercasePlugin implements Plugin {
    @Override
    public String getName() {
        return "Convert Selection to Uppercase";
    }
    @Override
    public void execute(JTextArea textArea) {
        String selectedText = textArea.getSelectedText();
        if (selectedText != null) {
            textArea.replaceSelection(selectedText.toUpperCase());
        }
    }
}
