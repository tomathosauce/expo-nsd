package expo.modules.nsd

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class ExpoNsdModule : Module() {
    private var mdns: Mdns? = null

    private val context
        get() = requireNotNull(appContext.reactContext)

    private val activity
        get() = requireNotNull(appContext.currentActivity)

    private val requestCode = 0
    override fun definition() = ModuleDefinition {
        Name("ExpoNsd")

        Events("onServiceDiscovered")

        Function("requestPermissions") {
            if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_NETWORK_STATE
                    ) == PackageManager.PERMISSION_GRANTED) {
            } else {
                requestPermissions(activity,
                        arrayOf(Manifest.permission.ACCESS_NETWORK_STATE
                        ),
                        requestCode
                )
            }
        }

        Function("configureDiscovery") { serviceType: String ->
            mdns = Mdns(context, serviceType) {
                this@ExpoNsdModule.sendEvent("onServiceDiscovered",
                        bundleOf(
                                "address" to it.address,
                                "port" to it.port,
                                "name" to it.name
                        ))
            }
        }

        Function("startDiscovery") {
            mdns?.startDiscovery()
        }

        Function("stopDiscovery") {
            mdns?.stopDiscovery()
        }
    }
}
