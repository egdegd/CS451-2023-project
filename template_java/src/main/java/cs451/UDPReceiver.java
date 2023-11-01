package cs451;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;

public class UDPReceiver extends Thread{
    private final DatagramSocket serverSocket;
    private final ProcessManager processManager;
    private final ArrayList<Message> delivered = new ArrayList<>();
    int port;
    public UDPReceiver(int port, DatagramSocket socket, ProcessManager processManager) throws IOException {
        serverSocket = socket;
        this.port = port;
        this.processManager = processManager;
    }
    public void run() {
        System.out.println("Server Started. Listening for Clients on port " + port + "...");
        while (true) {
            byte[] receiveData = new byte[1024];
            // Server waiting for clients message
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                serverSocket.receive(receivePacket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // Get the client's IP address and port
            String IPAddress = receivePacket.getAddress().toString();
            if (IPAddress.startsWith("/")) {
                IPAddress = IPAddress.substring(1);
            }
            // Convert Byte Data to String
            String messageText = new String(receivePacket.getData(), 0, receivePacket.getLength());

            String[] splitMessage = messageText.split("&&");
            if (Objects.equals(splitMessage[0], "is_ack")) {
                Message oneMessage = new Message(splitMessage[1], processManager.getHost(), processManager.getHostByIpAndPort(IPAddress, receivePacket.getPort()));
                processManager.deleteMessageFromStubbornList(oneMessage);
            } else {
                for (String textMessage: splitMessage) {
                    Message oneMessage = new Message(textMessage, processManager.getHostByIpAndPort(IPAddress, receivePacket.getPort()), processManager.getHost());
                    try {
                        processManager.send(true, oneMessage);
                        if (!delivered.contains(oneMessage)) {
                            delivered.add(oneMessage);
                            processManager.deliver(oneMessage);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

}