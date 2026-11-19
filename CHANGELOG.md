# v3.2 — every feature built, nothing left planned

- **Liquid touch ripple** — every glass touch target now expands a soft double ring from the
  exact point pressed, on top of the jelly squash.
- **Gesture trail** — a fading ribbon of droplets follows the finger across the home screen.
  It watches the Initial pointer pass, so it never consumes a tap, drag or pager swipe.
- **3D app carousel** — coverflow shelf of every app; the centred card stands upright and the
  rest rotate and dim in depth. Open it from the Control Center tile or bind it to a gesture.
- **Multi-device hub** — sends this device's whole layout (theme, dock, folders, grid,
  gestures, fonts, private PIN) to another phone as a single file through any share channel,
  and receives it back. FileProvider + cache-dir staging, no server, no upload.
- Two new gesture actions (3D carousel, multi-device hub) appended to the enum so previously
  saved gesture assignments keep their ordinals.
- Feature Studio: no feature is marked PLANNED any more.

# v3.1 — glitch pass + Type & Language Studio

## Glitches fixed
- **Home clock was cut in half.** The numerals used a fixed 38sp size with a 46sp line
  height inside a fixed 114dp card, so a long string (12:30), a large display size or a
  font scale above 1.0 clipped the glyphs top and bottom. The clock now measures the card
  it is actually in (`BoxWithConstraints`) and sizes the numerals to fit, with a line
  height derived from the font size and no clipping animation.
- **Symbol mismatches.** The Wi-Fi quick tile was drawn with the Settings gear (so Wi-Fi
  and Settings showed the same icon), the torch tile always showed the "off" icon even
  while lit, the Feature Studio button used a gear, and the new Private / Pairs tiles were
  borrowing unrelated icons. Each tile now has the icon that matches its label, and the
  torch tile swaps FlashlightOn / FlashlightOff with the real torch state.
- **The same apps appeared on two pages.** Page 1 counted folder cells when deciding how
  many apps it had shown, but pages 2+ counted app cells only, so apps were re-drawn.
  Both pages now slice the same ordered list.
- **Grid rows were uneven.** Empty cells collapsed to 1dp, so rows with fewer items were
  shorter. Every cell now reserves the same height.
- Battery percentage / caption could clip in a narrow widget; the idle clock could
  overflow at large font scales. Both are now bounded and ellipsised.
- Undo chip, edit chip and the new sheets are closed by Back in the right order.

## New: Type & Language Studio
- Pick **any folder, any app label, the dock, the drawer or any widget**, then give it its
  own font and its own language. Resolution order is item → its folder → home default.
- **Search fonts by name**, filter by **any language** (50 languages across Indian and
  world scripts), then **download** the family on demand and apply it. Fonts come from the
  public google/fonts repository (the same files Google Fonts serves), are stored in the
  launcher's private storage and keep working offline.
- Installed fonts are listed with a live preview rendered in the actual typeface.
- Entry points: home top bar, Theme Engine, folder sheet, and every app's long-press menu.

## New: workspace
- Dock styles: glass pill, floating, minimal, frosted bar, invisible.
- Grid presets: 3×5, 4×5, 4×6, 5×5, 5×6.
- Edit mode with real drag reordering, undo, and drag-to-create folder (hold an app over
  another app).
- Edge auto-scroll while dragging, one-handed mode, floating shortcut bubble.

## New: gestures & privacy
- Gesture Studio: swipe up, swipe down, double tap and long press each map to any action.
- App pairs (split screen where the device supports it).
- Private space: PIN-gated hidden apps.

## New: widgets
- Interactive widget stack (clock › weather › music › timer › world clock › countdown).
- Weather from Open-Meteo (no key, no location permission — the city is geocoded by name).
- World clock, calendar countdown, timer/stopwatch, device monitor, activity timeline.
- Live activity island with transport controls, charging edge lighting.

# Liquid Glass Launcher – v2.0

## Glitches fixed
- Drawer no longer lets taps fall through to home icons underneath.
- Home grid can no longer cut off apps on small screens (rows are computed from real height; extra apps flow to new pages).
- Page count was stale when apps loaded late; pager now updates correctly.
- Battery was read once; now live (with charging state).
- Swipe-up fired on every drag event; now a single clean gesture (up = drawer, down = notifications).
- Pressing Home now closes drawer/menus and returns to the main page (onNewIntent).
- Back button closes menu → theme engine → folder → drawer in order.
- Icon buttons were clipped (48dp button in 46dp pill); replaced with proper glass buttons.
- Glass rim gradient now scales with surface size (was hard-coded 400px).
- Wallpaper, theme, dock, hidden apps and usage now persist (wallpaper uses persistable URI permission).
- App list auto-refreshes on install/uninstall/update.
- Icons/colours are pre-computed off the main thread (no scroll jank); apps launch by exact activity with zoom animation.
- White flash on launch removed (dark window theme), no-recents/clearTask quirks removed, config changes handled without recreate.
- Missing launcher icon, proguard file, and uninstall permission added.

## New
- Living wallpaper (aurora blobs, dot grid, scan line, bubbles) with gyro + page parallax; custom image gets parallax too.
- Glass 2.0: gyro specular glare, sheen sweep, bevel rim; jelly squash/stretch touch, pop-in, idle bobbing.
- Widgets: rolling-digit clock with seconds ring, liquid-wave battery, RAM/Storage gauges, quick tiles (torch, Wi-Fi, Bluetooth, Settings), frequent apps.
- Control Center page left of home; 3D glass page transitions.
- Drawer: blurred/scaled home behind (Android 12+), A–Z fast scroller with magnified bubble, web-search fallback, pull-down to close, enter launches single match.
- Long-press menu: open, app info, uninstall, add/remove dock, hide app.
- Theme Engine: presets, secondary hue, icon size, 7 effect toggles, restore hidden apps, wallpaper controls.

# v3.0 additions
- Notification badges (glass count bubbles on icons and folders) via notification access.
- Music widget (spinning art, jelly play/pause/next) and animated time-of-day Sky widget.
- Icon shapes: Squircle / Circle / Drop / Square.
- Your own folders: long-press app > Add to folder (create, add, remove, delete).
- Smart suggestions for the current time of day (morning / afternoon / evening / night).
- Focus mode: mark apps as distracting, toggle Focus tile to hide them.
- Match theme to wallpaper (extracts two hues from your image).
- Backup / restore of theme, dock, folders, hidden and focus apps to a JSON file.
- Idle mode: always-on style liquid clock that drifts to avoid burn-in; tap to exit.
- Water-drop sound effects (synthesised, toggleable).
- Gestures: double-tap empty space = lock screen, long-press empty space = Theme Engine.
