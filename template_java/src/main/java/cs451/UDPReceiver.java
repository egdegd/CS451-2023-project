package cs451;

import java.io.*;
import java.net.*;

public class UDPReceiver {
    DatagramSocket serverSocket;
    DatagramPacket receivePacket;
    int port;
    public UDPReceiver(int port) throws IOException {
        serverSocket = new DatagramSocket(port);
        this.port = port;

    }
    public void listen() throws IOException {
        System.out.println("Server Started. Listening for Clients on port " + port + "...");
        while (true) {
            byte[] receiveData = new byte[1024];
            // Server waiting for clients message
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            // Get the client's IP address and port
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            // Convert Byte Data to String
            String clientMessage = new String(receivePacket.getData(),0,receivePacket.getLength());
            // Print the message with log header
            System.out.println("[" + " ,IP: " + IPAddress + " ,Port: " + port +"]  " + clientMessage);
        }
    }
}