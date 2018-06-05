# Unity-Android-Bluetooth-Plugin
[Work in progress] Bluetooth administration plugin for Unity Android

Currently, this plugin can just **paring** with Bluetooth devices.

### Add plugin to Unity project

`.unitypackage` file placed on [Releases](https://github.com/kshoji/Unity-Android-Bluetooth-Plugin/releases).

Import the package to your Unity project.

### Use plugin from Unity C#

#### Use the BluetoothManager utility methods

Starts to scan devices:
```cs
BluetoothManager.StartScanBluetooth();
```

Obtains found devices' addresses
```cs
List<string> addresses = BluetoothManager.GetDeviceAddresses();
```

Pair with a device (Android native UI may be appeared)
```cs
// Use bluetooth address obtained by GetDeviceAddresses()
BluetoothManager.BondWithDeviceAddress(addresses[0]);
```
