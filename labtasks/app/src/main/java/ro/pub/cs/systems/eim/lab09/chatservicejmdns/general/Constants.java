package ro.pub.cs.systems.eim.lab09.chatservicejmdns.general;

public interface Constants {
    boolean DEBUG = true;

    String TAG = "[Chat Service]";

    String SERVICE_NAME = "Chat";
    String SERVICE_TYPE = "_chatservice._tcp.local.";
    String SERVICE_DESCRIPTION = "\u001bEIM Chat Service with JmDNS";

    String FRAGMENT_TAG = "ContainerFrameLayout";

    int MESSAGE_QUEUE_CAPACITY = 50;

    int CONVERSATION_TO_SERVER = 1;
    int CONVERSATION_FROM_CLIENT = 2;

    int MESSAGE_TYPE_SENT = 1;
    int MESSAGE_TYPE_RECEIVED = 2;

    int ALPHABET_LENGTH = 26;
    char FIRST_LETTER = 'a';
    int IDENTIFIER_LENGTH = 5;

    String CLIENT_POSITION = "clientPosition";
    String CLIENT_TYPE = "clientType";
}
