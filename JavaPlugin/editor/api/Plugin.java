package editor.api;

import javax.swing.JTextArea;

/**
 * Interface for plugins that can be loaded dynamically by the editor.
 */
public interface Plugin {

    /**
     * Returns the name of the plugin.
     *
     * @return The plugin name.
     */
    String getName();

    /**
     * Executes the plugin's functionality on the given text area.
     *
     * @param textArea The text area to operate on.
     */
    void execute(JTextArea textArea);
}