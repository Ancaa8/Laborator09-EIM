package ro.pub.cs.systems.eim.lab09.chatservicejmdns.networkservicediscoveryoperations;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import ro.pub.cs.systems.eim.lab09.chatservicejmdns.general.Constants;
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.general.Utilities;
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.model.Message;
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.view.ChatActivity;
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.view.ChatConversationFragment;

public class ChatClient {

    private Socket socket;
    private String host = "";
    private int port = 0;

    private Context context;

    private SendThread sendThread = null;
    private ReceiveThread receiveThread = null;

    private final BlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(Constants.MESSAGE_QUEUE_CAPACITY);

    private final List<Message> conversationHistory = new ArrayList<>();

    public ChatClient(Context context, String host, int port) {
        this.context = context;
        this.port = port;
        this.host = host;
        this.socket = null;
    }
    public void connect() {
        try {
            socket = new Socket(host, port);
            Log.i(Constants.TAG, "A socket has been created on: " + socket.getInetAddress() + ":" + socket.getLocalPort());
        } catch (NoRouteToHostException e){
        Log.i(Constants.TAG, "Address is stale: " + host + ":" + port);
        } catch (IOException ioException) {
            Log.i(Constants.TAG, "An exception has occurred while creating the socket: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
        if (socket != null) {
            startThreads();
        }
    }

    public ChatClient(Context context, Socket socket) {
        this.context = context;
        this.socket = socket;
        if (socket != null) {
            startThreads();
        }
    }

    public void sendMessage(String message) {
        try {
            messageQueue.put(message);
        } catch (InterruptedException interruptedException) {
            Log.e(Constants.TAG, "An exception has occurred: " + interruptedException.getMessage());
            if (Constants.DEBUG) {
                interruptedException.printStackTrace();
            }
        }
    }

    private class SendThread extends Thread {

        @Override
        public void run() {
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (printWriter != null) {
                Log.d(Constants.TAG, "Sending messages to " + socket.getInetAddress() + ":" + socket.getLocalPort());
                // TODO exercise 6
                // iterate while the thread is not yet interrupted
                // - get the content (a line) from the messageQueue, if available, using the take() method
                // - if the content is not null
                //   - send the content to the PrintWriter, as a line
                //   - create a Message instance, with the content received and Constants.MESSAGE_TYPE_SENT as message type
                //   - add the message to the conversationHistory
                //   - if the ChatConversationFragment is visible (query the FragmentManager for the Constants.FRAGMENT_TAG tag)
            }

            Log.i(Constants.TAG, "Send Thread ended");
        }

        public void stopThread() {
            interrupt();
        }

    }

    private class ReceiveThread extends Thread {

        @Override
        public void run() {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            if (bufferedReader != null) {
                Log.d(Constants.TAG, "Receiving messages from " + socket.getInetAddress() + ":" + socket.getLocalPort());
                // TODO: exercise 7
                // iterate while the thread is not yet interrupted
                // - receive the content (a line) from the bufferedReader, if available
                // - if the content is not null
                //   - create a Message instance, with the content received and Constants.MESSAGE_TYPE_RECEIVED as message type
                //   - add the message to the conversationHistory
                //   - if the ChatConversationFragment is visible (query the FragmentManager for the Constants.FRAGMENT_TAG tag)
                //   append the message to the graphic user interface
            }

            Log.i(Constants.TAG, "Receive Thread ended");
        }

        public void stopThread() {
            interrupt();
        }

    }

    public Socket getSocket() {
        return socket;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @NonNull
    public String toString(){
        return host + ":" + port;
    }

    public List<Message> getConversationHistory() {
        return conversationHistory;
    }

    public void startThreads() {
        sendThread = new SendThread();
        sendThread.start();

        receiveThread = new ReceiveThread();
        receiveThread.start();
    }

    public void stopThreads() {
        if(sendThread != null)
            sendThread.stopThread();
        if(receiveThread != null)
            receiveThread.stopThread();
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }
}