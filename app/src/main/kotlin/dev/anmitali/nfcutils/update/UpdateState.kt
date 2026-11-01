// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.update

enum class UpdateFailure { Network, Corrupted, Rejected }

sealed interface UpdateState {
    data object Idle : UpdateState

    data object Checking : UpdateState

    data object UpToDate : UpdateState

    data object CheckFailed : UpdateState

    class Available(val release: ReleaseInfo) : UpdateState

    class NeedsPermission(val release: ReleaseInfo) : UpdateState

    class Downloading(val release: ReleaseInfo, val progress: Float) : UpdateState

    class Installing(val release: ReleaseInfo) : UpdateState

    class Failed(val release: ReleaseInfo, val failure: UpdateFailure) : UpdateState
}
