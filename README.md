# Achordify 🎵

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](./LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Desktop-green.svg)]()
[![Min Android Version](https://img.shields.io/badge/Android-8.0%2B%20(API%2026)-orange.svg)]()
[![Build](https://img.shields.io/badge/Architecture-Split--ABI%20Optimized-purple.svg)]()

**Achordify** is a modern, lightweight, privacy-respecting music streaming application for Android and desktop. Built with Kotlin and Jetpack Compose Multiplatform, Achordify delivers an ad-free, high-fidelity listening experience powered by YouTube Music, accompanied by synchronized dynamic lyrics, local audio caching, intelligent multi-seed recommendations, and a dark YouTube Music-inspired user interface.

> [!NOTE]
> **Attribution & Upstream Lineage**: Achordify is an independent fork of the open-source [BitChord](https://github.com/kushagrasinghx/BitChord) project created by [Kushagra Singh](https://github.com/kushagrasinghx). We gratefully acknowledge and credit Kushagra Singh and the BitChord contributors for laying the foundation. In adherence to the **GNU General Public License v3.0 (GPLv3)**, Achordify is fully open-source, keeps all original license notices, and makes its modifications available to the community.

---

## ✨ Features

### 🎧 Core Playback & Audio
- **Ad-Free Music Streaming**: Seamlessly stream any song, album, artist, or playlist from YouTube Music without audio ads or video interruptions.
- **Offline Audio Caching**: Automatic and user-configured song caching with discrete storage limits (50 MB floor, 100 MB default up to Unlimited).
- **Background & Lock Screen Playback**: Full support for background audio playback, persistent notification controls, and lock screen media sessions.
- **Android Auto & Ecosystem**: Seamless automotive infotainment integration, Android Auto media browser support, and Discord Rich Presence for desktop.

### 🧠 Intelligent Multi-Seed "Quick Picks" Discovery
- **Multi-Seed Recommendation Engine**: Rather than basing Quick Picks on a single randomly selected song, Achordify randomly samples up to **5 favorite songs** from your top 10 most played tracks.
- **Parallel Radio Mixes**: Radio suggestions for all 5 seeds are fetched concurrently using Kotlin coroutines and round-robin interleaved into a vibrant 24-track Quick Picks shelf.
- **Spotify Compatibility**: Spotify integration in Achordify is utilized strictly for **Spotify Canvas** (looping background artwork) and **Spotify Playlist Matching**. Your home shelves and Quick Picks are generated independently through YouTube Music and local playback history—meaning connecting Spotify will never disrupt your personalized Quick Picks!

### 🎨 Clean Dark UI & YouTube Music Styling
- **Glass Bottom Navigation**: 4 core destinations (**1. Home**, **2. Search**, **3. Explore**, **4. Library**) uniformly embedded within a sleek frosted glass navigation bar.
- **Dynamic Full-Screen Player**:
  - Ambient vertical gradient fading from dominant artwork tones into deep dark `#0C0C0C`.
  - Non-distorting fixed rounded square artwork container (`18.dp`).
  - Symmetrical 5-button transport controls with a distinct circular white Play/Pause button.
  - Bottom peek sheet offering instantaneous access to **UP NEXT**, synchronized **LYRICS**, and **RELATED** songs.
- **Curated Indian & International Mood Chips**:
  - Native explore filter chips for Hindi, Ghazal/Sufi, Hindustani Classical, Indian Indie, Devotional, Bhojpuri, and Party.
- **Fluid Queue & Drawer Management**:
  - Clamped drag-and-drop queue reordering with IPC synchronization.
  - Deep song actions drawer expanding immediately to >80% screen height for quick access to actions without scrolling.
- **Synchronized Real-Time Lyrics**:
  - Word-level and line-synchronized animated lyrics powered by LRCLIB, BetterLyrics, Unison, Genius, and Paxsenix.

---

## 🏗️ Architecture & Important Developer Notice

> [!WARNING]
> ### ⚠️ DO NOT Globally Replace `bitchord` in Codebase Packages
> If you are contributing to or modifying this repository, **do NOT perform a global search-and-replace of `bitchord` with `achordify` in the source tree!**
> 
> Here is why internal package namespaces are intentionally preserved as `com.music.bitchord`:
> 1. **Native C++ JNI Bindings**: The low-level audio engine and decoder in `app/src/main/cpp` register native JNI function pointers matching the package structure (e.g. `Java_com_music_bitchord_...`). Changing package names causes native linkage to fail with runtime `UnsatisfiedLinkError`, causing the app to crash immediately when playing audio.
> 2. **Disk Directory Hierarchy**: In Kotlin/Java, package namespaces correspond to directory paths on the filesystem (`com/music/bitchord/...`). Changing declarations without moving hundreds of files causes compilation failures.
> 3. **Inter-Process Communication (IPC)**: Foreground media playback services and background notification receivers communicate using established intent action strings.
> 
> **How Achordify Rebrands Safely**:
> - **Display App Name**: Set in `app/src/main/res/values/strings.xml` (`<string name="app_name">Achordify</string>`).
> - **Application ID**: Configured in `app/build.gradle.kts` (`applicationId = "com.music.achordify"`).
> - **Public Repository & Documentation**: All public-facing documentation, assets, and releases use the **Achordify** name.

---

## 📦 Lightweight Split-ABI Builds

Upstream builds frequently exceed 70–80 MB due to universal packaging and bundled offline vocal isolation AI models. Achordify is optimized with lightweight build flags and ABI splits:

| Artifact | Architecture | Target Devices | Typical Size |
| :--- | :--- | :--- | :--- |
| `app-dev-arm64-v8a-release.apk` | 64-bit ARM | Modern Android smartphones & tablets | **~30.0 MB** |
| `app-dev-armeabi-v7a-release.apk` | 32-bit ARM | Legacy 32-bit Android phones | **~28.5 MB** |
| `app-dev-x86_64-release.apk` | 64-bit x86 | Android emulators, ChromeOS, x86 tablets | **~30.6 MB** |
| `app-dev-universal-release.apk` | Universal | All architectures combined | **~43.2 MB** |

---

## 🛠️ Building & Compiling

### Prerequisites
- **Android Studio** Hedgehog (2023.1.1) or higher
- **JDK 17** (Eclipse Temurin or Azul Zulu recommended)
- **Android SDK** API 26 (Min) to API 37 (Compile)

### 1. Clone the Repository
```bash
git clone https://github.com/ajay8873/achordify.git
cd achordify
```

### 2. Build Release APKs
Execute the Gradle wrapper command corresponding to your platform:

```bash
# Windows (PowerShell)
.\gradlew.bat :app:assembleDevRelease

# Linux / macOS
./gradlew :app:assembleDevRelease
```

Generated APKs will be saved to:
```
app/build/outputs/apk/dev/release/
```

---

## 📚 Technical Documentation

For an in-depth changelog and technical implementation details of all modified components, please consult:
- [`CUSTOM_UI_MODIFICATIONS.md`](./CUSTOM_UI_MODIFICATIONS.md) — Comprehensive guide covering every file modification, UI layout change, and custom parameter.

---

## 📜 License & Open Source Attribution

Achordify is licensed under the **GNU General Public License v3.0 (GPLv3)**.

- **Copyleft**: Any modifications or derivatives must remain open-source under GPLv3.
- **Attribution**: Derived from [BitChord](https://github.com/kushagrasinghx/BitChord) by [Kushagra Singh](https://github.com/kushagrasinghx) (Copyright (c) BitChord Contributors).
- **Disclaimer**: Achordify is not affiliated with, endorsed by, or associated with Google LLC, YouTube, or Spotify. All brand names and trademarks belong to their respective owners.
