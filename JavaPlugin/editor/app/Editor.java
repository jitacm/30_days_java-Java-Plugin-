package editor.app;

import editor.api.Plugin;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Editor extends JFrame {

    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final JLabel statusBar = new JLabel(" Lines: 0 | Words: 0 | Chars: 0 ");
    private final JFileChooser fileChooser = new JFileChooser();
    private final JMenu pluginsMenu = new JMenu("Plugins");
    private final Map<JTextArea, UndoManager> undoManagers = new HashMap<>();

    public Editor() {
        setTitle("Modern Plugin Text Editor");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLookAndFeel();
        setLayout(new BorderLayout());

        // Tabbed Editor Area
        add(tabbedPane, BorderLayout.CENTER);

        // Status Bar
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusBar.setOpaque(true);
        statusBar.setBackground(new Color(50, 50, 50));
        statusBar.setForeground(Color.WHITE);
        add(statusBar, BorderLayout.SOUTH);

        setupMenuBar();
        loadPlugins();
        createNewTab();
    }

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(createMenuItem("New", e -> createNewTab(), KeyEvent.VK_N));
        fileMenu.add(createMenuItem("Open", e -> openFile(), KeyEvent.VK_O));
        fileMenu.add(createMenuItem("Save", e -> saveFile(), KeyEvent.VK_S));
        fileMenu.add(createMenuItem("Find", e -> findText(), KeyEvent.VK_F));
        fileMenu.addSeparator();
        fileMenu.add(createMenuItem("Exit", e -> System.exit(0), KeyEvent.VK_Q));
        menuBar.add(fileMenu);

        // Edit Menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.add(createMenuItem("Undo", e -> undo(), KeyEvent.VK_Z));
        editMenu.add(createMenuItem("Redo", e -> redo(), KeyEvent.VK_Y));
        menuBar.add(editMenu);

        // Plugins Menu
        menuBar.add(pluginsMenu);

        setJMenuBar(menuBar);
    }

    private JMenuItem createMenuItem(String title, ActionListener action, int keyEvent) {
        JMenuItem item = new JMenuItem(title);
        item.addActionListener(action);
        item.setAccelerator(KeyStroke.getKeyStroke(keyEvent, InputEvent.CTRL_DOWN_MASK));
        return item;
    }



    private JButton createToolButton(String title, ActionListener action) {
        JButton button = new JButton(title);
        button.addActionListener(action);
        return button;
    }

    private void createNewTab() {
        JTextArea textArea = createTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        tabbedPane.addTab("Untitled", scrollPane);
        tabbedPane.setSelectedComponent(scrollPane);
        updateStatus(textArea);
    }

    private JTextArea createTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        textArea.setBackground(new Color(40, 40, 40));
        textArea.setForeground(Color.WHITE);
        textArea.setCaretColor(Color.WHITE);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        UndoManager undoManager = new UndoManager();
        undoManagers.put(textArea, undoManager);

        textArea.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateStatus(textArea); }
            public void removeUpdate(DocumentEvent e) { updateStatus(textArea); }
            public void changedUpdate(DocumentEvent e) { updateStatus(textArea); }
        });

        return textArea;
    }

    private JTextArea getCurrentTextArea() {
        JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
        if (scrollPane == null) return null;
        Component view = scrollPane.getViewport().getView();
        if (view instanceof JTextArea) {
            return (JTextArea) view;
        }
        return null;
    }

    private void updateStatus(JTextArea area) {
        SwingUtilities.invokeLater(() -> {
            String text = area.getText();
            int lines = area.getLineCount();
            int words = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
            int chars = text.length();
            statusBar.setText(" Lines: " + lines + " | Words: " + words + " | Chars: " + chars + " ");
        });
    }

    private void openFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                JTextArea textArea = createTextArea();
                textArea.read(reader, null);
                JScrollPane scrollPane = new JScrollPane(textArea);
                tabbedPane.addTab(file.getName(), scrollPane);
                tabbedPane.setSelectedComponent(scrollPane);
                updateStatus(textArea);
            } catch (IOException e) {
                showError("Failed to open file: " + e.getMessage());
            }
        }
    }

    private void saveFile() {
        JTextArea textArea = getCurrentTextArea();
        if (textArea == null) return;

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                textArea.write(writer);
                tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), file.getName());
            } catch (IOException e) {
                showError("Failed to save file: " + e.getMessage());
            }
        }
    }

    private void findText() {
        JTextArea textArea = getCurrentTextArea();
        if (textArea == null) return;

        String toFind = JOptionPane.showInputDialog(this, "Enter text to find:");
        if (toFind != null && !toFind.isEmpty()) {
            String content = textArea.getText();
            int index = content.indexOf(toFind);
            if (index >= 0) {
                textArea.setCaretPosition(index);
                textArea.select(index, index + toFind.length());
            } else {
                JOptionPane.showMessageDialog(this, "Text not found.");
            }
        }
    }

    private void undo() {
        JTextArea textArea = getCurrentTextArea();
        if (textArea != null) {
            UndoManager manager = undoManagers.get(textArea);
            if (manager != null && manager.canUndo()) manager.undo();
        }
    }

    private void redo() {
        JTextArea textArea = getCurrentTextArea();
        if (textArea != null) {
            UndoManager manager = undoManagers.get(textArea);
            if (manager != null && manager.canRedo()) manager.redo();
        }
    }

    private void loadPlugins() {
        File pluginFolder = new File("plugins");
        if (!pluginFolder.exists() || !pluginFolder.isDirectory()) return;

        File[] jars = pluginFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null) return;

        for (File jar : jars) {
            try {
                URLClassLoader loader = new URLClassLoader(new URL[]{ jar.toURI().toURL() }, getClass().getClassLoader());
                JarFile jarFile = new JarFile(jar);
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        String className = entry.getName().replace("/", ".").replace(".class", "");
                        Class<?> cls = loader.loadClass(className);
                        if (Plugin.class.isAssignableFrom(cls)) {
                            Plugin plugin = (Plugin) cls.getDeclaredConstructor().newInstance();
                            JMenuItem item = new JMenuItem(plugin.getName());
                            item.addActionListener(e -> {
                                JTextArea area = getCurrentTextArea();
                                if (area != null) plugin.execute(area);
                            });
                            pluginsMenu.add(item);
                        }
                    }
                }
                jarFile.close();
            } catch (Exception e) {
                System.err.println("Plugin load error in: " + jar.getName() + " — " + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Editor().setVisible(true));
    }
}
