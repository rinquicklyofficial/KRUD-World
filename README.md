# 🌍 KRUD World
### ✨ A High-Performance Minecraft World Generator ✨

[![License](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Paper%20%7C%20Spigot-green.svg)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)

**KRUD World** is a powerful, feature-rich world generator for Minecraft servers, specifically designed for the **KRUD SMP**. Based on the robust **Iris World Generator** engine, it brings unparalleled performance and breathtaking aesthetics to your server's terrain.

---

## 🚀 Key Features

*   🏙️ **Custom Dimensions:** Explore the legendary **Khmer Kingdom**, a unique dimension crafted for KRUD SMP.
*   ⚡ **Performance-First:** Built on the Iris engine, providing industry-leading world generation speed.
*   📦 **Cross-Version Support:** Compatible with Minecraft versions **1.20.1** all the way to **1.21.1**.
*   🔔 **DonutSMP Style Alerts:** Branded messaging system for a modern server feel.
*   🎨 **Full Customization:** Branded KRUD configuration and specialized commands.
*   🌏 **Global Support:** Native support for the `Asia/Phnom_Penh` timezone.

---

## 🗺️ Included Dimensions

Explore a variety of unique worlds out of the box:
*   🏞️ **Overworld**
*   ⚪ **Vanilla**
*   📏 **Flat**
*   🔴 **Redstone**
*   ☄️ **Mars**
*   ✨ **New Horizons**
*   🌑 **The End**

---

## 🎮 Commands & Permissions

### Base Command: `/kworld` (Aliases: `/kw`, `/iris`)
The main administrative command for managing your worlds.

| Command | Description | Permission |
|:--- |:--- |:--- |
| `/kworld create <name> [dimension] [seed] [main]` | Create a new world using a specific dimension. | `kworld.admin` |
| `/kworld tp <world> [player]` | Teleport yourself or another player to a world. | `kworld.admin` |
| `/kworld list` | List all available KrudWorld dimensions. | `kworld.admin` |
| `/kworld pregen <radius> [world] [center] [gui]` | Start world pregeneration for better performance. | `kworld.admin` |
| `/kworld pregen stop` | Stop the active pregeneration task. | `kworld.admin` |
| `/kworld pregen pause` | Pause or resume the active pregeneration task. | `kworld.admin` |
| `/kworld studio` | Open the KrudWorld Studio editor. | `kworld.studio` |
| `/kworld version` | Display the current plugin version. | `kworld.admin` |

### Permissions Summary
*   🔑 **`kworld.admin`**: Full administrative access (Defaults to OP).
*   🛠️ **`kworld.studio`**: Access to the Studio world editor (Defaults to OP).
*   🚀 **`kworld.bypass`**: Bypass specific world generation restrictions.

---

## 🛠️ Build & Installation

### Requirements
*   **Java 21** or newer.
*   **Gradle** (Bundled via `gradlew`).

### Building from Source
```bash
# Clone the repository
git clone https://github.com/KrudStudio/KRUD-World.git

# Enter the directory
cd KRUD-World

# Build the project
./gradlew build
```
The compiled `.jar` file will be located in `build/libs/`.

---

## 📜 Attribution & Licensing

This project is a fork of the **Iris World Generator** by **Volmit Software** (Arcane Arts).

*   **Original Project:** [Iris on GitHub](https://github.com/VolmitSoftware/Iris)
*   **Original License:** GPL-3.0
*   **Modified by:** Krud Studio

This project is licensed under the **GNU General Public License v3.0**. Under the terms of this license, this fork and any derivatives **must remain open source**.

---

## 🙌 Credits
*   🛡️ **Krud Studio** — KRUD modifications, branding, and Khmer Kingdom dimension.
*   🔮 **Volmit Software** — The incredible original Iris engine.

---
<p align="center">Made with ❤️ for the KRUD SMP Community</p>
