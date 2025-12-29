package ro.pub.cs.systems.eim.lab09.chatservicejmdns.view

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.R
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.general.Constants
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.model.Message
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.networkservicediscoveryoperations.ChatClient
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.networkservicediscoveryoperations.NetworkServiceDiscoveryOperations

class ChatConversationFragment : Fragment() {

    private var chatCommunicationHistoryLinearLayout: LinearLayout? = null
    private var messageEditText: EditText? = null

    private var chatClient: ChatClient? = null

    private var clientPosition: Int = -1
    private var clientType: Int = -1

    private var view: View? = null

    private val sendMessageButtonClickListener = object : View.OnClickListener {
        override fun onClick(view: View) {
            val message = messageEditText!!.text.toString()
            if (message.isEmpty()) {
                Toast.makeText(activity, "You should fill a message!", Toast.LENGTH_SHORT).show()
            } else {
                messageEditText!!.setText("")
                chatClient!!.sendMessage(message)
            }
        }
    }

    init {
        this.clientPosition = -1
        this.clientType = -1
    }

    @Synchronized
    fun appendMessage(message: Message) {
        chatCommunicationHistoryLinearLayout!!.post {
            val messageTextView = TextView(activity)
            messageTextView.text = message.content
            val messageTextViewLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

            when (message.type) {
                Constants.MESSAGE_TYPE_SENT -> {
                    messageTextView.setBackgroundResource(R.drawable.frame_border_sent_message)
                    messageTextView.gravity = Gravity.START
                    messageTextViewLayoutParams.gravity = Gravity.START
                }

                Constants.MESSAGE_TYPE_RECEIVED -> {
                    messageTextView.setBackgroundResource(R.drawable.frame_border_received_message)
                    messageTextView.gravity = Gravity.END
                    messageTextViewLayoutParams.gravity = Gravity.END
                }
            }

            chatCommunicationHistoryLinearLayout!!.addView(messageTextView, messageTextViewLayoutParams)

            val space = Space(activity)
            val spaceLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            chatCommunicationHistoryLinearLayout!!.addView(space, spaceLayoutParams)
        }
    }

    @Suppress("UseRequireInsteadOfGet")
    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        if (view == null)
            view = inflater.inflate(R.layout.fragment_chat_conversation, parent, false)

        val rootView = view!!

        val arguments = arguments
        if (arguments == null) {
            this.clientPosition = -1
            this.clientType = -1
        } else {
            this.clientPosition = arguments.getInt(Constants.CLIENT_POSITION, -1)
            this.clientType = arguments.getInt(Constants.CLIENT_TYPE, -1)
        }

        val chatServiceActivity = activity as ChatActivity?
        assert(chatServiceActivity != null)
        val networkServiceDiscoveryOperations: NetworkServiceDiscoveryOperations = chatServiceActivity!!.getNetworkServiceDiscoveryOperations()

        when (clientType) {
            Constants.CONVERSATION_TO_SERVER -> {
                chatClient = networkServiceDiscoveryOperations.getCommunicationToServers()[clientPosition]
            }
            Constants.CONVERSATION_FROM_CLIENT -> {
                chatClient = networkServiceDiscoveryOperations.getCommunicationFromClients()[clientPosition]
            }
        }

        chatCommunicationHistoryLinearLayout = rootView.findViewById(R.id.chat_communication_history_linear_layout)
        messageEditText = rootView.findViewById(R.id.message_edit_text)

        val sendMessageButton: Button = rootView.findViewById(R.id.send_message_button)
        sendMessageButton.setOnClickListener(sendMessageButtonClickListener)

        if (chatClient != null) {
            chatClient!!.setContext(chatServiceActivity)
            val conversationHistory = chatClient!!.getConversationHistory()
            for (conversation in conversationHistory) {
                appendMessage(conversation)
            }
        }

        return rootView
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

