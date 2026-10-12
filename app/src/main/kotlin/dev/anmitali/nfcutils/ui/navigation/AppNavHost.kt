// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.anmitali.nfcutils.AppContainer
import dev.anmitali.nfcutils.ui.history.HistoryDetailScreen
import dev.anmitali.nfcutils.ui.history.HistoryScreen
import dev.anmitali.nfcutils.ui.saved.SavedScreen
import dev.anmitali.nfcutils.ui.saved.SetEditorScreen
import dev.anmitali.nfcutils.ui.scan.ScanScreen
import dev.anmitali.nfcutils.ui.settings.AboutScreen
import dev.anmitali.nfcutils.ui.settings.NoticesScreen
import dev.anmitali.nfcutils.ui.settings.SettingsScreen
import dev.anmitali.nfcutils.ui.tools.ToolsScreen
import dev.anmitali.nfcutils.ui.write.WriteScreen

@Composable
fun AppNavHost(
    container: AppContainer,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onOpenEditor: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val idArgument = listOf(navArgument(Routes.ARG_ID) { type = NavType.LongType })
    NavHost(navController = navController, startDestination = Routes.SCAN, modifier = modifier) {
        composable(Routes.SCAN) { ScanScreen(container, onOpenEditor) }
        composable(Routes.WRITE) { WriteScreen(container, snackbarHostState) }
        composable(Routes.TOOLS) { ToolsScreen(container) }
        composable(Routes.HISTORY) {
            HistoryScreen(container, onOpen = { navController.navigate(Routes.historyDetail(it)) })
        }
        composable(Routes.SAVED) {
            SavedScreen(container, onEdit = { navController.navigate(Routes.setEditor(it)) })
        }
        composable(Routes.HISTORY_DETAIL, arguments = idArgument) { entry ->
            HistoryDetailScreen(
                container,
                id = entry.arguments?.getLong(Routes.ARG_ID) ?: 0L,
                onClose = { navController.popBackStack() },
            )
        }
        composable(Routes.SET_EDITOR, arguments = idArgument) { entry ->
            SetEditorScreen(
                container,
                id = entry.arguments?.getLong(Routes.ARG_ID) ?: 0L,
                onClose = { navController.popBackStack() },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                container,
                onOpenAbout = { navController.navigate(Routes.ABOUT) },
                onOpenNotices = { navController.navigate(Routes.NOTICES) },
            )
        }
        composable(Routes.ABOUT) { AboutScreen() }
        composable(Routes.NOTICES) { NoticesScreen() }
    }
}
