package eu.arolg.cloud.utils;

import eu.arolg.cloud.service.ServiceType;

import java.net.ServerSocket;
import java.net.Socket;

public class PortFinder {


    public   static boolean available(int port) {
        try (Socket socket = new Socket("localhost", port)) {
            return false;
        } catch (java.io.IOException e) {
            return true;
        }
    }

    public static Integer getFreePort(ServiceType serviceType) {
        if(available(25565) && serviceType.equals(ServiceType.PROXY)){
            return 25565;
        }
        try {
            ServerSocket socket = new ServerSocket(0);
            int port = socket.getLocalPort();
            socket.close();
            return port;
        } catch ( java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
