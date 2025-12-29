package ro.pub.cs.systems.eim.lab09.chatservicejmdns.networkservicediscoveryoperations

import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentManager
import java.io.BufferedReader
import java.io.IOException
import java.io.PrintWriter
import java.net.NoRouteToHostException
import java.net.Socket
import java.util.ArrayList
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.general.Constants
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.general.Utilities
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.model.Message
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.view.ChatActivity
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.view.ChatConversationFragment

class ChatClient(
    private var context: Context?,
    private val host: String,
    private val port: Int
) {
    private var socket: Socket? = null

    constructor(context: Context?, socket: Socket) : this(context, "", 0) {
        this.socket = socket
        if (socket != null) {
            startThreads()
        }
    }

    private var sendThread: SendThread? = null
    private var receiveThread: ReceiveThread? = null

    private val messageQueue: BlockingQueue<String> = ArrayBlockingQueue(Constants.MESSAGE_QUEUE_CAPACITY)

    private val conversationHistory: MutableList<Message> = ArrayList()

    fun connect() {
        try {
            socket = Socket(host, port)
            Log.i(Constants.TAG, "A socket has been created on: ${socket!!.inetAddress}:${socket!!.localPort}")
        } catch (e: NoRouteToHostException) {
            Log.i(Constants.TAG, "Address is stale: $host:$port")
        } catch (ioException: IOException) {
            Log.i(Constants.TAG, "An exception has occurred while creating the socket: ${ioException.message}")
            if (Constants.DEBUG) {
                ioException.printStackTrace()
            }
        }
        if (socket != null) {
            startThreads()
        }
    }

    fun sendMessage(message: String) {
        try {
            messageQueue.put(message)
        } catch (interruptedException: InterruptedException) {
            Log.e(Constants.TAG, "An exception has occurred: ${interruptedException.message}")
            if (Constants.DEBUG) {
                interruptedException.printStackTrace()
            }
        }
    }

    private inner class SendThread : Thread() {
        override fun run() {
            val printWriter: PrintWriter? = Utilities.getWriter(socket!!)
            if (printWriter != null) {
                Log.d(Constants.TAG, "Sending messages to ${socket!!.inetAddress}:${socket!!.localPort}")
                // TODO exercise 6
                // iterate while the thread is not yet interrupted
                // - get the content (a line) from the messageQueue, if available, using the take() method
                // - if the content is not null
                //   - send the content to the PrintWriter, as a line
                //   - create a Message instance, with the content received and Constants.MESSAGE_TYPE_SENT as message type
                //   - add the message to the conversationHistory
                //   - if the ChatConversationFragment is visible (query the FragmentManager for the Constants.FRAGMENT_TAG tag)
            }

            Log.i(Constants.TAG, "Send Thread ended")
        }

        fun stopThread() {
            interrupt()
        }
    }

    private inner class ReceiveThread : Thread() {
        override fun run() {
            val bufferedReader: BufferedReader? = Utilities.getReader(socket!!)
            if (bufferedReader != null) {
                Log.d(Constants.TAG, "Receiving messages from ${socket!!.inetAddress}:${socket!!.localPort}")
                // TODO: exercise 7
                // iterate while the thread is not yet interrupted
                // - receive the content (a line) from the bufferedReader, if available
                // - if the content is not null
                //   - create a Message instance, with the content received and Constants.MESSAGE_TYPE_RECEIVED as message type
                //   - add the message to the conversationHistory
                //   - if the ChatConversationFragment is visible (query the FragmentManager for the Constants.FRAGMENT_TAG tag)
                //   append the message to the graphic user interface
            }

            Log.i(Constants.TAG, "Receive Thread ended")
        }

        fun stopThread() {
            interrupt()
        }
    }

    fun getSocket(): Socket? {
        return socket
    }

    fun setContext(context: Context?) {
        this.context = context
    }

    override fun toString(): String {
        return "$host:$port"
    }

    fun getConversationHistory(): List<Message> {
        return conversationHistory
    }

    fun startThreads() {
        sendThread = SendThread()
        sendThread!!.start()

        receiveThread = ReceiveThread()
        receiveThread!!.start()
    }

    fun stopThreads() {
        sendThread?.stopThread()
        receiveThread?.stopThread()
        try {
            socket?.close()
        } catch (ioException: IOException) {
            Log.e(Constants.TAG, "An exception has occurred: ${ioException.message}")
            if (Constants.DEBUG) {
                ioException.printStackTrace()
            }
        }
    }
}

