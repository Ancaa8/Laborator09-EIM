package ro.pub.cs.systems.eim.lab09.chatservicejmdns.networkservicediscoveryoperations

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.nio.ByteBuffer
import java.util.ArrayList
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo
import javax.jmdns.ServiceListener
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.general.Constants
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.general.Utilities
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.model.NetworkService
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.view.ChatActivity

class NetworkServiceDiscoveryOperations(context: Context) {
    private val chatActivity: ChatActivity = context as ChatActivity

    private var serviceName: String? = null

    private var chatServer: ChatServer? = null
    private val communicationToServers: MutableList<ChatClient> = ArrayList()
    private var communicationFromClients: MutableList<ChatClient> = ArrayList()

    private var jmDNS: JmDNS? = null
    private val serviceListener: ServiceListener

    init {
        Thread {
            try {
                val wifiManager = (context as ChatActivity).getWifiManager()
                val address = InetAddress.getByAddress(
                    ByteBuffer.allocate(4).putInt(Integer.reverseBytes(wifiManager.connectionInfo.ipAddress)).array()
                )

                val name = address.hostName
                Log.i(Constants.TAG, "address = $address name = $name")
                jmDNS = JmDNS.create(address, name)
            } catch (ioException: IOException) {
                Log.e(Constants.TAG, "An exception has occurred: ${ioException.message}")
                if (Constants.DEBUG) {
                    ioException.printStackTrace()
                }
            }
        }.start()

        serviceListener = object : ServiceListener {
            override fun serviceAdded(serviceEvent: ServiceEvent) {
                Log.i(Constants.TAG, "Service found: $serviceEvent")
                if (serviceEvent.type != Constants.SERVICE_TYPE) {
                    Log.i(Constants.TAG, "Unknown service type: ${serviceEvent.type}")
                } else if (serviceEvent.name == serviceName) {
                    Log.i(Constants.TAG, "The service running on the same machine has been discovered: $serviceName")
                } else if (serviceEvent.name.contains(Constants.SERVICE_NAME)) {
                    Log.i(Constants.TAG, "The service name should be resolved now: $serviceName")
                    jmDNS!!.requestServiceInfo(serviceEvent.type, serviceEvent.name)
                }
            }

            override fun serviceRemoved(serviceEvent: ServiceEvent) {
                Log.i(Constants.TAG, "Service lost: $serviceEvent")

                val serviceInfo = serviceEvent.info
                if (serviceInfo == null) {
                    Log.e(Constants.TAG, "Service info for service is null!")
                    return
                }

                val hosts = serviceInfo.hostAddresses
                var host: String? = null
                if (hosts.isNotEmpty()) {
                    host = hosts[0]
                    if (host!!.startsWith("/")) {
                        host = host.substring(1)
                    }
                }

                val finalizedHost = host
                val finalizedPort = serviceInfo.port

                val handler: Handler = chatActivity.getHandler()
                handler.post {
                    val discoveredServices = chatActivity.getDiscoveredServices()
                    val networkService = NetworkService(serviceEvent.name, finalizedHost, finalizedPort, -1)

                    if (discoveredServices.contains(networkService)) {
                        val position = discoveredServices.indexOf(networkService)
                        discoveredServices.removeAt(position)
                        communicationToServers.removeAt(position)
                        chatActivity.setDiscoveredServices(discoveredServices)
                    }
                }
            }

            override fun serviceResolved(serviceEvent: ServiceEvent) {
                Log.i(Constants.TAG, "Resolve succeeded: $serviceEvent")

                if (serviceEvent.name == serviceName) {
                    Log.i(Constants.TAG, "The service running on the same machine has been discovered.")
                    return
                }

                val serviceInfo = serviceEvent.info
                if (serviceInfo == null) {
                    Log.e(Constants.TAG, "Service info for service is null!")
                    return
                }

                val hosts = serviceInfo.hostAddresses
                if (hosts.isEmpty()) {
                    Log.e(Constants.TAG, "No host addresses returned for the service!")
                    return
                }
                var host = hosts[0]
                if (host.startsWith("/")) {
                    host = host.substring(1)
                }
                val port = serviceInfo.port

                val discoveredServices = chatActivity.getDiscoveredServices()
                val networkService = NetworkService(serviceEvent.name, host, port, Constants.CONVERSATION_TO_SERVER)
                if (!discoveredServices.contains(networkService)) {
                    val chatClient = ChatClient(null, host, port)
                    communicationToServers.add(chatClient)
                    discoveredServices.add(networkService)
                    chatActivity.setDiscoveredServices(discoveredServices)
                }
                Log.i(Constants.TAG, "A service has been discovered on $host:$port")
            }
        }
    }

    fun registerNetworkService(port: Int) {
        Log.i(Constants.TAG, "Register network service on port $port")
        chatServer = ChatServer(this, port)
        val serverSocket: ServerSocket? = chatServer!!.getServerSocket()
        if (serverSocket == null) {
            throw Exception("Could not get server socket")
        }
        chatServer!!.start()

        val serviceInfo = ServiceInfo.create(
            Constants.SERVICE_TYPE,
            Constants.SERVICE_NAME + Utilities.generateIdentifier(Constants.IDENTIFIER_LENGTH),
            port,
            Constants.SERVICE_DESCRIPTION
        )

        if (jmDNS != null) {
            serviceName = serviceInfo.name
            jmDNS!!.registerService(serviceInfo)
        }
        chatActivity.title = serviceInfo.name
        Log.i(Constants.TAG, "Register service ${serviceInfo.name}:${serviceInfo.typeWithSubtype}:${serviceInfo.port}")
    }

    fun unregisterNetworkService() {
        Log.i(Constants.TAG, "Unregister network service")
        jmDNS?.unregisterAllServices()
        for (communicationFromClient in communicationFromClients) {
            communicationFromClient.stopThreads()
        }
        communicationFromClients.clear()
        chatServer?.stopThread()
        val conversations = chatActivity.getConversations()
        conversations.clear()
        chatActivity.setConversations(conversations)
        chatActivity.title = "Chat Service JmDNS"
    }

    fun startNetworkServiceDiscovery() {
        Log.i(Constants.TAG, "Start network service discovery")
        if (jmDNS != null && serviceListener != null) {
            jmDNS!!.addServiceListener(Constants.SERVICE_TYPE, serviceListener)
        }
    }

    fun stopNetworkServiceDiscovery() {
        Log.i(Constants.TAG, "Stop network service discovery")
        if (jmDNS != null && serviceListener != null) {
            jmDNS!!.removeServiceListener(Constants.SERVICE_TYPE, serviceListener)
        }
        val discoveredServices = chatActivity.getDiscoveredServices()
        discoveredServices.clear()
        chatActivity.setDiscoveredServices(discoveredServices)
        for (communicationToServer in communicationToServers) {
            communicationToServer.stopThreads()
        }
        communicationToServers.clear()
    }

    fun getCommunicationToServers(): List<ChatClient> {
        return communicationToServers
    }

    fun getCommunicationFromClients(): List<ChatClient> {
        return communicationFromClients
    }

    fun setCommunicationFromClients(communicationFromClients: List<ChatClient>) {
        this.communicationFromClients = communicationFromClients as MutableList<ChatClient>
        val conversations = ArrayList<NetworkService>()
        for (communicationFromClient in communicationFromClients) {
            val conversation = NetworkService(
                null,
                communicationFromClient.getSocket()!!.inetAddress.toString(),
                communicationFromClient.getSocket()!!.port,
                Constants.CONVERSATION_FROM_CLIENT
            )
            conversations.add(conversation)
        }
        chatActivity.setConversations(conversations)
    }

    fun closeJmDNS() {
        try {
            jmDNS?.close()
            jmDNS = null
        } catch (ioException: IOException) {
            Log.e(Constants.TAG, "An exception has occurred: ${ioException.message}")
            if (Constants.DEBUG) {
                ioException.printStackTrace()
            }
        }
    }
}

