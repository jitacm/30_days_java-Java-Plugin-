package plugins.textstats;

import editor.api.Plugin;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A plugin to provide text statistics such as sentence count and average word length.
 */
public class TextStatsPlugin implements Plugin {

    /**
     * Returns the name of the plugin.
     *
     * @return The plugin name.
     */
    @Override
    public String getName() {
        return "Text Statistics";
    }

    /**
     * Executes the plugin's functionality on the given text area.
     * It calculates the number of sentences and the average word length,
     * then displays the results in a dialog box.
     *
     * @param textArea The text area to operate on.
     */
    @Override
    public void execute(JTextArea textArea) {
        String text = textArea.getText();

        // Check if the document is empty or contains only whitespace
        if (text.trim().isEmpty()) {
            JOptionPane.showMessageDialog(textArea, "The document is empty.",
                    "Text Statistics", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // --- Calculate Sentence Count ---
        // A sentence is considered to end with a '.', '!', or '?' followed by optional whitespace.
        // The split method divides the string and the stream filters out empty strings
        // that may result from trailing punctuation.
        String[] sentences = text.split("[.!?]\\s*");
        long sentenceCount = Arrays.stream(sentences)
                                   .filter(s -> !s.trim().isEmpty())
                                   .count();

        // --- Calculate Average Word Length ---
        // Use a regex pattern to find all words (sequences of word characters).
        Pattern wordPattern = Pattern.compile("\\b\\w+\\b");
        Matcher matcher = wordPattern.matcher(text);

        long totalCharactersInWords = 0;
        long wordCount = 0;

        // Iterate through all found words, counting their total length and the number of words.
        while (matcher.find()) {
            wordCount++;
            totalCharactersInWords += matcher.group().length();
        }

        double averageWordLength = 0;
        // Prevent division by zero if there are no words
        if (wordCount > 0) {
            averageWordLength = (double) totalCharactersInWords / wordCount;
        }

        // --- Display Results ---
        // Format the output string with the calculated statistics.
        String message = String.format(
            "Text Statistics:\n\n" +
            "Sentence Count: %d\n" +
            "Average Word Length: %.2f characters",
            sentenceCount, averageWordLength
        );

        // Show the results in a non-blocking dialog box.
        JOptionPane.showMessageDialog(textArea, message,
                "Text Statistics", JOptionPane.INFORMATION_MESSAGE);
    }
}
