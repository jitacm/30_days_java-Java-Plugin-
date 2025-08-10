package plugins.spellcheck;

import editor.api.Plugin;
import javax.swing.*;
import java.util.*;
import java.util.regex.*;

public class SpellCheckPlugin implements Plugin {

    private Set<String> dictionary;

    @Override
    public String getName() {
        return "Spell Check Plugin";
    }

    @Override
    public void execute(JTextArea editor) {
        loadDictionary();

        String text = editor.getText().toLowerCase();
        Matcher matcher = Pattern.compile("\\b\\w+\\b").matcher(text);


        java.util.List<String> misspelled = new ArrayList<>();

        while (matcher.find()) {
            String word = matcher.group();
            if (!dictionary.contains(word)) {
                misspelled.add(word);
            }
        }

        if (misspelled.isEmpty()) {
            JOptionPane.showMessageDialog(editor, "No spelling errors found!",
                    "Spell Check", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(editor,
                    "Misspelled words:\n" + String.join(", ", misspelled),
                    "Spell Check", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void loadDictionary() {
        dictionary = new HashSet<>(Arrays.asList(
                "this", "is", "a", "simple", "spell", "check", "plugin", "for",
                "the", "modern", "plugin", "text", "editor", "java", "code"
     
        ));
    }
}
