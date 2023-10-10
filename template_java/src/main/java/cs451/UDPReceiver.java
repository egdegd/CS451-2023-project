package cs451;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class UDPReceiver extends Thread{
    private final DatagramSocket serverSocket;
    private final ProcessManager processManager;
    private final ArrayList<Message> delivered = new ArrayList<>();
    int port;
    public UDPReceiver(int port, ProcessManager processManager) throws IOException {
        serverSocket = new DatagramSocket(port);
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
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            // Convert Byte Data to String
            Message message = deserializeMessage(receivePacket.getData());
            if (message.isAck) {
                processManager.deleteMessageFromStubbornList(Integer.parseInt(message.getText()));
            } else {
                try {
                    processManager.send(new Message(true, message.getMessageID(), processManager.getHost(), message.getSender()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (!delivered.contains(message)) {
                    delivered.add(message);
                    processManager.deliver(message);
                }

            }
            // Print the message with log header
//            System.out.println("[" + " ,IP: " + IPAddress + " ,Port: " + port +"]  " + message.getText() + " " + message.isAck + " " +  message.getMessageID());
        }
    }

    private Message deserializeMessage(byte[] data) {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Message message = null;
        try {
            message = (Message) is.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return message;
    }
}