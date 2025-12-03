package ro.pub.cs.systems.eim.lab09.chatservicejmdns.general

object Constants {
    const val DEBUG = true

    const val TAG = "[Chat Service]"

    const val SERVICE_NAME = "Chat"
    const val SERVICE_TYPE = "_chatservice._tcp.local."
    const val SERVICE_DESCRIPTION = "\u001bEIM Chat Service with JmDNS"

    const val FRAGMENT_TAG = "ContainerFrameLayout"

    const val MESSAGE_QUEUE_CAPACITY = 50

    const val CONVERSATION_TO_SERVER = 1
    const val CONVERSATION_FROM_CLIENT = 2

    const val MESSAGE_TYPE_SENT = 1
    const val MESSAGE_TYPE_RECEIVED = 2

    const val ALPHABET_LENGTH = 26
    const val FIRST_LETTER = 'a'
    const val IDENTIFIER_LENGTH = 5

    const val CLIENT_POSITION = "clientPosition"
    const val CLIENT_TYPE = "clientType"
}

