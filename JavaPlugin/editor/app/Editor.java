package editor.app;

import editor.api.Plugin;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoManager;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

/**
 * The main editor application with enhanced UI and Undo/Redo support.
 */
public class Editor extends JFrame {

    private JTextArea textArea;
    private JFileChooser fileChooser;
    private JLabel statusBar;
    private JMenu pluginsMenu;
    private List<Plugin> loadedPlugins = new ArrayList<>();
    private UndoManager undoManager = new UndoManager();

    public Editor() {
        // Set window title and size
        setTitle("Modern Plugin Text Editor");
        setSize(900, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Apply modern Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Text Area with Enhanced Styling
        textArea = new JTextArea();
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        textArea.setBackground(new Color(45, 45, 45));
        textArea.setForeground(new Color(220, 220, 220));
        textArea.setCaretColor(Color.WHITE);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setTabSize(4);

        // Add text area to scroll pane with padding
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        // Status Bar (bottom of window)
        statusBar = new JLabel(" Lines: 0 | Words: 0 | Chars: 0 ");
        statusBar.setOpaque(true);
        statusBar.setBackground(new Color(60, 60, 60));
        statusBar.setForeground(Color.WHITE);
        statusBar.setHorizontalAlignment(SwingConstants.LEFT);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusBar, BorderLayout.SOUTH);

        // Real-time status update
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateStatus(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateStatus(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateStatus(); }
        });

        // Add Undo/Redo support
        textArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                undoManager.addEdit(e.getEdit());
            }
        });

        fileChooser = new JFileChooser();
        setupMenuBar();
        loadPlugins();
    }

    /**
     * Sets up the menu bar with File, Edit, and Plugins menus.
     */
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        addMenuItem(fileMenu, "Open", "Open a text file", e -> openFile());
        addMenuItem(fileMenu, "Save", "Save the current file", e -> saveFile());
        addMenuItem(fileMenu, "Find", "Find text in the document", e -> findText());
        fileMenu.addSeparator();
        addMenuItem(fileMenu, "Exit", "Exit the application", e -> System.exit(0));
        menuBar.add(fileMenu);

        // Edit Menu (Undo/Redo)
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');

        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.setToolTipText("Undo last change");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
        undoItem.addActionListener(e -> {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        });

        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.setToolTipText("Redo last undone change");
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));
        redoItem.addActionListener(e -> {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        });

        editMenu.add(undoItem);
        editMenu.add(redoItem);
        menuBar.add(editMenu);

        // Plugins Menu
        pluginsMenu = new JMenu("Plugins");
        pluginsMenu.setMnemonic('P');
        pluginsMenu.setToolTipText("Access available plugins");
        menuBar.add(pluginsMenu);

        setJMenuBar(menuBar);
    }

    /**
     * Helper method to add menu items with tooltips.
     */
    private void addMenuItem(JMenu menu, String text, String tooltip, ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        item.setToolTipText(tooltip);
        item.addActionListener(listener);
        menu.add(item);
    }

    /**
     * Updates the status bar with current text statistics.
     */
    private void updateStatus() {
        SwingUtilities.invokeLater(() -> {
            String text = textArea.getText();
            int lines = textArea.getLineCount();
            String[] words = text.trim().isEmpty() ? new String[0] : text.trim().split("\\s+");
            int wordCount = words.length;
            int charCount = text.length();
            statusBar.setText(String.format(" Lines: %d | Words: %d | Chars: %d ", lines, wordCount, charCount));
        });
    }

    /**
     * Loads plugins and adds them to the plugins menu.
     */
    private void loadPlugins() {
        File pluginsDir = new File("plugins");
        if (!pluginsDir.exists() || !pluginsDir.isDirectory()) {
            pluginsMenu.add(new JMenuItem("No plugins found"));
            return;
        }

        File[] pluginFiles = pluginsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (pluginFiles == null || pluginFiles.length == 0) {
            pluginsMenu.add(new JMenuItem("No plugins found"));
            return;
        }

        for (File file : pluginFiles) {
            try {
                URL jarUrl = file.toURI().toURL();
                URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, Plugin.class.getClassLoader());

                try (JarFile jarFile = new JarFile(file)) {
                    java.util.Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (entry.getName().endsWith(".class")) {
                            String className = entry.getName().replace("/", ".").replaceAll(".class$", "");
                            try {
                                Class<?> cls = classLoader.loadClass(className);
                                if (Plugin.class.isAssignableFrom(cls) && !cls.isInterface()) {
                                    Plugin plugin = (Plugin) cls.getDeclaredConstructor().newInstance();
                                    loadedPlugins.add(plugin);

                                    JMenuItem pluginItem = new JMenuItem(plugin.getName());
                                    pluginItem.setToolTipText("Run " + plugin.getName());
                                    pluginItem.addActionListener(e -> {
                                        try {
                                            plugin.execute(textArea);
                                        } catch (Exception ex) {
                                            JOptionPane.showMessageDialog(Editor.this,
                                                    "Error executing plugin: " + ex.getMessage(),
                                                    "Error", JOptionPane.ERROR_MESSAGE);
                                        }
                                    });

                                    pluginsMenu.add(pluginItem);
                                    System.out.println("Loaded plugin: " + plugin.getName());
                                }
                            } catch (ClassNotFoundException e) {
                                // Ignore non-plugin classes
                            } catch (Exception e) {
                                System.err.println("Error instantiating plugin from " + className + ": " + e.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading plugin JAR: " + file.getName() + " - " + e.getMessage());
            }
        }

        if (loadedPlugins.isEmpty()) {
            pluginsMenu.add(new JMenuItem("No valid plugins found"));
        }
    }

    private void openFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                textArea.read(reader, null);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveFile() {
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                textArea.write(writer);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void findText() {
        String textToFind = JOptionPane.showInputDialog(this, "Enter text to find:");
        if (textToFind != null && !textToFind.isEmpty()) {
            String text = textArea.getText();
            int index = text.indexOf(textToFind);
            if (index != -1) {
                textArea.setCaretPosition(index);
                textArea.setSelectionStart(index);
                textArea.setSelectionEnd(index + textToFind.length());
                textArea.requestFocus();
            } else {
                JOptionPane.showMessageDialog(this, "Text not found", "Not Found",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Editor().setVisible(true);
        });
    }
}
