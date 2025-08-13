# Modern Plugin-Based Text Editor

## 📖 Project Overview

This project is a sophisticated, lightweight text editor built with Java Swing. Its primary feature is a dynamic plugin architecture, which allows for seamless extension of its core functionality without modifying the main application's code. By leveraging interfaces and Java's dynamic class loading capabilities, the editor becomes a highly modular and extensible platform. The application provides a modern, dark-themed user interface, complete with a real-time status bar and essential file operations.

This serves as an excellent demonstration of object-oriented design principles, showcasing how a well-defined API can enable a robust, modular, and easily expandable software system.

---

## 🌟 Features

### Core Editor
-   **Modern Dark Theme UI**: A clean, professional-looking interface designed with a dark color palette for enhanced readability and reduced eye strain.
-   **Real-time Status Bar**: A dynamically updating status bar at the bottom of the window provides live statistics on the current document, including:
    -   Line count
    -   Word count
    -   Character count
-   **Enhanced Text Editing**: The text area uses a modern, monospace font (Consolas or similar) for code-friendly display, with proper line wrapping and tab support.
-   **File Operations**: The "File" menu includes standard functionalities:
    -   `Open`: Load a file from your system.
    -   `Save`: Save the current content to a file.
    -   `Find`: A robust search feature that highlights the first occurrence of the search term in the document.

### Dynamic Plugin System
-   **Auto-Detection**: At startup, the editor automatically scans the dedicated `plugins` directory for compiled `.jar` files.
-   **Plugin Menu**: Any valid plugin found is instantiated and added as a menu item in the "Plugins" menu.
-   **Extensible Architecture**: The system is built around a simple `Plugin` interface, making it easy for developers to create and distribute new functionalities without touching the core application code.
-   **Robust Error Handling**: The application gracefully handles corrupted or invalid plugin files, logging errors to the console and preventing crashes.

### Sample Plugins Included
1.  **Word Count**: A utility to count the total words in the entire document and display the result in a dialog.
2.  **To Uppercase**: A text-transformation plugin that converts the currently selected text to all uppercase characters.

---

## 🚀 Getting Started

### Prerequisites
-   Java Development Kit (JDK) version 11 or higher.

### Installation

1.  **Clone the Repository**
    First, clone this repository to your local machine using Git.

    ```bash
    git clone <your-repository-url>
    cd <repository-directory>
    ```

2.  **Compile the Project**
    The project is structured into multiple modules. Follow these steps to compile everything in the correct order.

    -   **A. Compile the Plugin API**
        The `Plugin.java` interface must be compiled first as it is a dependency for both the editor and the plugins.
        ```bash
        javac JavaPlugin\editor\api\Plugin.java
        ```

    -   **B. Compile the Plugins**
        Compile each plugin, specifying the `JavaPlugin` directory in the classpath (`-cp`) so the compiler can find the `Plugin.class` file.
        ```bash
        javac -cp JavaPlugin JavaPlugin\plugins\touppercase\ToUppercasePlugin.java
        javac -cp JavaPlugin JavaPlugin\plugins\wordcount\WordCountPlugin.java
        ```

    -   **C. Create Plugin JARs**
        These commands package the compiled classes into JAR files with the correct package structure. The `-C .` flag is crucial as it ensures the package hierarchy (e.g., `plugins/touppercase/ToUppercasePlugin.class`) is preserved inside the JAR.
        ```bash
        cd JavaPlugin
        jar -cf plugins\touppercase.jar -C . plugins\touppercase
        jar -cf plugins\wordcount.jar -C . plugins\wordcount
        ```

    -   **D. Compile the Main Editor Application**
        Finally, compile the main `Editor.java` application.
        ```bash
        javac -cp . editor\app\Editor.java
        ```

3.  **Run the Editor**
    Launch the application from the `JavaPlugin` directory. The editor will automatically find and load the `.jar` files in the `plugins` directory at startup.
    ```bash
    java -cp . editor.app.Editor
    ```

---

## 🛠 How to Create Your Own Plugin

Creating a new plugin is a straightforward process:

1.  **Create a New Folder**: Inside the `plugins` directory, create a new folder for your plugin (e.g., `plugins/myplugin`). This directory will correspond to your plugin's package name.

2.  **Implement the Plugin Interface**: Create a new `.java` file in your folder. Your class **must** implement the `editor.api.Plugin` interface, which defines two methods: `getName()` and `execute(JTextArea textArea)`.

    ```java
    // in plugins/myplugin/MyCoolPlugin.java
    package plugins.myplugin;

    import editor.api.Plugin;
    import javax.swing.JTextArea;
    import javax.swing.JOptionPane;

    public class MyCoolPlugin implements Plugin {
        @Override
        public String getName() {
            // This name will appear as the menu item text.
            return "My Cool Feature";
        }

        @Override
        public void execute(JTextArea textArea) {
            // Add your plugin's logic here. The JTextArea object gives you access
            // to the editor's content.
            String text = textArea.getText();
            JOptionPane.showMessageDialog(null, "My plugin is running! The text has " + text.length() + " characters.");
        }
    }
    ```

3.  **Compile and JAR Your Plugin**:
    ```bash
    # From the project root, compile your plugin
    javac -cp JavaPlugin JavaPlugin\plugins\myplugin\MyCoolPlugin.java

    # Navigate to the JavaPlugin directory and create the JAR
    cd JavaPlugin
    jar -cf plugins\myplugin.jar -C . plugins\myplugin
    ```

4.  **Run the Editor**: Relaunch the editor. Your new plugin will automatically be detected and appear in the "Plugins" menu!

## 📄 License

This project is open source and available under the MIT License.
