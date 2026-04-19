package com.versicode.bepresent.notifications

import com.versicode.bepresent.R

enum class NotificationSound {
    A_SHARP_BOWL, D_SHARP_BOWL, BEE_PRESENT;

    fun toRawResId(): Int = when (this) {
        A_SHARP_BOWL -> R.raw.a_sharp_bowl
        D_SHARP_BOWL -> R.raw.d_sharp_bowl
        BEE_PRESENT -> R.raw.bee_present
    }
}

