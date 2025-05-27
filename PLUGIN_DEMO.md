# Scripts Launcher Plugin - Demo

## Overview
The Scripts Launcher plugin for IntelliJ IDEA allows you to configure and execute custom scripts and applications directly from your IDE. All scripts are global and shared across all projects.

## Features

### 1. Global Script Management
- **Add Scripts**: Create new global script configurations with:
  - Name and description
  - Command to execute
  - Working directory
  - Enable/disable toggle

### 2. Script Execution
- **Double-click** any enabled script to execute it
- **Right-click** context menu for additional options
- **Toolbar buttons** for quick actions

### 3. Tool Window
- Access via "Scripts Launcher" tool window (right sidebar)
- List view of all configured global scripts
- Add/Edit/Remove/Execute actions

## Usage Examples

### Example 1: Simple Command
- **Name**: "Hello World"
- **Command**: `echo "Hello from Scripts Launcher!"`
- **Description**: "Test script to verify functionality"

### Example 2: Open Application
- **Name**: "Open Calculator"
- **Command**: `gnome-calculator` (Linux) or `calc.exe` (Windows)
- **Description**: "Launch system calculator"

### Example 3: Development Script
- **Name**: "Build Project"
- **Command**: `./gradlew build`
- **Working Directory**: `/path/to/your/project`
- **Description**: "Build the current project"

### Example 4: Custom Executable
- **Name**: "My App"
- **Command**: `/path/to/my/executable.exe`
- **Working Directory**: `/path/to/app/directory`
- **Description**: "Launch my custom application"

## Installation
1. Build the plugin: `./gradlew buildPlugin -x instrumentCode` 
   - (For GitHub Codespaces, skip instrumentCode due to Java path issues)
   - Alternative: `./gradlew buildPluginSafe` (custom task for safe building)
2. Install the generated ZIP from `build/distributions/scripts-launcher-0.0.1.zip`
3. Restart IntelliJ IDEA
4. Find "Scripts Launcher" in the right tool window bar

## Configuration Storage
Script configurations are stored globally in the IDE's application settings, not per-project. This means your scripts will be available across all projects.

## Plugin Architecture
- **ScriptConfiguration**: Data model for script settings
- **ScriptLauncherService**: Manages script storage and execution
- **ScriptLauncherToolWindow**: Main UI interface
- **ScriptConfigurationDialog**: Add/edit script form

## Next Steps
- Add keyboard shortcuts for frequently used scripts
- Support for environment variables
- Script templates and presets
- Integration with run configurations
- Script output capture and display
