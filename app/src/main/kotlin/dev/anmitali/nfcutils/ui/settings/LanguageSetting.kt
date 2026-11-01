// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.settings

import android.app.LocaleManager
import android.os.LocaleList
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.anmitali.nfcutils.R

private class LanguageOption(val tag: String, @StringRes val label: Int)

private val LanguageOptions = listOf(
    LanguageOption("", R.string.settings_language_system),
    LanguageOption("en", R.string.language_english),
    LanguageOption("ru", R.string.language_russian),
    LanguageOption("kk", R.string.language_kazakh),
)

@RequiresApi(33)
@Composable
fun LanguageSetting() {
    val localeManager = LocalContext.current.getSystemService(LocaleManager::class.java)
    var selected by remember {
        mutableStateOf(localeManager.applicationLocales.takeIf { !it.isEmpty }?.get(0)?.language.orEmpty())
    }
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(text = stringResource(R.string.settings_language), style = MaterialTheme.typography.titleMedium)
        for (option in LanguageOptions) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selected == option.tag,
                        onClick = {
                            selected = option.tag
                            localeManager.applicationLocales =
                                if (option.tag.isEmpty()) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(option.tag)
                        },
                        role = Role.RadioButton,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = selected == option.tag, onClick = null)
                Text(
                    text = stringResource(option.label),
                    modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
                )
            }
        }
    }
}
