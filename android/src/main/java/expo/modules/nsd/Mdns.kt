package expo.modules.nsd

import android.annotation.SuppressLint
import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress

data class Service(
        val address: String,
        val port: Int,
        val name: String
)

class Mdns(context: Context, private val serviceType: String, onServiceDiscovery: (Service) -> Unit) {

    private val nsdManager: NsdManager =
            context.getSystemService(Context.NSD_SERVICE) as NsdManager

    private val discoveryListener = object : NsdManager.DiscoveryListener {
        @SuppressLint("LongLogTag")
        override fun onDiscoveryStarted(regType: String) {
            Log.d("Mdns.discoveryListener.onDiscoveryStarted", "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            CoroutineScope(Dispatchers.Default).launch {
                resolveService(service, onServiceDiscovery)
            }
        }

        @SuppressLint("LongLogTag")
        override fun onServiceLost(service: NsdServiceInfo) {
            Log.e("Mdns.discoveryListener.onServiceLost", "service lost: $service")
        }

        @SuppressLint("LongLogTag")
        override fun onDiscoveryStopped(serviceType: String) {
            Log.i("Mdns.discoveryListener.onDiscoveryStopped", "Discovery stopped: $serviceType")
        }

        @SuppressLint("LongLogTag")
        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(
                    "Mdns.discoveryListener.onStartDiscoveryFailed",
                    "Discovery failed: Error code:$errorCode"
            )
            nsdManager.stopServiceDiscovery(this)
        }

        @SuppressLint("LongLogTag")
        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(
                    "Mdns.onDiscoveryStopped.onStopDiscoveryFailed",
                    "Discovery failed: Error code:$errorCode"
            )
            nsdManager.stopServiceDiscovery(this)
        }
    }

    private suspend fun resolveService(
            serviceInfo: NsdServiceInfo,
            onServiceDiscovery: (Service) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                @SuppressLint("LongLogTag")
                override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    // Called when resolving the service fails
                    Log.d(
                            "Mdns.resolveService.onResolveFailed",
                            "Couldn't resolve service: ${serviceInfo.serviceName}"
                    )
                }

                @SuppressLint("LongLogTag")
                override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                    val port: Int = serviceInfo.port
                    val host: InetAddress = serviceInfo.host
                    val name = serviceInfo.serviceName
                    Log.d(
                            "Mdns.resolveService.onServiceResolved",
                            "$name, Puerto: $port, Host: ${host.hostAddress}"
                    )

                    if (host.hostAddress != null) {
                        onServiceDiscovery(
                                Service(
                                        port = port,
                                        address = host.hostAddress!!,
                                        name = name
                                )
                        )
                    }
                }
            })
        }
    }

    fun startDiscovery() {
        nsdManager.discoverServices(
                serviceType,
                NsdManager.PROTOCOL_DNS_SD,
                discoveryListener
        )
    }

    fun stopDiscovery() {
        nsdManager.stopServiceDiscovery(discoveryListener)
    }
}