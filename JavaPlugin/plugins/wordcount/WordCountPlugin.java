package plugins.wordcount;

import editor.api.Plugin;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;

public class WordCountPlugin implements Plugin {
    @Override
    public String getName() {
        return "Word Count";
    }

    @Override
    public void execute(JTextArea textArea) {
        String text = textArea.getText();
        if (text == null || text.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Word count: 0");
            return;
        }
        String[] words = text.trim().split("\\s+");
        JOptionPane.showMessageDialog(null, "Word count: " + words.length);
    }
}
