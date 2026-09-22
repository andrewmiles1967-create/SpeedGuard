package uk.co.speedguard.app

import android.location.Location

interface SpeedLimitProvider {
    fun limitFor(location: Location): Int?
}

/**
 * V0.1 test provider. Replace with D-TRO-backed provider once credentials/data are added.
 * Deliberately returns null: the app must never invent a legal speed limit.
 */
class DtroSpeedLimitProvider : SpeedLimitProvider {
    override fun limitFor(location: Location): Int? = null
}
