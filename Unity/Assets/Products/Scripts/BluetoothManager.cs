using UnityEngine;
using System.Collections.Generic;

public static class BluetoothManager
{
#if UNITY_ANDROID && !UNITY_EDITOR
    static AndroidJavaObject bluetoothPlugin;
#endif

    static AndroidJavaObject GetBluetoothPlugin()
    {
#if UNITY_ANDROID && !UNITY_EDITOR
        if (bluetoothPlugin == null)
        {
            using (AndroidJavaClass javaClass = new AndroidJavaClass("jp.kshoji.bluetoothmanagerplugin.BluetoothPlugin$Singleton"))
            {
                var bluetoothStaticInstance = javaClass.GetStatic<AndroidJavaObject>("INSTANCE");
                bluetoothPlugin = bluetoothStaticInstance.Call<AndroidJavaObject>("getInstance");
            }
        }
        return bluetoothPlugin;
#else
        return null;
#endif
    }

    public static void StartScanBluetooth()
    {
#if UNITY_ANDROID && !UNITY_EDITOR
        GetBluetoothPlugin().Call("startScanBluetooth");
#endif
    }

    public static List<string> GetDeviceAddresses()
    {
        var result = new List<string>();
#if UNITY_ANDROID && !UNITY_EDITOR
        var deviceCount = GetBluetoothPlugin().Call<int>("getDeviceCount");
        for (int i = 0; i < deviceCount; i++)
        {
            var deviceAddress = GetBluetoothPlugin().Call<string>("getDeviceAddress", i);
            if (deviceAddress != null && !result.Contains(deviceAddress))
            {
                result.Add(deviceAddress);
            }
        }
#endif
        return result;
    }

    public static void BondWithDeviceAddress(string deviceAddress)
    {
#if UNITY_ANDROID && !UNITY_EDITOR
        GetBluetoothPlugin().Call("bondWithDeviceAddress", deviceAddress);
#endif
    }
}