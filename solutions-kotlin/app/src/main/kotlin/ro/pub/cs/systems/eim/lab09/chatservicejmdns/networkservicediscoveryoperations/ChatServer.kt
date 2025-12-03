package ro.pub.cs.systems.eim.lab09.chatservicejmdns.networkservicediscoveryoperations

import android.util.Log
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.general.Constants

class ChatServer(
    private val networkServiceDiscoveryOperations: NetworkServiceDiscoveryOperations,
    port: Int
) : Thread() {

    private var serverSocket: ServerSocket? = null

    init {
        try {
            serverSocket = ServerSocket(port)
        } catch (ioException: IOException) {
            Log.e(Constants.TAG, "An exception has occurred while opening the server socket: ${ioException.message}")
            if (Constants.DEBUG) {
                ioException.printStackTrace()
            }
        }
    }

    override fun run() {
        while (!currentThread().isInterrupted) {
            Log.i(Constants.TAG, "Waiting for a connection...")
            try {
                val socket: Socket = serverSocket!!.accept()
                Log.i(Constants.TAG, "Received a connection request from: ${socket.inetAddress}")
                val communicationFromClients = ArrayList(networkServiceDiscoveryOperations.getCommunicationFromClients())
                communicationFromClients.add(ChatClient(null, socket))
                networkServiceDiscoveryOperations.setCommunicationFromClients(communicationFromClients)
            } catch (ioException: IOException) {
                Log.e(Constants.TAG, "An exception has occurred during server run: ${ioException.message}")
                if (Constants.DEBUG) {
                    ioException.printStackTrace()
                }
            }
        }
    }

    fun getServerSocket(): ServerSocket? {
        return serverSocket
    }

    fun stopThread() {
        interrupt()
        try {
            serverSocket?.close()
        } catch (ioException: IOException) {
            Log.e(Constants.TAG, "An error has occurred while closing the server socket: ${ioException.message}")
            if (Constants.DEBUG) {
                ioException.printStackTrace()
            }
        }
    }
}

