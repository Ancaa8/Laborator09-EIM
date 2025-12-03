package ro.pub.cs.systems.eim.lab09.chatservicejmdns.model

class NetworkService(
    val serviceName: String?,
    var serviceHost: String?,
    val servicePort: Int,
    val serviceType: Int
) {
    init {
        if (serviceHost != null && serviceHost!!.startsWith("/")) {
            serviceHost = serviceHost!!.substring(1)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is NetworkService) return false
        if (serviceName == null) return false
        return serviceName == other.serviceName
    }

    override fun toString(): String {
        return "${serviceName ?: ""} $serviceHost:$servicePort"
    }
}

