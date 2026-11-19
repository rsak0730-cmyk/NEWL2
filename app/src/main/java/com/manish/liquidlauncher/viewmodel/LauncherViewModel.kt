package com.manish.liquidlauncher.viewmodel

import android.app.ActivityOptions
import android.app.Application
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import org.json.JSONObject
import java.net.URL
import com.manish.liquidlauncher.data.LauncherPrefs
import com.manish.liquidlauncher.fonts.CatalogFont
import com.manish.liquidlauncher.fonts.FontCatalog
import com.manish.liquidlauncher.fonts.FontLibrary
import com.manish.liquidlauncher.fonts.Languages
import com.manish.liquidlauncher.model.AppInfo
import com.manish.liquidlauncher.model.AppPair
import com.manish.liquidlauncher.model.CustomFolder
import com.manish.liquidlauncher.model.FontChoice
import com.manish.liquidlauncher.model.FolderState
import com.manish.liquidlauncher.model.GestureAction
import com.manish.liquidlauncher.model.GestureMap
import com.manish.liquidlauncher.model.GestureSlot
import com.manish.liquidlauncher.model.LaunchEvent
import com.manish.liquidlauncher.model.LiquidThemeConfig
import com.manish.liquidlauncher.model.TypeTargets
import com.manish.liquidlauncher.model.WeatherInfo
import com.manish.liquidlauncher.service.LockAccessibilityService
import com.manish.liquidlauncher.util.ThemeFromImage
import com.manish.liquidlauncher.util.drawableToBitmap
import com.manish.liquidlauncher.util.extractDominantColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val app: Application = application
    private val prefs = LauncherPrefs(application)

    // ---- App lists -------------------------------------------------------------------------
    var allApps by mutableStateOf<List<AppInfo>>(emptyList())
        private set
    var installedApps by mutableStateOf<List<AppInfo>>(emptyList())
        private set
    var dockApps by mutableStateOf<List<AppInfo>>(emptyList())
        private set
    var frequentApps by mutableStateOf<List<AppInfo>>(emptyList())
        private set
    var suggestedApps by mutableStateOf<List<AppInfo>>(emptyList())
        private set
    var hiddenPackages by mutableStateOf(prefs.loadHidden())
        private set
    var distractingPackages by mutableStateOf(prefs.loadDistracting())
        private set
    var customFolders by mutableStateOf(prefs.loadFolders())
        private set
    var focusMode by mutableStateOf(prefs.focusMode)
        private set

    // ---- UI state --------------------------------------------------------------------------
    var searchQuery by mutableStateOf("")
    var isDrawerOpen by mutableStateOf(false)
        private set
    var isThemeEngineOpen by mutableStateOf(false)
    var idleOpen by mutableStateOf(false)
    var openedFolder by mutableStateOf<FolderState?>(null)
    var menuApp by mutableStateOf<AppInfo?>(null)
    var homeTick by mutableIntStateOf(0)
        private set
    var customWallpaperUri by mutableStateOf<Uri?>(prefs.wallpaperUri?.let { Uri.parse(it) })
        private set
    var themeConfig by mutableStateOf(prefs.loadTheme())
        private set
    var torchOn by mutableStateOf(false)
        private set

    // ---- Type & language (per folder / per app / per widget) --------------------------------
    var typeMap by mutableStateOf(prefs.loadTypeMap())
        private set
    var typeRevision by mutableIntStateOf(0)
        private set
    var typeStudioOpen by mutableStateOf(false)
    var typeStudioTarget by mutableStateOf(TypeTargets.HOME)
        private set
    var fontQuery by mutableStateOf("")
    var fontLanguage by mutableStateOf("")
        private set
    var fontResults by mutableStateOf<List<CatalogFont>>(emptyList())
        private set
    var fontSearching by mutableStateOf(false)
        private set
    var fontBusyId by mutableStateOf<String?>(null)
        private set
    var fontNote by mutableStateOf<String?>(null)
        private set

    // ---- Workspace -------------------------------------------------------------------------
    var workspaceOrder by mutableStateOf(prefs.loadOrder() ?: emptyList())
        private set
    var undoOrder by mutableStateOf<List<String>?>(null)
        private set
    var editMode by mutableStateOf(false)
    var gestureMap by mutableStateOf(prefs.loadGestures())
        private set
    var appPairs by mutableStateOf(prefs.loadPairs())
        private set
    var gestureStudioOpen by mutableStateOf(false)
    var carouselOpen by mutableStateOf(false)
    var deviceHubOpen by mutableStateOf(false)

    // ---- Private space ---------------------------------------------------------------------
    var privateSpaceOpen by mutableStateOf(false)
    var privateUnlocked by mutableStateOf(false)
        private set
    var privatePin by mutableStateOf(prefs.privatePin)
        private set
    var privateError by mutableStateOf(false)
        private set

    // ---- Activity --------------------------------------------------------------------------
    var history by mutableStateOf(prefs.loadHistory())
        private set

    // ---- Weather ---------------------------------------------------------------------------
    var weatherCity by mutableStateOf(prefs.weatherCity ?: "New Delhi")
        private set
    var weather by mutableStateOf<WeatherInfo?>(null)
        private set
    var weatherBusy by mutableStateOf(false)
        private set

    // ---- Timer / stopwatch ------------------------------------------------------------------
    var timerStartedAt by mutableStateOf<Long?>(null)
        private set
    var timerAccumMs by mutableLongStateOf(0L)
        private set
    val timerRunning: Boolean get() = timerStartedAt != null

    // ---- Countdown ---------------------------------------------------------------------------
    var countdownTarget by mutableStateOf(prefs.countdownTarget)
        private set
    var countdownLabel by mutableStateOf(prefs.countdownLabel ?: "New Year")
        private set

    // ---- Internals -------------------------------------------------------------------------
    private var storedDock: List<String>? = prefs.loadDock()
    private var loadJob: Job? = null
    private var saveJob: Job? = null
    private var torchId: String? = null
    private val cameraManager: CameraManager? =
        app.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadInstalledApps(delayMs = 500)
        }
    }

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            if (cameraId == torchId) torchOn = enabled
        }
    }

    init {
        FontLibrary.init(application)
        // Live-refresh when apps are installed / removed / updated
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(app, packageReceiver, filter, ContextCompat.RECEIVER_EXPORTED)

        // Flashlight tile
        try {
            val cm = cameraManager
            if (cm != null) {
                torchId = cm.cameraIdList.firstOrNull { id ->
                    cm.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                }
                if (torchId != null) {
                    cm.registerTorchCallback(torchCallback, Handler(Looper.getMainLooper()))
                }
            }
        } catch (_: Exception) {
            torchId = null
        }

        loadInstalledApps()
    }

    override fun onCleared() {
        try {
            app.unregisterReceiver(packageReceiver)
        } catch (_: Exception) {
        }
        try {
            cameraManager?.unregisterTorchCallback(torchCallback)
        } catch (_: Exception) {
        }
        super.onCleared()
    }

    // ---- Loading ---------------------------------------------------------------------------
    fun loadInstalledApps(delayMs: Long = 0L) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (delayMs > 0) delay(delayMs)
            val apps = withContext(Dispatchers.Default) { queryApps() }
            allApps = apps
            rebuild()
        }
    }

    private fun queryApps(): List<AppInfo> {
        val pm = app.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(mainIntent, 0)
            .filter { it.activityInfo.packageName != app.packageName }
            .distinctBy { it.activityInfo.packageName }
            .map { ri ->
                val bitmap = drawableToBitmap(ri.loadIcon(pm))
                AppInfo(
                    label = ri.loadLabel(pm).toString(),
                    packageName = ri.activityInfo.packageName,
                    activityName = ri.activityInfo.name,
                    icon = bitmap.asImageBitmap(),
                    tint = extractDominantColor(bitmap)
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    private fun defaultDock(visible: List<AppInfo>): List<String> {
        val pm = app.packageManager
        val probes = listOf(
            Intent(Intent.ACTION_DIAL),
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING),
            Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA),
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_BROWSER)
        )
        val known = visible.map { it.packageName }.toSet()
        val picks = mutableListOf<String>()
        for (probe in probes) {
            val pkg = try {
                pm.resolveActivity(probe, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
            } catch (_: Exception) {
                null
            }
            if (pkg != null && pkg in known && pkg !in picks) picks.add(pkg)
        }
        for (a in visible) {
            if (picks.size >= 4) break
            if (a.packageName !in picks) picks.add(a.packageName)
        }
        return picks
    }

    private fun rebuild() {
        val visible = allApps.filter {
            it.packageName !in hiddenPackages && !(focusMode && it.packageName in distractingPackages)
        }
        installedApps = visible
        val byPkg = visible.associateBy { it.packageName }
        val dockPackages = storedDock ?: defaultDock(visible)
        dockApps = dockPackages.mapNotNull { byPkg[it] }.take(MAX_DOCK)
        val counts = prefs.launchCounts()
        frequentApps = visible
            .filter { (counts[it.packageName] ?: 0) > 0 }
            .sortedByDescending { counts[it.packageName] ?: 0 }
            .take(4)
        val bucketCounts = prefs.bucketCounts(prefs.currentBucket())
        suggestedApps = visible
            .filter { (bucketCounts[it.packageName] ?: 0) > 0 }
            .sortedByDescending { bucketCounts[it.packageName] ?: 0 }
            .take(4)
    }

    // ---- Navigation / overlays -------------------------------------------------------------
    fun openDrawer() {
        menuApp = null
        openedFolder = null
        isDrawerOpen = true
    }

    fun closeDrawer() {
        isDrawerOpen = false
        searchQuery = ""
    }

    fun onHomePressed() {
        idleOpen = false
        isDrawerOpen = false
        isThemeEngineOpen = false
        openedFolder = null
        menuApp = null
        searchQuery = ""
        homeTick++
    }

    fun handleBack() {
        when {
            idleOpen -> idleOpen = false
            privateSpaceOpen -> privateSpaceOpen = false
            carouselOpen -> carouselOpen = false
            deviceHubOpen -> deviceHubOpen = false
            gestureStudioOpen -> gestureStudioOpen = false
            typeStudioOpen -> typeStudioOpen = false
            menuApp != null -> menuApp = null
            isThemeEngineOpen -> isThemeEngineOpen = false
            openedFolder != null -> openedFolder = null
            isDrawerOpen -> {
                if (searchQuery.isNotEmpty()) searchQuery = "" else closeDrawer()
            }
        }
    }

    // ---- Actions ---------------------------------------------------------------------------
    fun launchApp(context: Context, view: View?, app: AppInfo) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(app.packageName, app.activityName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }
        try {
            val options = if (view != null) {
                ActivityOptions.makeScaleUpAnimation(view, view.width / 2, view.height / 2, 1, 1)
            } else {
                ActivityOptions.makeCustomAnimation(context, android.R.anim.fade_in, android.R.anim.fade_out)
            }
            context.startActivity(intent, options.toBundle())
            prefs.bumpLaunch(app.packageName)
            prefs.pushHistory(app.packageName)
            history = prefs.loadHistory()
            rebuild()
            closeDrawer()
            openedFolder = null
            menuApp = null
        } catch (_: Exception) {
            Toast.makeText(context, "Can't open ${app.label}", Toast.LENGTH_SHORT).show()
            loadInstalledApps()
        }
    }

    fun openAppInfo(context: Context, app: AppInfo) {
        safeStart(context, Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}")))
        menuApp = null
    }

    fun uninstall(context: Context, app: AppInfo) {
        safeStart(context, Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.packageName}")))
        menuApp = null
    }

    fun openSettingsScreen(context: Context, action: String) {
        safeStart(context, Intent(action))
    }

    fun webSearch(context: Context, query: String) {
        if (query.isBlank()) return
        safeStart(context, Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))))
        closeDrawer()
    }

    fun openDefaultLauncherPicker(context: Context) {
        try {
            context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
            safeStart(context, Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
        }
    }

    /** Pull down the notification shade (needs the normal EXPAND_STATUS_BAR permission). */
    fun expandNotifications(context: Context) {
        try {
            val service = context.getSystemService("statusbar")
            Class.forName("android.app.StatusBarManager")
                .getMethod("expandNotificationsPanel")
                .invoke(service)
        } catch (_: Throwable) {
        }
    }

    fun toggleTorch() {
        val id = torchId ?: run {
            Toast.makeText(app, "No flashlight available", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            cameraManager?.setTorchMode(id, !torchOn)
        } catch (_: Exception) {
        }
    }

    private fun safeStart(context: Context, intent: Intent) {
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
            Toast.makeText(context, "Action not available on this device", Toast.LENGTH_SHORT).show()
        }
    }

    // ---- Dock / hidden ---------------------------------------------------------------------
    fun isInDock(packageName: String): Boolean = dockApps.any { it.packageName == packageName }

    fun toggleDock(target: AppInfo) {
        val current = dockApps.map { it.packageName }
        if (target.packageName in current) {
            storedDock = current - target.packageName
        } else {
            if (current.size >= MAX_DOCK) {
                Toast.makeText(app, "Dock is full - remove an app first", Toast.LENGTH_SHORT).show()
                return
            }
            storedDock = current + target.packageName
        }
        prefs.saveDock(storedDock ?: emptyList())
        rebuild()
        menuApp = null
    }

    fun hideApp(target: AppInfo) {
        hiddenPackages = hiddenPackages + target.packageName
        prefs.saveHidden(hiddenPackages)
        rebuild()
        menuApp = null
    }

    fun restoreHiddenApps() {
        hiddenPackages = emptySet()
        prefs.saveHidden(hiddenPackages)
        rebuild()
    }

    // ---- Custom folders --------------------------------------------------------------------
    fun folderOf(packageName: String): String? =
        customFolders.firstOrNull { packageName in it.packages }?.name

    private fun setFolders(list: List<CustomFolder>) {
        customFolders = list.filter { it.packages.isNotEmpty() }
        prefs.saveFolders(customFolders)
    }

    fun addToFolder(name: String, target: AppInfo) {
        val clean = name.trim().take(20)
        if (clean.isEmpty()) return
        val updated = customFolders
            .map { f -> f.copy(packages = f.packages - target.packageName) }
            .toMutableList()
        val idx = updated.indexOfFirst { it.name.equals(clean, ignoreCase = true) }
        if (idx >= 0) {
            updated[idx] = updated[idx].copy(packages = updated[idx].packages + target.packageName)
        } else {
            updated.add(CustomFolder(clean, listOf(target.packageName)))
        }
        setFolders(updated)
        menuApp = null
    }

    fun removeFromFolder(target: AppInfo) {
        setFolders(customFolders.map { f -> f.copy(packages = f.packages - target.packageName) })
        openedFolder = null
        menuApp = null
    }

    fun deleteFolder(name: String) {
        setFolders(customFolders.filter { it.name != name })
        openedFolder = null
    }

    // ---- Focus mode ------------------------------------------------------------------------
    fun isDistracting(packageName: String) = packageName in distractingPackages

    fun toggleDistracting(target: AppInfo) {
        distractingPackages = if (isDistracting(target.packageName)) {
            distractingPackages - target.packageName
        } else {
            distractingPackages + target.packageName
        }
        prefs.saveDistracting(distractingPackages)
        rebuild()
        menuApp = null
    }

    fun toggleFocus() {
        focusMode = !focusMode
        prefs.focusMode = focusMode
        rebuild()
        val msg = if (focusMode) {
            if (distractingPackages.isEmpty()) "Focus on - long-press apps to mark them distracting" else "Focus on"
        } else {
            "Focus off"
        }
        Toast.makeText(app, msg, Toast.LENGTH_SHORT).show()
    }

    // ---- Lock gesture ----------------------------------------------------------------------
    fun lockScreen(context: Context) {
        if (!LockAccessibilityService.lock()) {
            Toast.makeText(
                context,
                "Enable \"Liquid lock\" in Accessibility to use double-tap lock",
                Toast.LENGTH_LONG
            ).show()
            safeStart(context, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    // ---- Backup / restore / wallpaper colours ----------------------------------------------
    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use {
                        it.write(prefs.exportJson().toByteArray(Charsets.UTF_8))
                    }
                    true
                } catch (_: Exception) {
                    false
                }
            }
            Toast.makeText(context, if (ok) "Backup saved" else "Backup failed", Toast.LENGTH_SHORT).show()
        }
    }

    /** Hands the whole layout to the system share sheet (Nearby Share, Bluetooth, mail, Drive). */
    fun shareLayout(context: Context) {
        com.manish.liquidlauncher.ui.screens.shareLayoutFile(context, prefs.exportJson())
    }

    fun importBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                try {
                    val text = context.contentResolver.openInputStream(uri)?.use {
                        it.readBytes().toString(Charsets.UTF_8)
                    }
                    text != null && prefs.importJson(text)
                } catch (_: Exception) {
                    false
                }
            }
            if (ok) {
                themeConfig = prefs.loadTheme()
                hiddenPackages = prefs.loadHidden()
                distractingPackages = prefs.loadDistracting()
                customFolders = prefs.loadFolders()
                focusMode = prefs.focusMode
                storedDock = prefs.loadDock()
                rebuild()
            }
            Toast.makeText(context, if (ok) "Backup restored" else "Couldn't read backup", Toast.LENGTH_SHORT).show()
        }
    }

    fun matchThemeToWallpaper(context: Context) {
        val uri = customWallpaperUri ?: return
        viewModelScope.launch {
            val hues = withContext(Dispatchers.Default) {
                ThemeFromImage.decode(context, uri)?.let { ThemeFromImage.extractHues(it) }
            }
            if (hues != null) {
                updateTheme(themeConfig.copy(accentHue = hues.first, secondaryHue = hues.second))
            } else {
                Toast.makeText(context, "Couldn't read wallpaper colours", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---- Theme / wallpaper -----------------------------------------------------------------
    fun updateTheme(newConfig: LiquidThemeConfig) {
        themeConfig = newConfig
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(250)
            prefs.saveTheme(newConfig)
        }
    }

    fun setWallpaper(uri: Uri?) {
        customWallpaperUri = uri
        prefs.wallpaperUri = uri?.toString()
    }

    // ---- Type & language -------------------------------------------------------------------

    fun openTypeStudio(target: String) {
        typeStudioTarget = target
        fontQuery = ""
        fontNote = null
        typeStudioOpen = true
    }

    /** Resolution order: exact target -> owning folder -> home default. */
    fun typeFor(target: String): FontChoice? {
        typeMap[target]?.let { return it }
        if (TypeTargets.isApp(target)) {
            val owner = folderOf(TypeTargets.packageName(target))
            if (owner != null) typeMap[TypeTargets.folder(owner)]?.let { return it }
        }
        return typeMap[TypeTargets.HOME]
    }

    fun typeFamily(target: String): FontFamily = FontLibrary.familyFor(typeFor(target))

    fun applyType(target: String, choice: FontChoice) {
        typeMap = typeMap + (target to choice)
        prefs.saveTypeMap(typeMap)
        typeRevision++
    }

    fun clearType(target: String) {
        if (typeMap.containsKey(target)) {
            typeMap = typeMap - target
            prefs.saveTypeMap(typeMap)
        }
        typeRevision++
    }

    fun chooseFontLanguage(tag: String) {
        fontLanguage = tag
    }

    fun searchFonts() {
        if (fontSearching) return
        fontSearching = true
        fontNote = null
        val query = fontQuery
        val language = fontLanguage
        viewModelScope.launch {
            val remote = FontLibrary.searchOnline(query, language)
            val fallback = FontCatalog.searchOffline(query, Languages.byTag(language)?.script ?: "")
            fontResults = if (remote.isNotEmpty()) remote else fallback
            if (remote.isEmpty()) {
                fontNote = if (fallback.isEmpty()) {
                    "No font matched that search."
                } else {
                    "Showing the built-in catalogue. Connect to the internet to search every " +
                        "Google Fonts family, in any language."
                }
            }
            fontSearching = false
        }
    }

    fun installFont(font: CatalogFont) {
        if (fontBusyId != null) return
        fontBusyId = font.id
        viewModelScope.launch {
            val result = FontLibrary.download(font)
            fontBusyId = null
            fontNote = if (result.ok) "${font.family} installed — tap it to apply" else result.message
            typeRevision++
        }
    }

    fun removeFont(id: String) {
        if (FontLibrary.delete(id)) {
            fontNote = "Font removed"
            typeRevision++
        }
    }

    fun installedFonts() = FontLibrary.installedFonts()

    // ---- Workspace order / undo ------------------------------------------------------------

    fun applyWorkspaceOrder(keys: List<String>) {
        undoOrder = workspaceOrder
        workspaceOrder = keys
        prefs.saveOrder(keys)
    }

    fun undoWorkspaceMove(): Boolean {
        val previous = undoOrder ?: return false
        workspaceOrder = previous
        prefs.saveOrder(previous)
        undoOrder = null
        return true
    }

    fun clearUndo() {
        undoOrder = null
    }

    fun toggleEditMode() {
        editMode = !editMode
        if (editMode) Toast.makeText(app, "Edit mode — drag icons to move, drop on another app to make a folder", Toast.LENGTH_LONG).show()
    }

    // ---- Gestures ---------------------------------------------------------------------------

    fun setGesture(slot: GestureSlot, action: GestureAction) {
        gestureMap = gestureMap.with(slot, action)
        prefs.saveGestures(gestureMap)
    }

    fun resetGestures() {
        gestureMap = GestureMap(emptyMap())
        prefs.saveGestures(gestureMap)
    }

    fun runGesture(action: GestureAction, context: Context) {
        when (action) {
            GestureAction.None -> Unit
            GestureAction.Drawer -> openDrawer()
            GestureAction.Notifications -> expandNotifications(context)
            GestureAction.ThemeEngine -> isThemeEngineOpen = true
            GestureAction.TypeStudio -> openTypeStudio(TypeTargets.HOME)
            GestureAction.IdleMode -> idleOpen = true
            GestureAction.Lock -> lockScreen(context)
            GestureAction.PrivateSpace -> openPrivateSpace()
            GestureAction.EditMode -> toggleEditMode()
            GestureAction.Carousel -> carouselOpen = true
            GestureAction.DeviceHub -> deviceHubOpen = true
            GestureAction.Search -> {
                searchQuery = ""
                openDrawer()
            }
        }
    }

    // ---- App pairs --------------------------------------------------------------------------

    fun addPair(name: String, first: String, second: String) {
        if (first == second) return
        val clean = name.trim().ifBlank { "Pair" }.take(18)
        appPairs = (appPairs.filterNot { it.name.equals(clean, ignoreCase = true) } +
            AppPair(clean, first, second)).take(6)
        prefs.savePairs(appPairs)
    }

    fun removePair(name: String) {
        appPairs = appPairs.filterNot { it.name == name }
        prefs.savePairs(appPairs)
    }

    fun launchPair(context: Context, pair: AppPair) {
        val byPkg = allApps.associateBy { it.packageName }
        val first = byPkg[pair.first] ?: return
        launchApp(context, null, first)
        val second = byPkg[pair.second] ?: return
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(second.packageName, second.activityName)
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            )
        }
        try {
            context.startActivity(intent)
            prefs.bumpLaunch(second.packageName)
            prefs.pushHistory(second.packageName)
            history = prefs.loadHistory()
        } catch (_: Exception) {
            Toast.makeText(context, "This device can't show two apps side by side", Toast.LENGTH_SHORT).show()
        }
    }

    // ---- Private space ----------------------------------------------------------------------

    fun openPrivateSpace() {
        privateError = false
        privateUnlocked = privatePin.isNullOrBlank()
        privateSpaceOpen = true
    }

    fun savePrivatePin(pin: String) {
        privatePin = pin.ifBlank { null }
        prefs.privatePin = privatePin
        privateUnlocked = true
        privateError = false
    }

    fun unlockPrivate(pin: String) {
        if (pin == privatePin) {
            privateUnlocked = true
            privateError = false
        } else {
            privateError = true
        }
    }

    fun unhideFromPrivate(app: AppInfo) {
        hiddenPackages = hiddenPackages - app.packageName
        prefs.saveHidden(hiddenPackages)
        rebuild()
    }

    // ---- Weather ----------------------------------------------------------------------------

    fun updateWeatherCity(city: String) {
        val clean = city.trim().take(40)
        if (clean.isBlank()) return
        weatherCity = clean
        prefs.weatherCity = clean
        refreshWeather()
    }

    /** Open-Meteo: free, no key, no location permission needed because the city is geocoded by name. */
    fun refreshWeather() {
        if (weatherBusy) return
        weatherBusy = true
        val city = weatherCity
        viewModelScope.launch {
            val info = withContext(Dispatchers.IO) { fetchWeather(city) }
            if (info != null) {
                weather = info
                prefs.weatherCache = info.toJson()
            }
            weatherBusy = false
        }
    }

    private fun fetchWeather(city: String): WeatherInfo? {
        return try {
            val geo = URL("https://geocoding-api.open-meteo.com/v1/search?name=" +
                java.net.URLEncoder.encode(city, "UTF-8") + "&count=1&language=en&format=json")
                .readText()
            val geoRoot = JSONObject(geo)
            val results = geoRoot.optJSONArray("results") ?: return null
            val place = results.optJSONObject(0) ?: return null
            val lat = place.optDouble("latitude")
            val lon = place.optDouble("longitude")
            val name = place.optString("name", city)
            val country = place.optString("country", "")
            val forecast = URL(
                "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                    "&current=temperature_2m,weather_code,is_day&timezone=auto"
            ).readText()
            val current = JSONObject(forecast).optJSONObject("current") ?: return null
            WeatherInfo(
                city = if (country.isBlank()) name else "$name, $country",
                temperatureC = current.optDouble("temperature_2m", Double.NaN).toFloat(),
                code = current.optInt("weather_code", -1),
                isDay = current.optInt("is_day", 1) == 1,
                updatedAt = System.currentTimeMillis()
            )
        } catch (_: Throwable) {
            prefs.weatherCache?.let { WeatherInfo.fromJson(it) }
        }
    }

    // ---- Timer / stopwatch ------------------------------------------------------------------

    fun toggleTimer() {
        if (timerRunning) {
            timerAccumMs += (System.currentTimeMillis() - (timerStartedAt ?: System.currentTimeMillis()))
            timerStartedAt = null
        } else {
            timerStartedAt = System.currentTimeMillis()
        }
    }

    fun resetTimer() {
        timerStartedAt = null
        timerAccumMs = 0L
    }

    fun elapsedTimerMs(): Long =
        timerAccumMs + (timerStartedAt?.let { System.currentTimeMillis() - it } ?: 0L)

    // ---- Countdown --------------------------------------------------------------------------

    fun setCountdown(label: String, targetMillis: Long) {
        countdownLabel = label.ifBlank { "Countdown" }.take(24)
        countdownTarget = targetMillis
        prefs.countdownLabel = countdownLabel
        prefs.countdownTarget = targetMillis
    }

    companion object {
        const val MAX_DOCK = 5
    }
}
