package cs451;
import java.io.*;
import java.net.*;

public class UDPSender {
    DatagramSocket clientSocket;
    public UDPSender() throws IOException {
        // Create a Datagram Socket
        clientSocket = new DatagramSocket();
    }
    public void send(String m, int port) throws IOException {
        byte[] sendData = m.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("127.0.0.1"), port);
        clientSocket.send(sendPacket);
    }
}