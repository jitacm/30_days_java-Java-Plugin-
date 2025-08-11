package plugins.theme;

import editor.api.Plugin;
import javax.swing.*;
import java.awt.*;

public class ThemePlugin implements Plugin {

    @Override
    public String getName() {
        return "Theme & Accessibility";
    }

    @Override
    public void execute(JTextArea editor) {
        String[] options = {"Dark Theme", "Light Theme", "Increase Font Size", "Decrease Font Size"};
        int choice = JOptionPane.showOptionDialog(
                editor,
                "Choose theme or font size:",
                "Theme & Accessibility",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        Font currentFont = editor.getFont();
        switch (choice) {
            case 0: // Dark Theme
                editor.setBackground(new Color(40, 44, 52));
                editor.setForeground(Color.WHITE);
                editor.setCaretColor(Color.WHITE);
                editor.setSelectionColor(new Color(80, 80, 120));
                break;
            case 1: // Light Theme
                editor.setBackground(Color.WHITE);
                editor.setForeground(Color.BLACK);
                editor.setCaretColor(Color.BLACK);
                editor.setSelectionColor(new Color(180, 200, 250));
                break;
            case 2: // Increase Font Size
                editor.setFont(new Font(currentFont.getName(), currentFont.getStyle(), Math.min(currentFont.getSize()+2, 40)));
                break;
            case 3: // Decrease Font Size
                editor.setFont(new Font(currentFont.getName(), currentFont.getStyle(), Math.max(currentFont.getSize()-2, 10)));
                break;
            default:
                // Do nothing
        }
    }
}
