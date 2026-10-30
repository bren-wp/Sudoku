package com.brendigo.sudoku

import android.os.Bundle
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.sudoku.data.Screen
import com.brendigo.sudoku.data.SudokuController
import com.brendigo.sudoku.ui.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var sudoku: SudokuController

    override fun onStop() {
        sudoku.flush()
        super.onStop()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enableImmersiveMode()
    }

    @Suppress("DEPRECATION")
    private fun enableImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Restore the currently visible interactive screen, not only New Game.
        // Settings must also retain its origin or Back may abandon the game.
        outState.putString("sudoku.active_screen", sudoku.page.name)
        outState.putBoolean("sudoku.settings_from_game", sudoku.settingsOpenedFromGame())
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sudoku = SudokuController(applicationContext)
        when (savedInstanceState?.getString("sudoku.active_screen")) {
            Screen.NEW_GAME.name -> sudoku.go(Screen.NEW_GAME)
            Screen.GAME.name -> if (sudoku.game != null) sudoku.go(Screen.GAME)
            Screen.SETTINGS.name -> {
                if (savedInstanceState.getBoolean("sudoku.settings_from_game") &&
                    sudoku.game != null) {
                    sudoku.go(Screen.GAME)
                }
                sudoku.go(Screen.SETTINGS)
            }
            else -> Unit
        }
        setContent {
            SudokuTheme(sudoku.options.dark) { Application(sudoku) }
        }
        // On Android 15+ the WindowInsetsController must not be queried before
        // DecorView is attached. Apply full-screen mode once UI is installed.
        window.decorView.post { enableImmersiveMode() }
    }
}

@Composable
private fun Application(controller: SudokuController) {
    LaunchedEffect(Unit) {
        delay(850)
        if (controller.page == Screen.SPLASH) controller.go(Screen.HOME)
    }
    BackHandler(enabled = controller.page != Screen.HOME && controller.page != Screen.SPLASH) {
        controller.back()
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    var inForeground by remember { mutableStateOf(
        lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    ) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> inForeground = true
                Lifecycle.Event.ON_STOP -> inForeground = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(controller.toast) {
        if (controller.toast.isNotBlank()) {
            delay(3500)
            controller.clearToast()
        }
    }
    LaunchedEffect(controller.page, controller.game?.paused, controller.game?.completed, controller.game?.failed, inForeground) {
        while (inForeground && controller.page == Screen.GAME &&
            controller.game?.paused != true && controller.game?.completed != true &&
            controller.game?.failed != true) {
            delay(1000)
            controller.tick()
        }
    }
    Box(Modifier.fillMaxSize()) {
        ScenicBackdrop(Modifier.fillMaxSize(), bright = !controller.options.dark,
            splash = controller.page == Screen.SPLASH,
            home = controller.page == Screen.HOME)
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            SudokuBackupFlow(controller) { onExportBackup, onImportBackup ->
                when (controller.page) {
                    Screen.SPLASH -> SplashPage()
                    Screen.HOME -> HomePage(controller)
                    Screen.NEW_GAME -> NewGamePage(controller)
                    Screen.GAME -> GamePage(controller)
                    Screen.DAILY -> DailyPage(controller)
                    Screen.STATS -> StatsPage(controller)
                    Screen.ACHIEVEMENTS -> AchievementsPage(controller)
                    Screen.SETTINGS -> SettingsPage(
                        controller, onExportBackup, onImportBackup
                    )
                }
            }
        }
        if (controller.toast.isNotBlank()) {
            Box(Modifier.fillMaxSize().navigationBarsPadding().padding(16.dp),
                contentAlignment = androidx.compose.ui.Alignment.BottomCenter) {
                androidx.compose.material3.Snackbar {
                    Text(controller.toast, color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}
