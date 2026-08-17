package com.example.data.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Monitors device network connectivity status safely and provides an observable StateFlow.
 */
class NetworkMonitor(context: Context, private val coroutineScope: CoroutineScope) {
  private val connectivityManager = try {
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
  } catch (e: Throwable) {
    null
  }

  private val _isOnline = MutableStateFlow(true)
  val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

  private val networkCallback = object : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
      coroutineScope.launch(Dispatchers.Main) {
        _isOnline.value = true
      }
    }

    override fun onLost(network: Network) {
      coroutineScope.launch(Dispatchers.Main) {
        _isOnline.value = checkCurrentConnectivity()
      }
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
      val hasInternet = try {
        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
          networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
      } catch (e: Throwable) {
        true
      }
      coroutineScope.launch(Dispatchers.Main) {
        _isOnline.value = hasInternet
      }
    }
  }

  init {
    try {
      _isOnline.value = checkCurrentConnectivity()
      val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()
      connectivityManager?.registerNetworkCallback(request, networkCallback)
    } catch (e: Throwable) {
      Log.w("NetworkMonitor", "Network callback registration skipped: ${e.message}")
      _isOnline.value = true
    }
  }

  private fun checkCurrentConnectivity(): Boolean {
    return try {
      val cm = connectivityManager ?: return true
      val activeNetwork = cm.activeNetwork ?: return true
      val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return true
      capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } catch (e: Throwable) {
      true
    }
  }
}
