package ro.pub.cs.systems.eim.lab09.chatservicejmdns.networkservicediscoveryoperations

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
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
                try {
                    while (!currentThread().isInterrupted) {
                        val content = messageQueue.take()
                        if (content != null) {
                            Log.d(Constants.TAG, "Sending the message: $content")
                            printWriter.println(content)
                            printWriter.flush()
                            val message = Message(content, Constants.MESSAGE_TYPE_SENT)
                            conversationHistory.add(message)
                            if (context != null) {
                                val chatActivity = context as ChatActivity
                                val fragmentManager: FragmentManager = chatActivity.supportFragmentManager
                                val fragment: Fragment? = fragmentManager.findFragmentByTag(Constants.FRAGMENT_TAG)
                                if (fragment is ChatConversationFragment && fragment.isVisible) {
                                    fragment.appendMessage(message)
                                }
                            }
                        }
                    }
                } catch (interruptedException: InterruptedException) {
                    Log.e(Constants.TAG, "An exception has occurred: ${interruptedException.message}")
                    if (Constants.DEBUG) {
                        interruptedException.printStackTrace()
                    }
                }
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
                try {
                    while (!currentThread().isInterrupted) {
                        val content = bufferedReader.readLine()
                        if (content != null) {
                            Log.d(Constants.TAG, "Received the message: $content")
                            val message = Message(content, Constants.MESSAGE_TYPE_RECEIVED)
                            conversationHistory.add(message)
                            if (context != null) {
                                val chatActivity = context as ChatActivity
                                val fragmentManager: FragmentManager = chatActivity.supportFragmentManager
                                val fragment: Fragment? = fragmentManager.findFragmentByTag(Constants.FRAGMENT_TAG)
                                if (fragment is ChatConversationFragment && fragment.isVisible) {
                                    fragment.appendMessage(message)
                                }
                            }
                        }
                    }
                } catch (ioException: IOException) {
                    Log.e(Constants.TAG, "An exception has occurred: ${ioException.message}")
                    if (Constants.DEBUG) {
                        ioException.printStackTrace()
                    }
                }
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
