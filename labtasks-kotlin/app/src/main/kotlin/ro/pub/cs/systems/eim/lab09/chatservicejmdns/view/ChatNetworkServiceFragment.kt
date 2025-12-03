package ro.pub.cs.systems.eim.lab09.chatservicejmdns.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Enumeration
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.R
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.controller.NetworkServiceAdapter
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.general.Constants
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.networkservicediscoveryoperations.NetworkServiceDiscoveryOperations

class ChatNetworkServiceFragment : Fragment() {

    private var servicePortEditText: EditText? = null

    private var serviceRegistrationStatusButton: Button? = null
    private var serviceDiscoveryStatusButton: Button? = null

    private var networkServiceDiscoveryOperations: NetworkServiceDiscoveryOperations? = null

    private var view: View? = null

    private var discoveredServicesAdapter: NetworkServiceAdapter? = null
    private var conversationsAdapter: NetworkServiceAdapter? = null

    private var chatActivity: ChatActivity? = null

    private val serviceRegistrationStatusButtonListener = object : View.OnClickListener {
        override fun onClick(view: View) {
            if (!chatActivity!!.getServiceRegistrationStatus()) {
                val port = servicePortEditText!!.text.toString()
                if (port.isEmpty()) {
                    Toast.makeText(activity, "Field service port should be filled!", Toast.LENGTH_LONG).show()
                    return
                }
                try {
                    networkServiceDiscoveryOperations!!.registerNetworkService(port.toInt())
                } catch (exception: Exception) {
                    Log.e(Constants.TAG, "Could not register network service: ${exception.message}")
                    if (Constants.DEBUG) {
                        exception.printStackTrace()
                    }
                    return
                }
                startServiceRegistration()
            } else {
                networkServiceDiscoveryOperations!!.unregisterNetworkService()
                stopServiceRegistration()
            }
            chatActivity!!.setServiceRegistrationStatus(!chatActivity!!.getServiceRegistrationStatus())
        }
    }

    private val serviceDiscoveryStatusButtonListener = object : View.OnClickListener {
        override fun onClick(view: View) {
            if (chatActivity == null)
                return

            if (!chatActivity!!.getServiceDiscoveryStatus()) {
                chatActivity!!.getNetworkServiceDiscoveryOperations().startNetworkServiceDiscovery()
                startServiceDiscovery()
            } else {
                networkServiceDiscoveryOperations!!.stopNetworkServiceDiscovery()
                stopServiceDiscovery()
            }
            chatActivity!!.setServiceDiscoveryStatus(!chatActivity!!.getServiceDiscoveryStatus())
        }
    }

    private fun getIPs(): String {
        return try {
            val interfaces: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            val IPb = StringBuilder()

            while (interfaces.hasMoreElements()) {
                val n = interfaces.nextElement()
                val ee: Enumeration<InetAddress> = n.inetAddresses

                while (ee.hasMoreElements()) {
                    val i = ee.nextElement()

                    if (i is Inet4Address) {
                        if (IPb.isNotEmpty())
                            IPb.append(", ")
                        IPb.append(i.hostAddress)
                    }
                }
            }

            IPb.toString()
        } catch (socketException: SocketException) {
            socketException.printStackTrace()
            ""
        }
    }

    @Suppress("UseRequireInsteadOfGet")
    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        Log.i(Constants.TAG, "ChatNetworkServiceFragment -> onCreateView() callback method was invoked")

        if (view == null)
            view = inflater.inflate(R.layout.fragment_chat_network_service, parent, false)

        val rootView = view!!

        val activity: FragmentActivity? = activity
        if (activity == null)
            return rootView

        chatActivity = activity as ChatActivity

        val LocalIPs: TextView = rootView.findViewById(R.id.service_discovery_local_addr)
        LocalIPs.text = getIPs()

        servicePortEditText = rootView.findViewById(R.id.port_edit_text)

        serviceRegistrationStatusButton = rootView.findViewById(R.id.service_registration_status_button)
        serviceRegistrationStatusButton!!.setOnClickListener(serviceRegistrationStatusButtonListener)

        serviceDiscoveryStatusButton = rootView.findViewById(R.id.service_discovery_status_button)
        serviceDiscoveryStatusButton!!.setOnClickListener(serviceDiscoveryStatusButtonListener)

        networkServiceDiscoveryOperations = chatActivity!!.getNetworkServiceDiscoveryOperations()

        val discoveredServicesListView: ListView = rootView.findViewById(R.id.discovered_services_list_view)
        discoveredServicesAdapter = NetworkServiceAdapter(chatActivity!!, chatActivity!!.getDiscoveredServices(), chatActivity!!.getNetworkServiceDiscoveryOperations())
        discoveredServicesListView.adapter = discoveredServicesAdapter

        val conversationsListView: ListView = rootView.findViewById(R.id.conversations_list_view)
        conversationsAdapter = NetworkServiceAdapter(chatActivity!!, chatActivity!!.getConversations(), chatActivity!!.getNetworkServiceDiscoveryOperations())
        conversationsListView.adapter = conversationsAdapter

        return rootView
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun startServiceRegistration() {
        serviceRegistrationStatusButton!!.setBackgroundColor(ContextCompat.getColor(chatActivity!!, R.color.colorGreen))
        serviceRegistrationStatusButton!!.text = resources.getString(R.string.unregister_service)
    }

    fun stopServiceRegistration() {
        serviceRegistrationStatusButton!!.setBackgroundColor(ContextCompat.getColor(chatActivity!!, R.color.colorRed))
        serviceRegistrationStatusButton!!.text = resources.getString(R.string.register_service)
    }

    fun startServiceDiscovery() {
        serviceDiscoveryStatusButton!!.setBackgroundColor(ContextCompat.getColor(chatActivity!!, R.color.colorGreen))
        serviceDiscoveryStatusButton!!.text = resources.getString(R.string.stop_service_discovery)
    }

    fun stopServiceDiscovery() {
        serviceDiscoveryStatusButton!!.setBackgroundColor(ContextCompat.getColor(chatActivity!!, R.color.colorRed))
        serviceDiscoveryStatusButton!!.text = resources.getString(R.string.start_service_discovery)
    }

    fun getDiscoveredServicesAdapter(): NetworkServiceAdapter {
        return discoveredServicesAdapter!!
    }

    fun getConversationsAdapter(): NetworkServiceAdapter {
        return conversationsAdapter!!
    }
}

