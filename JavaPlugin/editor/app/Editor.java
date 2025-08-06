package editor.app;

import editor.api.Plugin;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

public class Editor extends JFrame {
    private JTabbedPane tabbedPane;
    private JLabel statusLabel;

    public Editor() {
        setTitle("Plugin-Based Text Editor");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initGUI();
    }

    private void initGUI() {
        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // File menu (keep empty or you can add file menus here)
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        // Remove Plugins menu entirely (no longer added)

        // Tools Menu (with New, Save, and your tools)
        JMenu toolsMenu = new JMenu("Tools");
        menuBar.add(toolsMenu);
        addToolsToMenu(toolsMenu);

        // Toolbar (no New or Save button)
        JToolBar toolBar = new JToolBar();
        addToolbarButtons(toolBar);
        add(toolBar, BorderLayout.NORTH);

        // Tabbed Pane
        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        statusLabel = new JLabel(" Ready ");
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        add(statusBar, BorderLayout.SOUTH);

        // Start with one tab open
        addEditorTab(null);

        validate();
        repaint();
    }

    private void addToolsToMenu(JMenu toolsMenu) {
        JMenuItem newFileItem = new JMenuItem("New");
        newFileItem.addActionListener(e -> addEditorTab(null));
        toolsMenu.add(newFileItem);

        JMenuItem saveFileItem = new JMenuItem("Save");
        saveFileItem.addActionListener(e -> saveFileDialog());
        toolsMenu.add(saveFileItem);

        toolsMenu.addSeparator();

        JMenuItem clearTextItem = new JMenuItem("Clear All Text");
        clearTextItem.addActionListener(e -> {
            JTextArea current = getCurrentTextArea();
            if (current != null) {
                current.setText("");
            }
        });
        toolsMenu.add(clearTextItem);

        JMenuItem copyAllTextItem = new JMenuItem("Copy All Text");
        copyAllTextItem.addActionListener(e -> {
            JTextArea current = getCurrentTextArea();
            if (current != null) {
                current.selectAll();
                current.copy();
            }
        });
        toolsMenu.add(copyAllTextItem);

        JMenuItem lowerCaseItem = new JMenuItem("Convert to Lowercase");
        lowerCaseItem.addActionListener(e -> {
            JTextArea current = getCurrentTextArea();
            if (current != null) {
                String selected = current.getSelectedText();
                if (selected != null && !selected.isEmpty()) {
                    current.replaceSelection(selected.toLowerCase());
                } else {
                    current.setText(current.getText().toLowerCase());
                }
            }
        });
        toolsMenu.add(lowerCaseItem);

        JMenuItem spaceCountItem = new JMenuItem("Space Count");
        spaceCountItem.addActionListener(e -> {
            JTextArea current = getCurrentTextArea();
            if (current != null) {
                String text = current.getText();
                long spaces = text.chars().filter(ch -> ch == ' ').count();
                JOptionPane.showMessageDialog(this, "Space Count: " + spaces, "Space Count", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        toolsMenu.add(spaceCountItem);

        JMenuItem letterCountItem = new JMenuItem("Letter Count");
        letterCountItem.addActionListener(e -> {
            JTextArea current = getCurrentTextArea();
            if (current != null) {
                String text = current.getText();
                long letters = text.chars().filter(ch -> Character.isLetter(ch)).count();
                JOptionPane.showMessageDialog(this, "Letter Count: " + letters, "Letter Count", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        toolsMenu.add(letterCountItem);
    }

    private void addToolbarButtons(JToolBar toolBar) {
        // Only Undo and Redo buttons left on toolbar
        JButton undoBtn = new JButton(new AbstractAction("Undo") {
            public void actionPerformed(ActionEvent e) {
                JTextArea current = getCurrentTextArea();
                if (current != null) {
                    UndoManager undoManager = (UndoManager) current.getClientProperty("undoManager");
                    if (undoManager != null && undoManager.canUndo()) {
                        try {
                            undoManager.undo();
                        } catch (CannotUndoException ex) {
                            // ignore
                        }
                    }
                }
            }
        });
        toolBar.add(undoBtn);

        JButton redoBtn = new JButton(new AbstractAction("Redo") {
            public void actionPerformed(ActionEvent e) {
                JTextArea current = getCurrentTextArea();
                if (current != null) {
                    UndoManager undoManager = (UndoManager) current.getClientProperty("undoManager");
                    if (undoManager != null && undoManager.canRedo()) {
                        try {
                            undoManager.redo();
                        } catch (CannotRedoException ex) {
                            // ignore
                        }
                    }
                }
            }
        });
        toolBar.add(redoBtn);
    }

    private void addEditorTab(File file) {
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Fira Code", Font.PLAIN, 14));
        textArea.setBackground(new Color(0x16, 0x1b, 0x22));
        textArea.setForeground(new Color(0xc9, 0xd1, 0xd9));
        textArea.setCaretColor(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(textArea);

        UndoManager undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(undoManager);

        textArea.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                int pos = e.getDot();
                try {
                    int line = textArea.getLineOfOffset(pos) + 1;
                    int col = pos - textArea.getLineStartOffset(line - 1) + 1;
                    statusLabel.setText(" Ln: " + line + "  Col: " + col + " ");
                } catch (Exception ex) {
                    statusLabel.setText(" Ready ");
                }
            }
        });

        String title = (file != null) ? file.getName() : "Untitled";
        tabbedPane.addTab(title, scrollPane);
        tabbedPane.setSelectedComponent(scrollPane);

        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                textArea.read(reader, null);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading file: " + file.getName(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        textArea.putClientProperty("undoManager", undoManager);
    }

    private JTextArea getCurrentTextArea() {
        Component comp = tabbedPane.getSelectedComponent();
        if (comp instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) comp;
            JViewport viewport = scrollPane.getViewport();
            Component view = viewport.getView();
            if (view instanceof JTextArea) {
                return (JTextArea) view;
            }
        }
        return null;
    }

    private void openFileDialog() {
        JFileChooser fileChooser = new JFileChooser();
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            addEditorTab(file);
        }
    }

    private void saveFileDialog() {
        JTextArea current = getCurrentTextArea();
        if (current == null)
            return;

        JFileChooser fileChooser = new JFileChooser();
        int ret = fileChooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                current.write(writer);
                tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), file.getName());
                JOptionPane.showMessageDialog(this, "File saved successfully", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        try {
            // Use system look and feel for native appearance
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) {
        }

        SwingUtilities.invokeLater(() -> {
            Editor editor = new Editor();
            editor.setVisible(true);
        });
    }
}
