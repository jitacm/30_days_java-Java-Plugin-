package editor.api;

import javax.swing.JTextArea;

public interface Plugin {
    String getName();
    void execute(JTextArea textArea);
}
