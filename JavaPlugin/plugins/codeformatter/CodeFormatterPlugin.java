package plugins.codeformatter;

import editor.api.Plugin;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * An advanced plugin to automatically format code.
 * This version includes a more robust language detection system and
 * formatting rules for Java, Python, and C++.
 */
public class CodeFormatterPlugin implements Plugin {

    // Define language-specific keywords for more accurate detection
    private static final Set<String> JAVA_KEYWORDS = new HashSet<>(Arrays.asList(
        "public class", "import java.", "package ", "static void main", "System.out.println"
    ));
    private static final Set<String> PYTHON_KEYWORDS = new HashSet<>(Arrays.asList(
        "def ", "import ", "print(", "class ", "if __name__ == '__main__'"
    ));
    private static final Set<String> CPP_KEYWORDS = new HashSet<>(Arrays.asList(
        "#include <", "int main()", "std::", "using namespace"
    ));

    @Override
    public String getName() {
        return "Format Code";
    }

    @Override
    public void execute(JTextArea textArea) {
        String originalText = textArea.getText();
        if (originalText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(textArea, "The document is empty.",
                    "Code Formatter", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Step 1: Detect the programming language
        String language = detectLanguage(originalText);

        // Step 2: Format the code based on the detected language
        String formattedText;
        switch (language) {
            case "Java":
                formattedText = formatJavaCode(originalText);
                break;
            case "Python":
                formattedText = formatPythonCode(originalText);
                break;
            case "C++":
                formattedText = formatCplusplusCode(originalText);
                break;
            default:
                formattedText = originalText;
                break;
        }

        // Step 3: Update the text area and show a message
        if (!formattedText.equals(originalText)) {
            textArea.setText(formattedText);
            JOptionPane.showMessageDialog(textArea,
                    "Code has been formatted for " + language + ".",
                    "Code Formatter", JOptionPane.INFORMATION_MESSAGE);
        } else if (language.equals("Unknown")) {
             JOptionPane.showMessageDialog(textArea,
                    "Language could not be detected. No formatting applied.",
                    "Code Formatter", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(textArea,
                    "Code formatting for " + language + " was not needed.",
                    "Code Formatter", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Determines the most likely programming language based on a set of keywords.
     * @param text The text to analyze.
     * @return The name of the detected language, or "Unknown".
     */
    private String detectLanguage(String text) {
        String lowerCaseText = text.toLowerCase();
        int javaScore = countOccurrences(lowerCaseText, JAVA_KEYWORDS);
        int pythonScore = countOccurrences(lowerCaseText, PYTHON_KEYWORDS);
        int cppScore = countOccurrences(lowerCaseText, CPP_KEYWORDS);

        if (javaScore > pythonScore && javaScore > cppScore) {
            return "Java";
        } else if (pythonScore > javaScore && pythonScore > cppScore) {
            return "Python";
        } else if (cppScore > javaScore && cppScore > pythonScore) {
            return "C++";
        }
        return "Unknown";
    }
    
    /**
     * Helper method to count occurrences of keywords in a string.
     */
    private int countOccurrences(String text, Set<String> keywords) {
        int count = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Formats Java-style code by adjusting indentation based on braces and semicolons.
     * @param text The Java code as a single string.
     * @return The formatted Java code.
     */
    private String formatJavaCode(String text) {
        StringBuilder formatted = new StringBuilder();
        int indentLevel = 0;
        int tabSize = 4;
        String[] lines = text.split("\n");

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                formatted.append("\n");
                continue;
            }

            // Decrease indent for closing braces
            if (trimmedLine.startsWith("}")) {
                indentLevel = Math.max(0, indentLevel - 1);
            }
            
            // Append indentation
            formatted.append(" ".repeat(indentLevel * tabSize));
            formatted.append(trimmedLine);
            formatted.append("\n");

            // Increase indent for opening braces
            if (trimmedLine.endsWith("{")) {
                indentLevel++;
            }
        }
        return formatted.toString();
    }

    /**
     * Formats Python-style code by adjusting indentation based on colons.
     * @param text The Python code as a single string.
     * @return The formatted Python code.
     */
    private String formatPythonCode(String text) {
        StringBuilder formatted = new StringBuilder();
        int indentLevel = 0;
        int tabSize = 4;
        String[] lines = text.split("\n");

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                formatted.append("\n");
                continue;
            }

            // Decrease indent for dedent statements
            // A more complex parser would be needed for perfect dedenting
            if (trimmedLine.startsWith("return") || trimmedLine.startsWith("break") || trimmedLine.startsWith("pass")) {
                // A simple heuristic, not always accurate
                // indentLevel = Math.max(0, indentLevel - 1);
            }

            // Append indentation
            formatted.append(" ".repeat(indentLevel * tabSize));
            formatted.append(trimmedLine);
            formatted.append("\n");

            // Increase indent for opening blocks
            if (trimmedLine.endsWith(":")) {
                indentLevel++;
            }
        }
        return formatted.toString();
    }
    
    /**
     * Formats C++-style code, which is very similar to Java.
     * @param text The C++ code as a single string.
     * @return The formatted C++ code.
     */
    private String formatCplusplusCode(String text) {
        // C++ formatting logic is similar to Java, so we can reuse the method
        return formatJavaCode(text);
    }
}
