package jp.kshoji.bluetoothmanagerplugin

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.unity3d.player.UnityPlayerActivity

/**
 * Unity Bluetooth Manager Plugin implementation
 */
class BluetoothPlugin(activity_: RequestPermissionsResultCallbackActivity) {
    private var activity: RequestPermissionsResultCallbackActivity = activity_

    companion object {
        const val TAG = "BluetoothPlugin"
    }

    object Singleton {
        lateinit var instance: BluetoothPlugin
    }

    private var bluetoothReceiver: BluetoothReceiver? = null
    var foundBlueoothDevices = arrayListOf<BluetoothDevice>()

    /**
     * Starts Bluetooth device scanning
     */
    fun startScanBluetooth() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1000)
            return
        }

        // register intent action receivers
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        bluetoothReceiver = BluetoothReceiver()
        activity.registerReceiver(bluetoothReceiver, filter)

        // stopScanBluetooth
        val bluetoothManager = activity.getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter!!.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }

        bluetoothAdapter.startDiscovery()
    }

    /**
     * Stops Bluetooth device scanning
     * Called by Unity
     */
    @Suppress("unused")
    fun stopScanBluetooth() {
        val bluetoothManager = activity.getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter!!.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
    }

    /**
     * Checks if Bluetooth device scanning
     * @return true if scanning Bluetooth devices
     * Called by Unity
     */
    @Suppress("unused")
    fun isDiscovering(): Boolean {
        val bluetoothManager = activity.getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        return bluetoothAdapter!!.isDiscovering
    }

    /**
     * Get found Bluetooth device count
     * Called by Unity
     */
    @Suppress("unused")
    fun getDeviceCount(): Int = foundBlueoothDevices.size

    /**
     * Get Bluetooth device address
     * @param index Bluetooth device index
     * @return Bluetooth device address; null if index out of bounds
     * Called by Unity
     */
    @Suppress("unused")
    fun getDeviceAddress(index: Int): String? {
        try {
            return foundBlueoothDevices[index].address
        } catch (ex: ArrayIndexOutOfBoundsException) {
            Log.e(TAG, ex.message, ex)
        }
        return null
    }

    /**
     * Get Bluetooth device name
     * @param address Bluetooth device address
     * @return Bluetooth device name; null if no name device
     * Called by Unity
     */
    @Suppress("unused")
    fun getDeviceNameByAddress(address: String): String? {
        return foundBlueoothDevices.find { it.address == address }?.name
    }

    /**
     * Pair with device address
     * This method executed asynchronous, returns immediately.
     * This method will display Android System UI, when the device requests PIN code / user Action while paring.
     * @param address Bluetooth device address
     * Called by Unity
     */
    @Suppress("unused")
    fun bondWithDeviceAddress(address: String) {
        foundBlueoothDevices.find { it.address == address }?.createBond()
    }

    /**
     * Intent receiver while Bluetooth scanning / paring
     */
    inner class BluetoothReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            // When discovery finds a device
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device.bondState != BluetoothDevice.BOND_BONDED) {
                        foundBlueoothDevices.add(device)
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    if (foundBlueoothDevices.count() == 0) {
                        Log.d(TAG, "found NOTHING!")
                    } else {
                        Log.d(TAG, "${foundBlueoothDevices.count()} devices found.")
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0)
                    when (bondState) {
                        BluetoothDevice.BOND_BONDED -> {
                            val paired = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                            Log.d(TAG, "paired with ${paired.address}")
                        }
                        else -> {
                            Log.d(TAG, "bondState: $bondState")
                        }
                    }
                }
                else -> {
                    Log.d(TAG, "invalid action: $action")
                }
            }
        }
    }

    /**
     * UnityPlayerActivity with Android permission granting feature
     * this implementation premises the app requests only ONE permission.
     */
    open class RequestPermissionsResultCallbackActivity : UnityPlayerActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            when (requestCode) {
                1000 -> when (grantResults.size == 1) {
                    true -> {
                        when (grantResults[0]) {
                            PackageManager.PERMISSION_GRANTED -> {
                                Singleton.instance.startScanBluetooth()
                            }
                            PackageManager.PERMISSION_DENIED -> {
                                Log.d(TAG, "PERMISSION_DENIED");
                            }
                            else -> {
                                Log.d(TAG, "grantResults: ${grantResults[0]}");
                            }
                        }
                    }
                }
            }
        }

        override fun onDestroy() {
            if (Singleton.instance.bluetoothReceiver != null) {
                unregisterReceiver(Singleton.instance.bluetoothReceiver!!)
            }
            super.onDestroy()
        }
    }
}
