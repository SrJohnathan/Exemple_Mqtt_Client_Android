package br.com.atm.terminal.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class Beoaquest: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val startServiceIntent = Intent(context, Mqtt::class.java)
        context!!.startService(startServiceIntent)
    }


}