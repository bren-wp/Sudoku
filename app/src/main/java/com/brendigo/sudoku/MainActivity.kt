package com.brendigo.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.sudoku.data.Screen
import com.brendigo.sudoku.data.SudokuController
import com.brendigo.sudoku.ui.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(2, 11, 39)
        window.navigationBarColor = android.graphics.Color.rgb(2, 11, 39)
        setContent {
            val controller = remember { SudokuController(applicationContext) }
            SudokuTheme(controller.options.dark) { Application(controller) }
        }
    }
}

@Composable
private fun Application(controller: SudokuController) {
    BackHandler(enabled = controller.page != Screen.HOME) {
        controller.go(Screen.HOME)
    }
    LaunchedEffect(controller.page, controller.game?.paused, controller.game?.completed, controller.game?.failed) {
        while (controller.page == Screen.GAME &&
            controller.game?.paused != true && controller.game?.completed != true &&
            controller.game?.failed != true) {
            delay(1000)
            controller.tick()
        }
    }
    Box(Modifier.fillMaxSize()) {
        ScenicBackdrop(Modifier.fillMaxSize(), bright = !controller.options.dark)
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            when (controller.page) {
                Screen.HOME -> HomePage(controller)
                Screen.NEW_GAME -> NewGamePage(controller)
                Screen.GAME -> GamePage(controller)
                Screen.DAILY -> DailyPage(controller)
                Screen.STATS -> StatsPage(controller)
                Screen.ACHIEVEMENTS -> AchievementsPage(controller)
                Screen.SETTINGS -> SettingsPage(controller)
            }
        }
        if (controller.toast.isNotBlank()) {
            Text(controller.toast, color = Color.White, fontSize = 14.sp,
                modifier = Modifier.padding(22.dp))
        }
    }
}
