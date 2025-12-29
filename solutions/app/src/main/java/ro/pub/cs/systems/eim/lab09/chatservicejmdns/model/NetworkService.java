package ro.pub.cs.systems.eim.lab09.chatservicejmdns.model;

import androidx.annotation.NonNull;

public class NetworkService {

    private final String serviceName;
    private String serviceHost;
    private final int servicePort;
    private final int serviceType;

    public NetworkService(String serviceName, String serviceHost, int servicePort, int serviceType) {
        this.serviceName = serviceName;
        this.serviceHost = serviceHost;
        if (this.serviceHost != null && this.serviceHost.startsWith("/")) {
            this.serviceHost = this.serviceHost.substring(1);
        }
        this.servicePort = servicePort;
        this.serviceType = serviceType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getServiceType() {
        return serviceType;
    }

    @Override
    public boolean equals(Object networkService) {
        if (!(networkService instanceof NetworkService))
            return false;

        if (serviceName == null) {
            return false;
        }
        return serviceName.equals(((NetworkService)networkService).getServiceName());
    }

    @NonNull
    @Override
    public String toString() {
        return ((serviceName != null) ? serviceName : "") + " " + serviceHost + ":" + servicePort;
    }

}
