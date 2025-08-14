# Modern Plugin-Based Text Editor (Java Swing)

A lightweight, modular text editor built with Java Swing featuring a dynamic plugin architecture. The core editor is intentionally minimal and safe, while plugins extend its functionality at runtime by loading standalone JARs from the plugins folder. This repository demonstrates clean OOP design, dynamic class loading, and a practical approach to extending a GUI application without touching the core codebase.

---

## Table of Contents

- [Overview & Highlights](#overview--highlights)
- [Project Structure](#project-structure)
- [Key Features](#key-features)
- [How the Plugin System Works](#how-the-plugin-system-works)
- [Getting Started (Manual Build)](#getting-started-manual-build)
  - [Prerequisites](#prerequisites)
  - [Clone & Inspect](#clone--inspect)
  - [Build Instructions](#build-instructions)
  - [Run the Editor](#run-the-editor)
  - [Managing Plugins](#managing-plugins)
- [Developing Plugins](#developing-plugins)
  - [Create a New Plugin](#create-a-new-plugin)
  - [Sample Plugin Template](#sample-plugin-template)
  - [Packaging Plugins](#packaging-plugins)
- [Project Architecture Details](#project-architecture-details)
  - [API](#api)
  - [Editor Core](#editor-core)
  - [Plugin Manager UI](#plugin-manager-ui)
- [Usage & UX Walkthrough](#usage--ux-walkthrough)
- [Contributing](#contributing)
- [License](#license)

---

## Overview & Highlights

- A modern, dark-themed text editor built with Java Swing.
- Dynamic plugin system: load, enable/disable, and manage plugins at runtime.
- Real-time status bar (lines, words, characters).
- Built-in Undo/Redo support.
- A lightweight, extensible plugin API (Java interface) that plugins implement.
- Included sample plugins for word counting, text transformation, spell checking, statistics, and more.
- Plugins are distributed as JARs in the plugins folder and discovered automatically on startup.

---

## Project Structure

The repository is organized as follows:

```
jitacm-30_days_java-java-plugin-/
├── README.md
└── JavaPlugin/
    ├── editor/
    │   ├── api/
    │   │   └── Plugin.java
    │   └── app/
    │       └── Editor.java
    └── plugins/
        ├── codeformatter/
        │   └── CodeFormatterPlugin.java
        ├── spellcheck/
        │   └── SpellCheckPlugin.java
        ├── textstats/
        │   └── TextStatsPlugin.java
        ├── theme/
        │   └── ThemePlugin.java
        ├── touppercase/
        │   └── ToUppercasePlugin.java
        └── wordcount/
            └── WordCountPlugin.java
```

- Core API: `JavaPlugin/editor/api/Plugin.java`
- Editor App: `JavaPlugin/editor/app/Editor.java`
- Sample Plugins:
  - Word Count: `JavaPlugin/plugins/wordcount/WordCountPlugin.java`
  - To Uppercase: `JavaPlugin/plugins/touppercase/ToUppercasePlugin.java`
  - Spell Check: `JavaPlugin/plugins/spellcheck/SpellCheckPlugin.java`
  - Code Formatter: `JavaPlugin/plugins/codeformatter/CodeFormatterPlugin.java`
  - Text Stats: `JavaPlugin/plugins/textstats/TextStatsPlugin.java`
  - Theme: `JavaPlugin/plugins/theme/ThemePlugin.java`

---

## Key Features

- Dark theme UI with comfortable contrast.
- Live status bar showing lines, words, and characters.
- Undo/Redo with keyboard shortcuts (Ctrl+Z / Ctrl+Y).
- Plugin system:
  - Auto-detect and load `.jar` plugins from the plugins directory.
  - Dynamic menu listing loaded plugins.
  - Safe execution with per-plugin enable/disable controls.
  - Plugin Manager UI to enable/disable plugins at runtime.
- Included plugins:
  - Word Count
  - Convert Selection to Uppercase
  - Spell Check
  - Text Statistics
  - Theme & Accessibility
  - Code Formatter (multi-language awareness)

---

## How the Plugin System Works

- The editor looks into the plugins directory for JAR files.
- Each JAR is scanned for classes that implement the Plugin interface (editor.api.Plugin).
- For each valid plugin class, an instance is created and registered with the UI.
- The Plugins menu shows the name returned by getName().
- Clicking a plugin executes its execute(JTextArea) method on the current document.
- Plugins can be enabled/disabled via the Plugin Manager UI. Disabled plugins do not execute.

Notes:
- Plugins are loaded in isolation via a URLClassLoader pointing to the plugin JAR.
- The system uses reflection to instantiate plugin classes and to invoke their methods safely within the editor’s UI flow.

---

## Getting Started (Manual Build)

This project demonstrates a straightforward, no-ORM, no-build-tool approach using the JDK command line. It’s intentionally lightweight to illustrate dynamic class loading in Java.

### Prerequisites

- Java JDK 11+ (tested with Oracle/OpenJDK).
- A command-line environment (bash, zsh, PowerShell, etc.).

### Clone & Inspect

- Clone the repository (or download the folder contents).
- Inspect the directory structure shown above to understand the plugin layout.

### Build Instructions

The following commands illustrate a minimal, incremental build process. Run them from the repository root.

1) Compile the Plugin API (interface)

```
javac editor/api/Plugin.java
```

2) Compile the Editor (core app)

```
javac -cp . editor/app/Editor.java
```

3) Compile Sample Plugins and Package Them as JARs

For each plugin, compile and jar. The Editor expects plugins in the jars inside the plugins directory, with a structure matching their package declarations.

- Word Count

```
javac -cp . plugins/wordcount/WordCountPlugin.java
jar -cf plugins/wordcount.jar -C . plugins/wordcount
```

- To Uppercase

```
javac -cp . plugins/touppercase/ToUppercasePlugin.java
jar -cf plugins/touppercase.jar -C . plugins/touppercase
```

- Spell Check

```
javac -cp . plugins/spellcheck/SpellCheckPlugin.java
jar -cf plugins/spellcheck.jar -C . plugins/spellcheck
```

- Code Formatter

```
javac -cp . plugins/codeformatter/CodeFormatterPlugin.java
jar -cf plugins/codeformatter.jar -C . plugins/codeformatter
```

- Text Statistics

```
javac -cp . plugins/textstats/TextStatsPlugin.java
jar -cf plugins/textstats.jar -C . plugins/textstats
```

- Theme & Accessibility

```
javac -cp . plugins/theme/ThemePlugin.java
jar -cf plugins/theme.jar -C . plugins/theme
```

> Important: Each plugin declares its package (e.g., `package plugins.wordcount;`). The corresponding jar should contain the class file at the path matching the package. The Editor loads classes by their fully-qualified names derived from the jar’s internal path, so ensure the packaging respects the declared package.

4) Compile the Main Application (optional if you want to run directly from class files)

```
javac -cp . editor/app/Editor.java
```

5) Run the Editor

```
java -cp . editor.app.Editor
```

- The Editor will automatically scan the plugins directory, load plugins from the jars, and populate the Plugins menu.

Tips:
- If you add new plugins or jar files, use the “Reload Plugins” option from the Plugins menu to refresh the list without restarting the editor.
- Plugins are executed in the same JVM as the editor. While they run in isolation within a single process, ensure plugin code is defensive to avoid crashing the editor.

---

## Managing Plugins

- On startup, the editor scans the plugins directory for jars ending in .jar.
- Each loaded plugin is shown in the Plugins menu by name (as returned by getName()).
- The Plugins menu also includes:
  - Manage Plugins… — Opens a UI to enable/disable plugins.
  - Reload Plugins — Re-scan the plugins directory and refresh the menu.

Plugin Manager UI:
- Shows a list of available plugins with:
  - Name
  - Enable/Disable toggle
  - Status (Enabled/Disabled)
- Toggling a plugin updates its state in memory. You can enable a plugin later by reloading or re-opening the manager.

---

## Developing Plugins

The plugin API is intentionally lightweight to encourage experimentation and rapid iteration.

### Create a New Plugin

1) Create a new Java class implementing the Plugin interface:

```java
package plugins.myplugin;

import editor.api.Plugin;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class MyAwesomePlugin implements Plugin {
    @Override
    public String getName() {
        return "My Awesome Plugin";
    }

    @Override
    public void execute(JTextArea textArea) {
        JOptionPane.showMessageDialog(null, "Plugin executed!");
        // Example: insert text at the caret
        int pos = textArea.getCaretPosition();
        textArea.insert("Hello from My Awesome Plugin!", pos);
    }
}
```

2) Compile and Package into a JAR (as shown in the Build Instructions above). Place the resulting jar in the plugins directory.

3) Run the Editor and verify that your plugin appears in the Plugins menu and executes on the current document.

### Sample Plugin Template

The repository already contains a few sample plugins that illustrate common tasks:
- WordCountPlugin (counts words)
- ToUppercasePlugin (converts selected text to uppercase)
- SpellCheckPlugin (basic dictionary-based spell check)
- CodeFormatterPlugin (language-aware formatting)
- TextStatsPlugin (sentence count and average word length)
- ThemePlugin (theme and font adjustments)

You can copy, modify, and repackage any of these examples as a starting point.

### Packaging Plugins

- Build your plugin with its package structure preserved.
- Create a jar containing the compiled class files in your plugin’s package tree, e.g.:

```
jar -cf plugins/myplugin.jar -C . plugins/myplugin
```

- Place your jar in the plugins folder. The editor will load it on next startup or when you press Reload Plugins.

---

## Project Architecture Details

### API (Plugin Interface)

- Location: JavaPlugin/editor/api/Plugin.java
- Purpose: A minimal contract used by all plugins.
- Methods:
  - String getName(): Returns the user-facing plugin name.
  - void execute(JTextArea textArea): Executes the plugin’s functionality on the provided text area.

### Editor Core

- Location: JavaPlugin/editor/app/Editor.java
- Responsibilities:
  - UI: JFrame with a dark-themed text area, status bar, toolbar, and menus.
  - Editor features: New/Open/Save, Find, Undo/Redo, and a dynamic Plugins menu.
  - Plugin loading: Scans the plugins directory, loads classes implementing Plugin, instantiates them, and wires UI actions.
  - Plugin management: UI for enabling/disabling plugins; dynamic reloading.

Key Components:
- UndoManager: Provides robust Undo/Redo for text edits.
- JTextArea: Central editing component with custom font and colors for a dark UI.
- Plugins menu: Populated at runtime with all loaded plugins.
- Plugin Manager: Dialog-based UI to enable/disable plugins.

### Plugin Manager UI

- Displays each loaded plugin with:
  - Name
  - Enable/Disable button
  - Status label (Enabled/Disabled)
- Allows toggling plugin state in-app (without restart).

---

## Usage & UX Walkthrough

- Start the editor. You’ll see a dark-themed UI with a status bar at the bottom showing Lines, Words, and Chars.
- Use the toolbar for New/Open/Save, and Undo/Redo.
- Open a text document and select some text.
- Navigate to Plugins -> [Your Plugins] to execute a plugin on the current text.
- Open Plugins -> Manage Plugins… to enable or disable any loaded plugin.
- If you install new plugin jars, click Reload Plugins to refresh the menu.

Common plugin examples included:
- Word Count: Shows the number of words in the document.
- Convert Selection to Uppercase: Converts the selected text to uppercase.
- Spell Check: Highlights potential misspellings using a small dictionary.
- Text Statistics: Shows sentence count and average word length.
- Theme & Accessibility: Changes theme and font size for better readability.
- Code Formatter: Attempts basic language-aware formatting (Java, Python, C++).

---

## Contributing

- This project is designed to be accessible for contributors.
- If you’d like to add plugins, follow the “Developing Plugins” section above.
- Please submit issues and pull requests with a clear description of changes, testing steps, and potential impact on the plugin system.

---

## License

MIT License. See LICENSE (or the project root) for full text.

---

        └── wordcount/
            └── WordCountPlugin.java
```
