package ro.pub.cs.systems.eim.lab09.chatservicejmdns.view

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.util.ArrayList
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.R
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.general.Constants
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.model.NetworkService
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.networkservicediscoveryoperations.NetworkServiceDiscoveryOperations

class ChatActivity : AppCompatActivity() {

    private var networkServiceDiscoveryOperations: NetworkServiceDiscoveryOperations? = null

    private var serviceRegistrationStatus = false
    private var serviceDiscoveryStatus = false

    private var discoveredServices: ArrayList<NetworkService>? = null
    private var conversations: ArrayList<NetworkService>? = null

    private var handler: Handler? = null

    private var multicastLock: WifiManager.MulticastLock? = null
    private var wifiManager: WifiManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(Constants.TAG, "onCreate() callback method was invoked!")
        setContentView(R.layout.activity_chat)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        multicastLock = wifiManager!!.createMulticastLock(Constants.TAG)
        multicastLock!!.setReferenceCounted(true)
        multicastLock!!.acquire()

        discoveredServices = ArrayList()
        conversations = ArrayList()

        setHandler(Handler())
        setNetworkServiceDiscoveryOperations(NetworkServiceDiscoveryOperations(this))
        setChatNetworkServiceFragment(ChatNetworkServiceFragment())
    }

    override fun onResume() {
        super.onResume()
        Log.i(Constants.TAG, "onResume() callback method was invoked!")
        if (networkServiceDiscoveryOperations != null) {
            if (serviceDiscoveryStatus) {
                networkServiceDiscoveryOperations!!.startNetworkServiceDiscovery()
                getChatNetworkServiceFragment()?.startServiceDiscovery()
            }
        }
    }

    override fun onPause() {
        Log.i(Constants.TAG, "onPause() callback method was invoked!")
        if (networkServiceDiscoveryOperations != null) {
            if (serviceDiscoveryStatus) {
                networkServiceDiscoveryOperations!!.stopNetworkServiceDiscovery()
                getChatNetworkServiceFragment()?.stopServiceDiscovery()
            }
        }
        super.onPause()
    }

    override fun onDestroy() {
        Log.i(Constants.TAG, "onDestroy() callback method was invoked!")
        if (networkServiceDiscoveryOperations != null) {
            if (serviceDiscoveryStatus) {
                serviceDiscoveryStatus = false
                networkServiceDiscoveryOperations!!.stopNetworkServiceDiscovery()
                getChatNetworkServiceFragment()?.stopServiceDiscovery()
            }
            if (serviceRegistrationStatus) {
                serviceDiscoveryStatus = false
                networkServiceDiscoveryOperations!!.unregisterNetworkService()
                getChatNetworkServiceFragment()?.stopServiceRegistration()
            }
            networkServiceDiscoveryOperations!!.closeJmDNS()
        }

        multicastLock?.release()

        super.onDestroy()
    }

    fun setHandler(handler: Handler) {
        this.handler = handler
    }

    fun getHandler(): Handler {
        return handler!!
    }

    fun setNetworkServiceDiscoveryOperations(networkServiceDiscoveryOperations: NetworkServiceDiscoveryOperations) {
        this.networkServiceDiscoveryOperations = networkServiceDiscoveryOperations
    }

    fun getNetworkServiceDiscoveryOperations(): NetworkServiceDiscoveryOperations {
        return networkServiceDiscoveryOperations!!
    }

    fun setChatNetworkServiceFragment(chatNetworkServiceFragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .add(R.id.container_frame_layout, chatNetworkServiceFragment, Constants.FRAGMENT_TAG)
            .commit()
    }

    fun getChatNetworkServiceFragment(): ChatNetworkServiceFragment? {
        val fragment = supportFragmentManager
            .findFragmentByTag(Constants.FRAGMENT_TAG)
        return if (fragment is ChatNetworkServiceFragment) {
            fragment
        } else {
            null
        }
    }

    fun setServiceRegistrationStatus(serviceRegistrationStatus: Boolean) {
        this.serviceRegistrationStatus = serviceRegistrationStatus
    }

    fun getServiceRegistrationStatus(): Boolean {
        return serviceRegistrationStatus
    }

    fun setServiceDiscoveryStatus(serviceDiscoveryStatus: Boolean) {
        this.serviceDiscoveryStatus = serviceDiscoveryStatus
    }

    fun getServiceDiscoveryStatus(): Boolean {
        return serviceDiscoveryStatus
    }

    fun setDiscoveredServices(discoveredServices: ArrayList<NetworkService>) {
        this.discoveredServices = discoveredServices
        handler!!.post {
            val chatNetworkServiceFragment = getChatNetworkServiceFragment()
            if (chatNetworkServiceFragment != null && chatNetworkServiceFragment.isVisible) {
                chatNetworkServiceFragment.getDiscoveredServicesAdapter().setData(discoveredServices)
                chatNetworkServiceFragment.getDiscoveredServicesAdapter().notifyDataSetChanged()
            }
        }
    }

    fun getDiscoveredServices(): ArrayList<NetworkService> {
        return discoveredServices!!
    }

    fun setConversations(conversations: ArrayList<NetworkService>) {
        this.conversations = conversations
        handler!!.post {
            val chatNetworkServiceFragment = getChatNetworkServiceFragment()
            if (chatNetworkServiceFragment != null && chatNetworkServiceFragment.isVisible) {
                chatNetworkServiceFragment.getConversationsAdapter().setData(conversations)
                chatNetworkServiceFragment.getConversationsAdapter().notifyDataSetChanged()
            }
        }
    }

    fun getConversations(): ArrayList<NetworkService> {
        return conversations!!
    }

    fun getWifiManager(): WifiManager {
        return wifiManager!!
    }
}

