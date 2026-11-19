# NEW-LAUNCHER — Liquid Glass Android Launcher

A GitHub-ready Android Studio project built on the supplied NEW-LAUNCHER source. The
liquid / glassy visual language, living wallpaper, theme engine, app drawer, widgets,
notifications and launcher architecture are kept as the foundation.

## Build a debug APK

1. Push this repository to GitHub.
2. Open **Actions**.
3. Run **Build NEW-LAUNCHER Debug APK**, or push to `main` / `master`.
4. Download the `NEW-LAUNCHER-debug` artifact from the workflow run.

JDK 17, Android Gradle Plugin 8.5.2, Gradle 8.7, compile SDK 34, min SDK 26.

## What is new in 3.1

### Type & Language Studio (per folder, per app, per widget)

Every piece of launcher text can carry its own typeface and its own language:

| Target | Applies to |
| --- | --- |
| Home labels | every home-grid label |
| Dock labels | the dock |
| App drawer | drawer labels |
| Clock / Battery / Weather / Music / Activity | that widget |
| `Folder: <name>` | the folder title and the labels inside it |
| `App: <package>` | one app's label |

Resolution order is item → the folder that owns it → the home default, so setting a folder
sets its members unless a member overrides it.

The studio supports:

- **Search by font name** across the built-in catalogue and, when online, the whole Google
  Fonts library (about 1,900 families).
- **Language filter** for 50 languages covering Devanagari, Bengali, Gurmukhi, Gujarati,
  Odia, Tamil, Telugu, Kannada, Malayalam, Sinhala, Arabic, Hebrew, Thai, Lao, Khmer,
  Myanmar, Tibetan, Ethiopic, Armenian, Georgian, Cyrillic, Greek, Han, Japanese, Korean,
  Cherokee, Syriac, Thaana, Tifinagh, Adlam and Latin.
- **Download on demand**: the file is fetched from the public `google/fonts` repository
  (the same files Google Fonts serves), validated with `Typeface.createFromFile`, stored in
  the launcher's private storage and reused offline.
- Live preview rendered in the actual downloaded typeface.

Entry points: home top bar, Theme Engine › Type & language, any folder sheet, and the
long-press menu of any app.

### Workspace

- Edit mode with **real drag reordering** (long-press an icon, drag, drop), **undo** for six
  seconds, **drag-to-create folder** (hold a dragged app over another app) and **edge
  auto-scroll** while dragging.
- Five **dock styles**, five **grid presets**, **one-handed mode**, **floating bubble**.

### Gestures, privacy, widgets

- **Gesture Studio**: swipe up, swipe down, double tap, long press → any action.
- **App pairs** for split screen, **private space** with a PIN for hidden apps.
- **3D app carousel** (coverflow shelf), **multi-device hub** (send/receive your whole layout
  as one file through any share channel), **gesture trail** and **liquid touch ripple**.
- **Widget stack** (clock › weather › music › timer › world clock › countdown),
  **Open-Meteo weather** (no key, no location permission), **device monitor**,
  **activity timeline**, **live activity island**, **charging edge lighting**.

## Glitches fixed in 3.1

- Home clock no longer clips: the numerals are measured against the card and scaled to fit
  (the old fixed 38sp / 46sp line height inside a 114dp card cut "12:30" in half at large
  display or font scales).
- Icon mismatches: Wi-Fi no longer uses the Settings gear, the torch tile follows the real
  torch state, Feature Studio has its own icon, Private and Pairs tiles have matching icons.
- Duplicate apps on page 2, uneven grid rows, clipped battery text and an overflowing idle
  clock are all fixed.

## Feature status

The Feature Studio (`Dashboard` icon on the home bar, or Theme Engine › Feature Studio)
lists all 48 features with a status badge:

- **READY** — wired up and working on any supported device.
- **NEEDS PERMISSION** — works after one Android grant (notification access, accessibility).
- **DEVICE DEPENDENT** — depends on OEM multi-window support (floating windows, split
  screen app pairs, task switching).
- **PLANNED** — none. All 48 features are built; the badge now only distinguishes READY,
  NEEDS PERMISSION and DEVICE DEPENDENT.

Prediction and recommendations stay deterministic and on-device. No usage data leaves the
phone. Weather and font downloads are the only network calls.

## Network use

| Endpoint | Why |
| --- | --- |
| `fonts.google.com/metadata/fonts` | the online font catalogue |
| `api.github.com/repos/google/fonts/...` | locating the `.ttf` for a family |
| `raw` download URL from the same repo | the font file itself |
| `geocoding-api.open-meteo.com` | turning the city name into coordinates |
| `api.open-meteo.com` | current weather |

No API keys, no accounts, no analytics.

## Visual design constraints

Keep the established liquid, bubbly, glowing, transparent and glassy design language. New
modules are added as separate files and integrated with minimal edits to the existing core
visual files; animations are not removed as a shortcut for performance.
