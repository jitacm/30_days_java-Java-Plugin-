Java Plugin-Based Text Editor
--------------------
A simple, lightweight text editor built with Java Swing that features a dynamic plugin architecture. The core application provides basic text editing functionalities, while additional features can be seamlessly added by dropping new .jar files into a dedicated plugins directory.

This project is an excellent demonstration of object-oriented design principles, focusing on interfaces, abstraction, and dynamic class loading to create an extensible and modular application.

Features
-----------------
Core Editor: A clean, modern-looking text area with basic file operations.

New File

Open File

Save File

Dynamic Plugin System: The editor automatically detects and loads plugins from a plugins directory at startup.

Plugin Menu: Loaded plugins are automatically added to a "Plugins" menu in the menu bar for easy access.

Extensible: Easily create your own plugins by implementing a simple Plugin interface.

Sample Plugins Included:
---------------

Word Count: A simple plugin to count the total words in the current document.

To Uppercase: A utility plugin to convert the currently selected text to uppercase.

Getting Started
To get the editor running on your local machine, you will need to have a Java Development Kit (JDK) version 11 or higher installed.

1. Clone the Repository
First, clone this repository to your local machine.

git clone <your-repository-url>
cd <repository-directory>

2. Compile the Project
The project is structured into multiple modules. Follow these steps to compile everything in the correct order.

A. Compile the Plugin API
----------

The plugins and the main app both depend on the Plugin interface, so compile it first.

javac editor/api/Plugin.java

B. Compile the Plugins
-------------

Now, compile each plugin. The -cp . flag tells the compiler to look for the editor/api/Plugin.class file in the current directory structure.

javac -cp . plugins/wordcount/WordCountPlugin.java
javac -cp . plugins/touppercase/ToUppercasePlugin.java

C. Create Plugin JARs
---------------

Package the compiled plugin classes into .jar files. The editor will load these JARs.

jar -cf plugins/wordcount.jar -C plugins/wordcount .
jar -cf plugins/touppercase.jar -C plugins/touppercase .

D. Compile the Main Editor Application
-----------------

Finally, compile the main application.

javac -cp . editor/app/Editor.java

3. Run the Editor
------------------
Launch the application from the root directory. It will automatically find and load the .jar files in the plugins directory.

java -cp . editor.app.Editor

How to Create Your Own Plugin
Creating a new plugin is simple.

Create a New Folder: Create a new folder for your plugin inside the plugins directory (e.g., plugins/myplugin).

Implement the Plugin Interface: Create a new .java file in your folder. Your class must implement the editor.api.Plugin interface.

// in plugins/myplugin/MyCoolPlugin.java
package plugins.myplugin;

import editor.api.Plugin;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;

public class MyCoolPlugin implements Plugin {
    @Override
    public String getName() {
        // This name will appear in the "Plugins" menu
        return "My Cool Feature";
    }

    @Override
    public void execute(JTextArea textArea) {
        // Add your plugin's logic here
        String text = textArea.getText();
        JOptionPane.showMessageDialog(null, "My plugin is running! The text has " + text.length() + " characters.");
    }
}

Compile and JAR Your Plugin: Compile your new class and package it into a JAR file, just like the other plugins.

# Compile
javac -cp . plugins/myplugin/MyCoolPlugin.java

# Create JAR
jar -cf plugins/myplugin.jar -C plugins/myplugin .

Run the Editor: Relaunch the editor, and your new plugin will appear in the "Plugins" menu!
