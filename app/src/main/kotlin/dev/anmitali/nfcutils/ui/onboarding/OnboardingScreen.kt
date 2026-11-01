// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.onboarding

import android.content.Intent
import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anmitali.nfcutils.AppContainer
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.nfc.NfcAvailability
import kotlinx.coroutines.launch

private const val STEP_COUNT = 3
private const val STEP_NFC = 1
private const val STEP_SUPPORT = 2

@Composable
fun OnboardingScreen(container: AppContainer) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    val availability by container.nfcStatus.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repositoryUrl = stringResource(R.string.onboarding_repository_url)
    val finish: () -> Unit = { scope.launch { container.settingsStore.setOnboardingDone() } }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier.widthIn(max = 480.dp).fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = stringResource(R.string.onboarding_step, step + 1, STEP_COUNT),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    if (step < STEP_SUPPORT) {
                        TextButton(onClick = finish) { Text(stringResource(R.string.onboarding_skip)) }
                    }
                }
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                ) {
                    when (step) {
                        0 -> StepContent(R.drawable.ic_tab_scan, R.string.onboarding_welcome_title, R.string.onboarding_welcome_body)
                        STEP_NFC -> NfcStep(availability)
                        else -> StepContent(R.drawable.ic_star, R.string.onboarding_support_title, R.string.onboarding_support_body) {
                            Button(
                                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, repositoryUrl.toUri())) },
                                modifier = Modifier.padding(top = 24.dp),
                            ) {
                                Text(stringResource(R.string.onboarding_open_repository))
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (step > 0) {
                        OutlinedButton(onClick = { step-- }) { Text(stringResource(R.string.action_back)) }
                    }
                    Button(
                        onClick = { if (step < STEP_SUPPORT) step++ else finish() },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(if (step < STEP_SUPPORT) R.string.onboarding_next else R.string.onboarding_finish))
                    }
                }
            }
        }
    }
}

@Composable
private fun NfcStep(availability: NfcAvailability) {
    val context = LocalContext.current
    val body = when (availability) {
        NfcAvailability.Enabled -> R.string.onboarding_nfc_enabled
        NfcAvailability.Disabled -> R.string.onboarding_nfc_disabled
        NfcAvailability.Unavailable -> R.string.onboarding_nfc_unavailable
    }
    StepContent(R.drawable.ic_device_tag, R.string.onboarding_nfc_title, body) {
        if (availability == NfcAvailability.Disabled) {
            Button(
                onClick = { context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS)) },
                modifier = Modifier.padding(top = 24.dp),
            ) {
                Text(stringResource(R.string.nfc_open_settings))
            }
        }
        Text(
            text = stringResource(R.string.onboarding_no_permissions),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun StepContent(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes body: Int,
    extra: @Composable () -> Unit = {},
) {
    Box(
        modifier = Modifier.size(80.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(44.dp),
        )
    }
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(top = 24.dp),
    )
    Text(
        text = stringResource(body),
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(top = 12.dp),
    )
    extra()
}
