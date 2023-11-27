package cs451;
import java.io.*;
import java.net.*;
import java.util.*;


public class UDPSender extends Thread{
    private final DatagramSocket clientSocket;
    private final List<LightMessage> stubbornMessagesBeb = Collections.synchronizedList(new ArrayList<>());
    private final Map<Integer, Integer> nextBebIndex = new HashMap<>();
    private final List<LightMessage> stubbornMessagesUrb = Collections.synchronizedList(new ArrayList<>());
    private final Map<Integer, Integer> nextUrbIndex = new HashMap<>();
    private final Map<Integer, List<LightMessage>> messagesByReceiver = new HashMap<>();
    private final ProcessManager processManager;
    int batchSize = 8;

    public UDPSender(DatagramSocket socket, ProcessManager processManager) {
        // Create a Datagram Socket
        clientSocket = socket;
        this.processManager = processManager;
        for (Host h: processManager.getHostsList()) {
            messagesByReceiver.put(h.getId(), Collections.synchronizedList(new ArrayList<>()));
            nextBebIndex.put(h.getId(), 0);
            nextUrbIndex.put(h.getId(), 0);
        }
    }

    public void run() {
        while (true) {
            try {
                sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            combineMessagesByReceiver();
            try {
                sendBatch();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            int minLastBeb = Integer.MAX_VALUE;
            int minLastUrb = Integer.MAX_VALUE;
            for (Host h: processManager.getHostsList()) {
                minLastBeb = Math.min(minLastBeb, nextBebIndex.get(h.getId()));
                minLastUrb = Math.min(minLastUrb, nextUrbIndex.get(h.getId()));
            }
            synchronized (stubbornMessagesBeb) {
                if (minLastBeb > 0) {
                    stubbornMessagesBeb.subList(0, minLastBeb).clear();
                    synchronized (nextBebIndex) {
                        int finalMinLastBeb = minLastBeb;
                        nextBebIndex.replaceAll((id, v) -> v - finalMinLastBeb);
                    }
                }
            }
            synchronized (stubbornMessagesUrb) {
                if (minLastUrb > 0) {
                    stubbornMessagesUrb.subList(0, minLastUrb).clear();
                    synchronized (nextUrbIndex) {
                        int finalMinLastUrb = minLastUrb;
                        nextUrbIndex.replaceAll((id, v) -> v - finalMinLastUrb);
                    }
                }
            }
        }
    }

    private void sendBatch() throws IOException, ClassNotFoundException {
        for (Host host: processManager.getHostsList()) {
            List<LightMessage> messagesToSend = messagesByReceiver.get(host.getId());
            StringBuilder concatMessages = new StringBuilder();
            concatMessages.setLength(0);
            int curBatchSize = 0;
            int batchNumber = 0;
            synchronized (messagesToSend) {
                for(LightMessage m: messagesToSend) {
                    if (curBatchSize == batchSize) {
                        try {
                            UdpSend(concatMessages.toString(),host.getIp(), host.getPort());
                            batchNumber += 1;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        curBatchSize = 0;
                        concatMessages.setLength(0);
                    }
                    String text = m.getSenderId() + "@@" + m.getText() + "@@" + m.getMessageId();
                    concatMessages.append(text).append("&&");
                    curBatchSize += 1;
                    if (batchNumber == 100) {
                        try {
                            sleep(10);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        batchNumber = 0;
                    }
                }
                if (concatMessages.length() > 0) {
                    UdpSend(concatMessages.toString(), host.getIp(), host.getPort());
                    concatMessages.setLength(0);
                }
            }
        }
    }

    private void combineMessagesByReceiver() {
        for (Host h: processManager.getHostsList()) {
            List<LightMessage> messagesToSend = messagesByReceiver.get(h.getId());
            synchronized (messagesToSend) {
                synchronized (stubbornMessagesBeb) {
                    int next = nextBebIndex.get(h.getId());
                    while (messagesToSend.size() < 100) {
                        if (stubbornMessagesBeb.size() <= next) {
                            break;
                        }
                        messagesToSend.add(stubbornMessagesBeb.get(next));
                        next += 1;
                        nextBebIndex.put(h.getId(), next);
                    }
                }
                synchronized (stubbornMessagesUrb) {
                    int next = nextUrbIndex.get(h.getId());
                    while (messagesToSend.size() < 100) {
                        if (stubbornMessagesUrb.size() <= next) {
                            break;
                        }
                        messagesToSend.add(stubbornMessagesUrb.get(next));
                        next += 1;
                        nextUrbIndex.put(h.getId(), next);
                    }
                }
            }
        }
    }

    public void addBebMessageToStubbornList(LightMessage m) {
        synchronized (stubbornMessagesBeb) {
            stubbornMessagesBeb.add(m);
        }
    }
    public void deleteMessageFromStubbornList(Message message) {

        String[] messageInfo = message.getText().split("@@");
        LightMessage oneLightMessage = new LightMessage(Integer.parseInt(messageInfo[0]), messageInfo[1], Integer.parseInt(messageInfo[2]));
        synchronized (messagesByReceiver) {
            messagesByReceiver.get(message.getReceiver().getId()).remove(oneLightMessage);
        }
    }
    public void UdpSend(String messageText, String receiverIp, int receiverPort) throws IOException {
        byte[] sendData = messageText.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(receiverIp), receiverPort);
        clientSocket.send(sendPacket);
//        System.out.println("SEND " + messageText + " " + receiverPort);
    }

    public void addUrbMessageToStubbornList(LightMessage m) {
        synchronized (stubbornMessagesUrb) {
            stubbornMessagesUrb.add(m);
        }
    }
}