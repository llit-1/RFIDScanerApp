package com.example.rfidscanner

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.widget.Toast
import com.cf.beans.CmdData
import com.cf.ble.interfaces.IBleDisConnectCallback
import com.cf.ble.interfaces.IConnectDoneCallback
import com.cf.ble.interfaces.IOnNotifyCallback
import com.cf.zsdk.BleCore
import com.cf.zsdk.CfSdk
import java.util.UUID

object BleManager {
    private const val targetMac = "E0:4E:7A:F3:77:BB"
    private val SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
    private val NOTIFY_UUID = UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb")

    private lateinit var bleCore: BleCore
    private var isInitialized = false
    private var isConnected = false
    private var isNotifyEnabled = false

    private var disconnectedListener: (() -> Unit)? = null

    private val notifyCallbacks = mutableSetOf<IOnNotifyCallback>()

    fun init(context: Context) {
        if (!isInitialized) {
            bleCore = CfSdk.get(BleCore::class.java) as BleCore
            bleCore.init(context.applicationContext)
            isInitialized = true
        }
    }



    fun setOnDisconnectedListener(listener: () -> Unit) {
        disconnectedListener = listener
    }

    fun connect(
        context: Context,
        onConnectionResult: (Boolean) -> Unit
    ) {
        if (!isInitialized) throw IllegalStateException("BleManager not initialized")

        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (btAdapter == null || !btAdapter.isEnabled) {
            Toast.makeText(context, "Включите Bluetooth", Toast.LENGTH_SHORT).show()
            onConnectionResult(false)
            return
        }

        val device = btAdapter.getRemoteDevice(targetMac)
        bleCore.connectDevice(device, context, false)

        bleCore.setIConnectDoneCallback(object : IConnectDoneCallback {
            override fun onConnectDone(success: Boolean) {
                isConnected = success
                if (success) {
                    isNotifyEnabled = bleCore.setNotifyState(SERVICE_UUID, NOTIFY_UUID, true, internalNotifyCallback)
                }
                onConnectionResult(success)
            }
        })

        bleCore.setIBleDisConnectCallback(IBleDisConnectCallback {
            isConnected = false
            isNotifyEnabled = false
            disconnectedListener?.invoke()
        })
    }

    fun disconnect() {
        if (isNotifyEnabled) {
            bleCore.setNotifyState(SERVICE_UUID, NOTIFY_UUID, false, null)
            isNotifyEnabled = false
        }
        if (isConnected) {
            bleCore.disconnectedDevice()
            isConnected = false
        }
    }

    private val internalNotifyCallback = object : IOnNotifyCallback {
        override fun onNotify(cmdType: Int, cmdData: CmdData?) {}

        override fun onNotify(data: ByteArray?) {
            data ?: return
            for (cb in notifyCallbacks) {
                cb.onNotify(data)
            }
        }
    }

    fun registerNotifyCallback(callback: IOnNotifyCallback) {
        notifyCallbacks.add(callback)
    }

    fun unregisterNotifyCallback(callback: IOnNotifyCallback) {
        notifyCallbacks.remove(callback)
    }

    fun isConnected(): Boolean = isConnected
    fun isNotifyEnabled(): Boolean = isNotifyEnabled
}