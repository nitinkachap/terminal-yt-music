# 🎵 Terminal Music Streamer

A simple, lightweight terminal-based music streaming application that searches and plays music from YouTube Music using `yt-dlp`

## ✨ Features

- 🔍 **Search** for any song directly from terminal
- 🎵 **Stream** audio without downloading
- 📋 **Display** song titles and duration
- 🎮 **Simple** number-based selection
- 💨 **Fast** and lightweight
- 🖥️ **Pure terminal** interface - no GUI needed

## 📋 Requirements

- Java 17 or higher
- yt-dlp
- ffmpeg (optional, for better audio quality)

## 🔧 Installation

### 1. Install Dependencies

```bash
# Install required packages
sudo pacman -S jdk-openjdk yt-dlp mpv ffmpeg

# Verify installations
java --version
yt-dlp --version
mpv --version

#run
chmod +x start.sh
./start.sh

Search your song and enjoy ;)
