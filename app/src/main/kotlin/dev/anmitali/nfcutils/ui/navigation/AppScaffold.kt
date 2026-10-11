// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import dev.anmitali.nfcutils.AppContainer
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.ui.common.NfcStatusBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(container: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val currentTab = Tab.entries.firstOrNull { it.route == route }
    val availability by container.nfcStatus.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val onTab by rememberUpdatedState(currentTab != null)

    LaunchedEffect(Unit) {
        container.session.scanEvents.collect {
            if (onTab) navigateToTab(navController, Tab.Scan)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(titleFor(route))) },
                navigationIcon = {
                    if (currentTab == null) {
                        TextButton(onClick = { navController.popBackStack() }) { Text(stringResource(R.string.action_back)) }
                    }
                },
                actions = {
                    if (currentTab != null) {
                        TextButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                            Text(stringResource(R.string.screen_settings))
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (currentTab != null) {
                NavigationBar {
                    for (tab in Tab.entries) {
                        NavigationBarItem(
                            selected = tab == currentTab,
                            onClick = { navigateToTab(navController, tab) },
                            icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                            label = { Text(stringResource(tab.label)) },
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (currentTab != null) {
                NfcStatusBanner(availability, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
            AppNavHost(
                container = container,
                navController = navController,
                snackbarHostState = snackbarHostState,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun navigateToTab(navController: NavHostController, tab: Tab) {
    navController.navigate(tab.route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
