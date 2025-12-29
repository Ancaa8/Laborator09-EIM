package ro.pub.cs.systems.eim.lab09.chatservicejmdns.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AppCompatActivity;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import ro.pub.cs.systems.eim.lab09.chatservicejmdns.R;
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.controller.NetworkServiceAdapter;
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.general.Constants;
import ro.pub.cs.systems.eim.lab09.chatservicejmdns.networkservicediscoveryoperations.NetworkServiceDiscoveryOperations;

public class ChatNetworkServiceFragment extends Fragment {

    private EditText servicePortEditText = null;

    private Button serviceRegistrationStatusButton = null;
    private Button serviceDiscoveryStatusButton = null;

    private NetworkServiceDiscoveryOperations networkServiceDiscoveryOperations = null;

    private View view = null;

    private NetworkServiceAdapter discoveredServicesAdapter = null;
    private NetworkServiceAdapter conversationsAdapter = null;

    private ChatActivity chatActivity;

    private final ServiceRegistrationStatusButtonListener serviceRegistrationStatusButtonListener = new ServiceRegistrationStatusButtonListener();
    private class ServiceRegistrationStatusButtonListener implements Button.OnClickListener {
        @Override
        public void onClick(View view) {
            if (!chatActivity.getServiceRegistrationStatus()) {
                String port = servicePortEditText.getText().toString();
                if (port.isEmpty()) {
                    Toast.makeText(getActivity(), "Field service port should be filled!", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    networkServiceDiscoveryOperations.registerNetworkService(Integer.parseInt(port));
                } catch (Exception exception) {
                    Log.e(Constants.TAG, "Could not register network service: " + exception.getMessage());
                    if (Constants.DEBUG) {
                        exception.printStackTrace();
                    }
                    return;
                }
                startServiceRegistration();
            } else {
                networkServiceDiscoveryOperations.unregisterNetworkService();
                stopServiceRegistration();
            }
            chatActivity.setServiceRegistrationStatus(!chatActivity.getServiceRegistrationStatus());
        }

    }

    private final ServiceDiscoveryStatusButtonListener serviceDiscoveryStatusButtonListener = new ServiceDiscoveryStatusButtonListener();
    private class ServiceDiscoveryStatusButtonListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {
            if (chatActivity == null)
                return;

            if (!chatActivity.getServiceDiscoveryStatus()) {
                chatActivity.getNetworkServiceDiscoveryOperations().startNetworkServiceDiscovery();
                startServiceDiscovery();
            } else {
                networkServiceDiscoveryOperations.stopNetworkServiceDiscovery();
                stopServiceDiscovery();
            }
            chatActivity.setServiceDiscoveryStatus(!chatActivity.getServiceDiscoveryStatus());
        }

    }

    String getIPs() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            StringBuilder IPb = new StringBuilder();

            while (interfaces.hasMoreElements()) {
                NetworkInterface n = interfaces.nextElement();
                Enumeration<InetAddress> ee = n.getInetAddresses();

                while (ee.hasMoreElements()) {
                    InetAddress i = ee.nextElement();

                    if (i instanceof Inet4Address) {
                        if (IPb.length() != 0)
                            IPb.append(", ");
                        IPb.append(i.getHostAddress());
                    }
                }
            }

            return IPb.toString();
        } catch (SocketException socketException) {
            socketException.printStackTrace();
            return null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle state) {
        Log.i(Constants.TAG, "ChatNetworkServiceFragment -> onCreateView() callback method was invoked");

        if (view == null)
            view = inflater.inflate(R.layout.fragment_chat_network_service, parent, false);

        FragmentActivity activity = getActivity();
        if (activity == null)
            return view;

        chatActivity = (ChatActivity) activity;

        TextView LocalIPs = view.findViewById(R.id.service_discovery_local_addr);
        LocalIPs.setText(getIPs());

        servicePortEditText = view.findViewById(R.id.port_edit_text);

        serviceRegistrationStatusButton = view.findViewById(R.id.service_registration_status_button);
        serviceRegistrationStatusButton.setOnClickListener(serviceRegistrationStatusButtonListener);

        serviceDiscoveryStatusButton = view.findViewById(R.id.service_discovery_status_button);
        serviceDiscoveryStatusButton.setOnClickListener(serviceDiscoveryStatusButtonListener);

        networkServiceDiscoveryOperations = chatActivity.getNetworkServiceDiscoveryOperations();

        ListView discoveredServicesListView = view.findViewById(R.id.discovered_services_list_view);
        discoveredServicesAdapter = new NetworkServiceAdapter(chatActivity, chatActivity.getDiscoveredServices(), chatActivity.getNetworkServiceDiscoveryOperations());
        discoveredServicesListView.setAdapter(discoveredServicesAdapter);

        ListView conversationsListView = view.findViewById(R.id.conversations_list_view);
        conversationsAdapter = new NetworkServiceAdapter(chatActivity, chatActivity.getConversations(), chatActivity.getNetworkServiceDiscoveryOperations());
        conversationsListView.setAdapter(conversationsAdapter);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startServiceRegistration() {
        serviceRegistrationStatusButton.setBackgroundColor(ContextCompat.getColor(chatActivity, R.color.colorGreen));
        serviceRegistrationStatusButton.setText(getResources().getString(R.string.unregister_service));
    }

    public void stopServiceRegistration() {
        serviceRegistrationStatusButton.setBackgroundColor(ContextCompat.getColor(chatActivity, R.color.colorRed));
        serviceRegistrationStatusButton.setText(getResources().getString(R.string.register_service));
    }

    public void startServiceDiscovery() {
        serviceDiscoveryStatusButton.setBackgroundColor(ContextCompat.getColor(chatActivity, R.color.colorGreen));
        serviceDiscoveryStatusButton.setText(getResources().getString(R.string.stop_service_discovery));
    }

    public void stopServiceDiscovery() {
        serviceDiscoveryStatusButton.setBackgroundColor(ContextCompat.getColor(chatActivity, R.color.colorRed));
        serviceDiscoveryStatusButton.setText(getResources().getString(R.string.start_service_discovery));
    }

    public NetworkServiceAdapter getDiscoveredServicesAdapter() {
        return discoveredServicesAdapter;
    }

    public NetworkServiceAdapter getConversationsAdapter() {
        return conversationsAdapter;
    }

}
