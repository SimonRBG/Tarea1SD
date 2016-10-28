package cl.uchile.dcc.cc5303;

/**
 * Created by pecesito on 28-10-16.
 */

import java.net.NetworkInterface;
import java.util.Enumeration;
import java.net.SocketException;
import java.net.InetAddress;
import java.net.Inet4Address;

public class Util {
    public static String getIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual() || iface.isPointToPoint())
                    continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    final String ip = addr.getHostAddress();
                    if (Inet4Address.class == addr.getClass())
                        return ip;
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
