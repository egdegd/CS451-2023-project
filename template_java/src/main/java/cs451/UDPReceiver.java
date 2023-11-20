package cs451;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class UDPReceiver extends Thread{
    private final DatagramSocket serverSocket;
    private final ProcessManager processManager;
    private final ArrayList<LightMessage> urbDelivered = new ArrayList<>();
    private Map<LightMessage, Set<Integer>> urbMessages = new HashMap<>();
    private Map<Integer, List<LightMessage>> pending = new HashMap<>();
    private Map<Integer, Integer> LastDelivered = new HashMap<>();
    int port;
    public UDPReceiver(int port, DatagramSocket socket, ProcessManager processManager) throws IOException {
        serverSocket = socket;
        this.port = port;
        this.processManager = processManager;
        for (Host host : processManager.getHostsList()) {
            pending.put(host.getId(), new ArrayList<>());
            LastDelivered.put(host.getId(), 0);
        }
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
                for (int i = 1; i < splitMessage.length; i++) {
                    Message oneMessage = new Message(splitMessage[i], processManager.getHost(), processManager.getHostByIpAndPort(IPAddress, receivePacket.getPort()));
                    processManager.deleteMessageFromStubbornList(oneMessage);
                }
            } else {
                try {
                    processManager.send(true, new Message(messageText, processManager.getHostByIpAndPort(IPAddress, receivePacket.getPort()), processManager.getHost()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                for (String textMessage: splitMessage) {
                    String[] messageInfo = textMessage.split("@@");
                    LightMessage oneLightMessage = new LightMessage(Integer.parseInt(messageInfo[0]), messageInfo[1], Integer.parseInt(messageInfo[2]));
                    if (urbDelivered.contains(oneLightMessage)) continue;
                    if (!urbMessages.containsKey(oneLightMessage)) {
                        urbMessages.put(oneLightMessage, new HashSet<>());
                        processManager.bestEffortBroadCast(oneLightMessage);
                    }
                    urbMessages.get(oneLightMessage).add(processManager.getHostByIpAndPort(IPAddress, receivePacket.getPort()).getId());
                    if (urbMessages.get(oneLightMessage).size() >= processManager.numberOfHosts() / 2) {
                        urbDelivered.add(oneLightMessage);
                        addToPending(oneLightMessage);
                        urbMessages.remove(oneLightMessage);
                    }
                }
            }
        }
    }

    private void addToPending(LightMessage oneLightMessage) {
        pending.get(oneLightMessage.getSenderId()).add(oneLightMessage);
        if (LastDelivered.get(oneLightMessage.getSenderId()) + 1 == oneLightMessage.getMessageId()) {

            for (LightMessage m: pending.get(oneLightMessage.getSenderId()).stream().sorted(Comparator.comparing(LightMessage::getMessageId)).collect(Collectors.toList())) {
                if (m.getMessageId() == LastDelivered.get(oneLightMessage.getSenderId()) + 1) {
                    LastDelivered.put(oneLightMessage.getSenderId(), LastDelivered.get(oneLightMessage.getSenderId()) + 1);
                    processManager.FIFODeliver(m);
                    pending.get(oneLightMessage.getSenderId()).remove(m);
                }
            }
        }
    }


}