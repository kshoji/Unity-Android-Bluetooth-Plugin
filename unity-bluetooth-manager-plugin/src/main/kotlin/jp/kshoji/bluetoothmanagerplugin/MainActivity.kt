package jp.kshoji.bluetoothmanagerplugin

import android.os.Bundle

class MainActivity : BluetoothPlugin.RequestPermissionsResultCallbackActivity() {
    override fun onCreate(p0: Bundle?) {
        super.onCreate(p0)
        BluetoothPlugin.Singleton.instance = BluetoothPlugin(this)
    }
}