package com.cshawn.startsound

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 *
 * @author: C.Shawn
 * @date: 2022/11/20 13:31
 */
const val pathKey: String = "path"
const val spName: String = "audio"

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.getSharedPreferences(spName, Context.MODE_PRIVATE)?.getString(pathKey, null).also {
            startPlay(context!!, it)
        }
    }
}