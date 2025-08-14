package editor.app;

import editor.api.Plugin;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Editor extends JFrame {
    // ... [All existing fields preserved]
    private JTextArea textArea;
    private JEditorPane previewPane;
    private JFileChooser fileChooser;
    private JLabel statusBar;
    private JMenu pluginsMenu;
    private final Map<Plugin, Boolean> pluginStates = new LinkedHashMap<>();
    private File currentFile;
    private UndoManager undoManager = new UndoManager();
    private JSplitPane splitPane;
    private boolean isPreviewVisible = true;

    public Editor() {
        // ... [All existing initialization preserved]
        setTitle("Modern Plugin Text Editor");
        setSize(1200, 800); // Increased for split view
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // --- Text Area (preserved) ---
        textArea = new JTextArea();
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        textArea.setBackground(new Color(45, 45, 45));
        textArea.setForeground(new Color(220, 220, 220));
        textArea.setCaretColor(Color.WHITE);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setTabSize(4);

        // --- NEW: Preview Pane ---
        previewPane = new JEditorPane();
        previewPane.setContentType("text/html");
        previewPane.setEditable(false);
        previewPane.setBackground(new Color(240, 240, 240));
        previewPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Scroll Panes (preserved) ---
        JScrollPane textScrollPane = new JScrollPane(textArea);
        textScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane previewScrollPane = new JScrollPane(previewPane);
        previewScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- NEW: Split Pane ---
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, textScrollPane, previewScrollPane);
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        // --- Status Bar (preserved + updated) ---
        statusBar = new JLabel(" Lines: 0 | Words: 0 | Chars: 0 | Preview: ON ");
        statusBar.setOpaque(true);
        statusBar.setBackground(new Color(60, 60, 60));
        statusBar.setForeground(Color.WHITE);
        statusBar.setHorizontalAlignment(SwingConstants.LEFT);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusBar, BorderLayout.SOUTH);

        // --- Document Listeners (preserved + new preview update) ---
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updatePreview(); updateStatus(); }
            @Override public void removeUpdate(DocumentEvent e) { updatePreview(); updateStatus(); }
            @Override public void changedUpdate(DocumentEvent e) { updatePreview(); updateStatus(); }
        });

        textArea.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));
        addUndoRedoKeyBindings();

        fileChooser = new JFileChooser();

        setupToolBar();
        setupMenuBar(); // Updated to include View menu
        loadPlugins();
        
        // Initial preview update
        updatePreview();
    }

    // --- Toolbar (preserved) ---
    private void setupToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton newBtn = new JButton("New");
        newBtn.addActionListener(e -> newFile());
        toolBar.add(newBtn);

        JButton openBtn = new JButton("Open");
        openBtn.addActionListener(e -> openFile());
        toolBar.add(openBtn);

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> saveFile());
        toolBar.add(saveBtn);

        toolBar.addSeparator();

        JButton undoBtn = new JButton("Undo");
        undoBtn.addActionListener(e -> undoAction());
        toolBar.add(undoBtn);

        JButton redoBtn = new JButton("Redo");
        redoBtn.addActionListener(e -> redoAction());
        toolBar.add(redoBtn);

        add(toolBar, BorderLayout.NORTH);
    }

    // --- Menu Bar (updated with View menu) ---
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu (preserved)
        JMenu fileMenu = new JMenu("File");
        addMenuItem(fileMenu, "New", "Create a new file", e -> newFile());
        addMenuItem(fileMenu, "Open", "Open a text file", e -> openFile());
        addMenuItem(fileMenu, "Save", "Save the current file", e -> saveFile());
        addMenuItem(fileMenu, "Find and Replace", "Find and replace text", e -> findAndReplaceText());
        fileMenu.addSeparator();
        addMenuItem(fileMenu, "Exit", "Exit the application", e -> System.exit(0));
        menuBar.add(fileMenu);

        // NEW: View menu with preview toggle
        JMenu viewMenu = new JMenu("View");
        JCheckBoxMenuItem previewItem = new JCheckBoxMenuItem("Show Preview", true);
        previewItem.addActionListener(e -> togglePreview(previewItem.isSelected()));
        viewMenu.add(previewItem);
        menuBar.add(viewMenu);

        // Plugins menu (preserved)
        pluginsMenu = new JMenu("Plugins");
        menuBar.add(pluginsMenu);

        setJMenuBar(menuBar);
    }

    // --- Status Bar Update (preserved + updated) ---
    private void updateStatus() {
        SwingUtilities.invokeLater(() -> {
            String text = textArea.getText();
            int lines = textArea.getLineCount();
            int words = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
            int chars = text.length();
            statusBar.setText(String.format(" Lines: %d | Words: %d | Chars: %d | Preview: %s ", 
                lines, words, chars, isPreviewVisible ? "ON" : "OFF"));
        });
    }

    // --- Undo/Redo (preserved) ---
    private void addUndoRedoKeyBindings() {
        textArea.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
        textArea.getActionMap().put("Undo", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { undoAction(); }
        });

        textArea.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
        textArea.getActionMap().put("Redo", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { redoAction(); }
        });
    }

    private void undoAction() {
        try { if (undoManager.canUndo()) undoManager.undo(); }
        catch (CannotUndoException ignored) {}
    }

    private void redoAction() {
        try { if (undoManager.canRedo()) undoManager.redo(); }
        catch (CannotRedoException ignored) {}
    }

    // --- File Operations (preserved) ---
    private void newFile() {
        textArea.setText("");
        currentFile = null;
        setTitle("Modern Plugin Text Editor - New File");
        undoManager.discardAllEdits();
    }

    private void openFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
                textArea.read(reader, null);
                setTitle("Modern Plugin Text Editor - " + currentFile.getName());
                undoManager.discardAllEdits();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveFile() {
        if (currentFile == null) {
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                currentFile = fileChooser.getSelectedFile();
            } else return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
            textArea.write(writer);
            setTitle("Modern Plugin Text Editor - " + currentFile.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Find & Replace (preserved) ---
    private void findAndReplaceText() {
        JDialog findReplaceDialog = new JDialog(this, "Find and Replace", true);
        findReplaceDialog.setSize(400, 200);
        findReplaceDialog.setLocationRelativeTo(this);
        findReplaceDialog.setLayout(new GridLayout(5, 2, 5, 5));

        JTextField findField = new JTextField();
        JTextField replaceField = new JTextField();
        JCheckBox caseSensitive = new JCheckBox("Case Sensitive");
        JButton findButton = new JButton("Find Next");
        JButton replaceButton = new JButton("Replace");
        JButton replaceAllButton = new JButton("Replace All");

        findReplaceDialog.add(new JLabel("Find:"));
        findReplaceDialog.add(findField);
        findReplaceDialog.add(new JLabel("Replace with:"));
        findReplaceDialog.add(replaceField);
        findReplaceDialog.add(new JLabel("Options:"));
        findReplaceDialog.add(caseSensitive);

        JPanel findPanel = new JPanel(new GridLayout(1, 1));
        findPanel.add(findButton);
        findReplaceDialog.add(findPanel);

        JPanel replacePanel = new JPanel(new GridLayout(1, 2, 5, 5));
        replacePanel.add(replaceButton);
        replacePanel.add(replaceAllButton);
        findReplaceDialog.add(replacePanel);

        findButton.addActionListener(e -> {
            String textToFind = findField.getText();
            String editorText = caseSensitive.isSelected() ? textArea.getText() : textArea.getText().toLowerCase();
            String findText = caseSensitive.isSelected() ? textToFind : textToFind.toLowerCase();

            if (textToFind.isEmpty()) return;

            int start = textArea.getSelectionEnd();
            int index = editorText.indexOf(findText, start);
            if (index != -1) {
                textArea.setCaretPosition(index);
                textArea.setSelectionStart(index);
                textArea.setSelectionEnd(index + textToFind.length());
                textArea.requestFocus();
            } else {
                JOptionPane.showMessageDialog(findReplaceDialog, "Text not found from current position.", "Not Found", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        replaceButton.addActionListener(e -> {
            String replaceWith = replaceField.getText();
            if (textArea.getSelectedText() != null && textArea.getSelectedText().length() == findField.getText().length()) {
                textArea.replaceSelection(replaceWith);
            } else {
                JOptionPane.showMessageDialog(findReplaceDialog, "Please select the text to be replaced first.", "Cannot Replace", JOptionPane.WARNING_MESSAGE);
            }
        });

        replaceAllButton.addActionListener(e -> {
            String findStr = findField.getText();
            String replaceStr = replaceField.getText();
            String originalText = textArea.getText();
            
            String newText = originalText;
            if (caseSensitive.isSelected()) {
                newText = newText.replaceAll(Pattern.quote(findStr), Matcher.quoteReplacement(replaceStr));
            } else {
                Pattern p = Pattern.compile(Pattern.quote(findStr), Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(originalText);
                newText = m.replaceAll(Matcher.quoteReplacement(replaceStr));
            }
            
            textArea.setText(newText);
            JOptionPane.showMessageDialog(findReplaceDialog, "All occurrences replaced.", "Replace All", JOptionPane.INFORMATION_MESSAGE);
        });

        findReplaceDialog.setVisible(true);
    }

    // --- Plugin System (preserved) ---
    private void loadPlugins() {
        pluginsMenu.removeAll();
        pluginStates.clear();

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
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (entry.getName().endsWith(".class")) {
                            String className = entry.getName().replace("/", ".").replace(".class", "");
                            try {
                                Class<?> cls = classLoader.loadClass(className);
                                if (Plugin.class.isAssignableFrom(cls) && !cls.isInterface()) {
                                    Plugin plugin = (Plugin) cls.getDeclaredConstructor().newInstance();
                                    pluginStates.put(plugin, true);
                                    JMenuItem pluginItem = new JMenuItem(plugin.getName());
                                    pluginItem.addActionListener(e -> {
                                        if (!pluginStates.get(plugin)) {
                                            JOptionPane.showMessageDialog(this, "Plugin is disabled.", "Plugin Disabled", JOptionPane.WARNING_MESSAGE);
                                            return;
                                        }
                                        plugin.execute(textArea);
                                    });
                                    pluginsMenu.add(pluginItem);
                                }
                            } catch (ClassNotFoundException ignored) {}
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading plugin JAR: " + file.getName() + " - " + e.getMessage());
            }
        }

        pluginsMenu.addSeparator();
        JMenuItem manageItem = new JMenuItem("Manage Plugins...");
        manageItem.addActionListener(e -> openPluginManager());
        pluginsMenu.add(manageItem);

        JMenuItem reloadItem = new JMenuItem("Reload Plugins");
        reloadItem.addActionListener(e -> loadPlugins());
        pluginsMenu.add(reloadItem);
    }

    private void openPluginManager() {
        JDialog dialog = new JDialog(this, "Plugin Manager", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(0, 3, 5, 5));
        for (Plugin plugin : pluginStates.keySet()) {
            JLabel nameLabel = new JLabel(plugin.getName());
            JButton toggleBtn = new JButton(pluginStates.get(plugin) ? "Disable" : "Enable");
            JLabel statusLabel = new JLabel(pluginStates.get(plugin) ? "Enabled" : "Disabled");
            toggleBtn.addActionListener(e -> {
                boolean current = pluginStates.get(plugin);
                pluginStates.put(plugin, !current);
                toggleBtn.setText(!current ? "Disable" : "Enable");
                statusLabel.setText(!current ? "Enabled" : "Disabled");
            });
            panel.add(nameLabel);
            panel.add(toggleBtn);
            panel.add(statusLabel);
        }

        dialog.add(new JScrollPane(panel), BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // --- NEW: Preview Functionality ---
    private void togglePreview(boolean show) {
        isPreviewVisible = show;
        previewPane.setVisible(show);
        splitPane.setDividerLocation(show ? 0.5 : 1.0);
        updateStatus();
    }

    private void updatePreview() {
        if (!isPreviewVisible) return;
        
        SwingUtilities.invokeLater(() -> {
            String text = textArea.getText();
            String contentType = detectContentType(text);
            
            if ("text/html".equals(contentType)) {
                previewPane.setContentType("text/html");
                previewPane.setText(text);
            } else if ("text/markdown".equals(contentType)) {
                previewPane.setContentType("text/html");
                previewPane.setText(convertMarkdownToHtml(text));
            } else {
                previewPane.setContentType("text/plain");
                previewPane.setText("No preview available for plain text");
            }
        });
    }

    private String detectContentType(String text) {
        // Simple detection - check for HTML tags or Markdown headers
        if (text.trim().startsWith("<") && text.trim().endsWith(">")) {
            return "text/html";
        }
        if (text.contains("# ") || text.contains("## ") || text.contains("**") || 
            text.contains("* ") || text.contains("- ") || text.contains("```")) {
            return "text/markdown";
        }
        return "text/plain";
    }

    private String convertMarkdownToHtml(String markdown) {
        // Enhanced Markdown to HTML conversion
        String html = "<html><body style='font-family: Arial; font-size: 14px; padding: 10px;'>";
        
        // Headers
        html = html.replaceAll("(?m)^# (.*?)$", "<h1>$1</h1>");
        html = html.replaceAll("(?m)^## (.*?)$", "<h2>$1</h2>");
        html = html.replaceAll("(?m)^### (.*?)$", "<h3>$1</h3>");
        html = html.replaceAll("(?m)^#### (.*?)$", "<h4>$1</h4>");
        
        // Bold and italic
        html = html.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        html = html.replaceAll("__(.*?)__", "<b>$1</b>");
        html = html.replaceAll("\\*(.*?)\\*", "<i>$1</i>");
        html = html.replaceAll("_(.*?)_", "<i>$1</i>");
        
        // Code blocks
        html = html.replaceAll("```(.*?)```", "<pre><code>$1</code></pre>");
        
        // Inline code
        html = html.replaceAll("`(.*?)`", "<code>$1</code>");
        
        // Lists
        html = html.replaceAll("(?m)^- (.*?)$", "<li>$1</li>");
        html = html.replaceAll("(?m)^\\* (.*?)$", "<li>$1</li>");
        html = html.replaceAll("(?m)^\\d+\\. (.*?)$", "<li>$1</li>");
        html = html.replaceAll("(?s)(<li>.*?</li>)", "<ul>$1</ul>");
        
        // Links
        html = html.replaceAll("\```math
(.*?)\```\KATEX_INLINE_OPEN(.*?)\KATEX_INLINE_CLOSE", "<a href='$2'>$1</a>");
        
        // Images
        html = html.replaceAll("!\```math
(.*?)\```\KATEX_INLINE_OPEN(.*?)\KATEX_INLINE_CLOSE", "<img src='$2' alt='$1' style='max-width: 100%;'>");
        
        // Paragraphs
        html = html.replaceAll("(?m)^(?!<[hlu])(.*?)$", "<p>$1</p>");
        
        // Line breaks
        html = html.replaceAll("\n", "<br>");
        
        return html + "</body></html>";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Editor().setVisible(true));
    }
}
