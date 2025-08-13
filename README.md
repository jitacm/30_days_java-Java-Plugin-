# Modern Plugin-Based Text Editor

## 📖 Project Overview

This project is a sophisticated, lightweight text editor built with Java Swing. Its primary feature is a dynamic plugin architecture, which allows for seamless extension of its core functionality without modifying the main application's code. By leveraging interfaces and Java's dynamic class loading capabilities, the editor becomes a highly modular and extensible platform. The application provides a modern, dark-themed user interface, complete with a real-time status bar and essential file operations.

This serves as an excellent demonstration of object-oriented design principles, showcasing how a well-defined API can enable a robust, modular, and easily expandable software system.

---

A **powerful, extensible, and modern** text editor built with Java Swing, featuring:

- A **dark-themed** professional UI
- A **dynamic plugin architecture** for limitless extensibility
- Built-in **spell check**, **Undo/Redo**, and **Plugin Manager UI**
- Multiple **sample plugins** ready to use
- Easy **plugin development workflow**

This project demonstrates advanced **Java OOP design principles**, including interfaces, abstraction, dynamic class loading, and modular UI development.

---

## 🚀 Features

### 🖌 Modern Core Editor

- **Dark Theme UI**: Stylish, minimal, and easy on the eyes
- **Live Status Bar**: Tracks lines, words, and characters in real-time
- **Quick Access Toolbar**: Undo, Redo, New File, Open, Save — right at your fingertips
- **Find Functionality**: Search and highlight terms in your document
- **Undo & Redo Support**: With shortcuts <kbd>Ctrl+Z</kbd> and <kbd>Ctrl+Y</kbd>
- **Spell Check Plugin**: Highlights misspelled words instantly

### ⚙️ Dynamic Plugin System

- **Auto-Detection**: Loads `.jar` plugins from the `plugins` folder automatically
- **Plugin Menu**: Dynamically lists all loaded plugins
- **Safe Execution**: Runs plugins in isolation with error handling
- **Plugin Manager UI**: View, enable, or disable installed plugins

### 🧩 Included Sample Plugins

1. **Word Count Plugin** — Counts the words in your document
2. **To Uppercase Plugin** — Converts selected text to uppercase
3. **Spell Check Plugin** — Highlights misspelled words in the text

---

## 📂 Project Structure

```
JavaPlugin/
├── editor/
│   ├── api/
│   │   └── Plugin.java
│   ├── app/
│   │   └── Editor.java
├── plugins/
│   ├── wordcount/
│   │   └── WordCountPlugin.java
│   ├── touppercase/
│   │   └── ToUppercasePlugin.java
│   ├── spellcheck/
│   │   └── SpellCheckPlugin.java
└── README.md
```

---

## 🛠 Installation & Running

### 1️⃣ Prerequisites

- **Java JDK 11+**
- Command-line or IDE (IntelliJ/Eclipse/NetBeans)

### 2️⃣ Clone the Repository

```bash
git clone <your-repo-url>
cd JavaPlugin
```

### 3️⃣ Compile the Plugin API

```bash
javac editor/api/Plugin.java
```

### 4️⃣ Compile and Package Plugins

```bash
# Compile
javac -cp . plugins/wordcount/WordCountPlugin.java
javac -cp . plugins/touppercase/ToUppercasePlugin.java
javac -cp . plugins/spellcheck/SpellCheckPlugin.java

# Package into JARs
jar -cf plugins/wordcount.jar -C . plugins/wordcount
jar -cf plugins/touppercase.jar -C . plugins/touppercase
jar -cf plugins/spellcheck.jar -C . plugins/spellcheck
```

### 5️⃣ Compile the Main Application

```bash
javac -cp . editor/app/Editor.java
```

### 6️⃣ Run the Editor

```bash
java -cp . editor.app.Editor
```

---

## 🎨 Usage

**Toolbar Buttons:**

- 🆕 **New File** — Clears the editor
- 📂 **Open File** — Opens a .txt file
- 💾 **Save File** — Saves the current file
- ↩ **Undo** — Reverts last change (<kbd>Ctrl+Z</kbd>)
- ↪ **Redo** — Restores reverted change (<kbd>Ctrl+Y</kbd>)

**Plugins:**

- Access via **Plugins** menu
- Click a plugin to run it on the current text
- Manage plugins from **Plugin Manager UI**

---

## 🧑‍💻 Creating Your Own Plugin

**Create a New Plugin Class**

```java
package plugins.myplugin;

import editor.api.Plugin;
import javax.swing.*;

public class MyPlugin implements Plugin {
    @Override
    public String getName() {
        return "My Awesome Plugin";
    }

    @Override
    public void execute(JTextArea textArea) {
        JOptionPane.showMessageDialog(null, "Plugin executed!");
    }
}
```

**Compile & Package**

```bash
javac -cp . plugins/myplugin/MyPlugin.java
jar -cf plugins/myplugin.jar -C . plugins/myplugin
```

**Run the Editor**

Your plugin will now appear in the Plugins menu automatically.

---

## 📌 Upcoming Features (Proposal)

- Multiple theme support (Light/Dark/Custom)
- Real-time collaborative editing
- Syntax highlighting for code
- Plugin marketplace UI

---

## 📄 License

This project is licensed under the MIT License.

---

## 🤝 Contributing

We welcome contributions!

- Fork the repository
- Create a feature branch
- Submit a Pull Request describing your changes
- 
