# WifiP2p-Miracast
A demo of implementing Wifi-Display sink based on wifiP2p.
Based on WifiP2p, it realizes the signal discovery function of the sink side of wifi-display. Due to the existence of @hide related classes (WifiP2pWfdInfo ) and related functions (setWFDInfo), reflection calls are needed to wake up the searched function on the Wifi-display sink side. When setWFDInfo is called successfully, this device can be searched from other Miracast devices.
