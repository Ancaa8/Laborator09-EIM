package ro.pub.cs.systems.eim.lab09.chatservicejmdns.controller

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.util.ArrayList
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.R
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.general.Constants
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.model.NetworkService
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.networkservicediscoveryoperations.ChatClient
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.networkservicediscoveryoperations.NetworkServiceDiscoveryOperations
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.view.ChatActivity
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.view.ChatConversationFragment

class NetworkServiceAdapter(
    private val context: Context,
    private var data: ArrayList<NetworkService>,
    private val NSDops: NetworkServiceDiscoveryOperations
) : BaseAdapter() {

    private data class NetworkServiceViewHolder(
        var networkServiceNameTextView: TextView? = null,
        var networkServiceConnectButton: Button? = null
    )

    private inner class NetworkServiceConnectButtonClickListener(
        private val clientPosition: Int,
        private val clientType: Int
    ) : View.OnClickListener {
        override fun onClick(view: View) {
            val chat: ChatClient = when (clientType) {
                Constants.CONVERSATION_TO_SERVER -> {
                    val chat = NSDops.getCommunicationToServers()[clientPosition]
                    if (chat.getSocket() == null) {
                        val t = Thread {
                            chat.connect()
                        }
                        t.start()
                        try {
                            t.join()
                        } catch (e: InterruptedException) {
                            Log.i(Constants.TAG, "Thread did not want to join.", e)
                        }
                    }
                    chat
                }
                Constants.CONVERSATION_FROM_CLIENT -> {
                    NSDops.getCommunicationFromClients()[clientPosition]
                }
                else -> return
            }

            if (chat.getSocket() != null) {
                val chatConversationFragment = ChatConversationFragment()
                val arguments = Bundle()
                arguments.putInt(Constants.CLIENT_POSITION, clientPosition)
                arguments.putInt(Constants.CLIENT_TYPE, clientType)
                chatConversationFragment.arguments = arguments

                (context as ChatActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container_frame_layout, chatConversationFragment, Constants.FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(view.context, "Cannot connect to $chat", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val layoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    fun setData(data: ArrayList<NetworkService>) {
        this.data = data
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val networkService = getItem(position) as NetworkService

        val view: View
        val networkServiceViewHolder: NetworkServiceViewHolder
        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.network_service, parent, false)
            val holder = NetworkServiceViewHolder()
            holder.networkServiceNameTextView = view.findViewById(R.id.networkservice_name_text_view)
            holder.networkServiceConnectButton = view.findViewById(R.id.network_service_connect_button)
            view.tag = holder
            networkServiceViewHolder = holder
        } else {
            view = convertView
            networkServiceViewHolder = view.tag as NetworkServiceViewHolder
        }
        networkServiceViewHolder.networkServiceNameTextView!!.text = networkService.toString()
        when (networkService.serviceType) {
            Constants.CONVERSATION_TO_SERVER -> {
                networkServiceViewHolder.networkServiceConnectButton!!.text =
                    context.resources.getString(R.string.connect)
            }
            Constants.CONVERSATION_FROM_CLIENT -> {
                networkServiceViewHolder.networkServiceConnectButton!!.text =
                    context.resources.getString(R.string.view)
            }
        }
        networkServiceViewHolder.networkServiceConnectButton!!.setOnClickListener(
            NetworkServiceConnectButtonClickListener(
                position,
                networkService.serviceType
            )
        )

        return view
    }
}

