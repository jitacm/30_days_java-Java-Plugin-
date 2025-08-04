package editor.app;

import editor.api.Plugin;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

public class Editor extends JFrame {
    private JTextArea textArea;
    private JFileChooser fileChooser;
    private List<Plugin> loadedPlugins = new ArrayList<>();

    public Editor() {
        setTitle("Plugin-Based Text Editor");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        textArea = new JTextArea();
        textArea.setFont(new Font("Fira Code", Font.PLAIN, 14));
        textArea.setBackground(new Color(0x16, 0x1b, 0x22));
        textArea.setForeground(new Color(0xc9, 0xd1, 0xd9));
        textArea.setCaretColor(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        fileChooser = new JFileChooser();
        setupMenuBar();
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(e -> openFile());
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> saveFile());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // Plugins Menu
        JMenu pluginsMenu = new JMenu("Plugins");
        loadPlugins(pluginsMenu);
        menuBar.add(pluginsMenu);

        setJMenuBar(menuBar);
    }

    private void loadPlugins(JMenu pluginsMenu) {
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
                URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl});
                
                try (JarFile jarFile = new JarFile(file)) {
                    jarFile.stream()
                        .filter(entry -> entry.getName().endsWith(".class"))
                        .map(entry -> entry.getName().replace("/", ".").replaceAll(".class$", ""))
                        .forEach(className -> {
                            try {
                                Class<?> cls = classLoader.loadClass(className);
                                if (Plugin.class.isAssignableFrom(cls) && !cls.isInterface()) {
                                    Plugin plugin = (Plugin) cls.getDeclaredConstructor().newInstance();
                                    loadedPlugins.add(plugin);
                                    JMenuItem pluginItem = new JMenuItem(plugin.getName());
                                    pluginItem.addActionListener(e -> plugin.execute(textArea));
                                    pluginsMenu.add(pluginItem);
                                    System.out.println("Loaded plugin: " + plugin.getName());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                textArea.read(reader, null);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error opening file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveFile() {
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                textArea.write(writer);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Editor().setVisible(true));
    }
}


