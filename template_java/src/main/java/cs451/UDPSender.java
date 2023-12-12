package cs451;
import java.io.*;
import java.net.*;
import java.util.*;


public class UDPSender extends Thread{
    private final DatagramSocket clientSocket;
    private final List<LAMessage> stubbornMessagesBeb = Collections.synchronizedList(new ArrayList<>());
    private final Map<Integer, Integer> nextBebIndex = new HashMap<>();
    private final Map<Integer, List<String>> messagesByReceiver = new HashMap<>();
    private final ProcessManager processManager;
    final private int throughput = 100;

    int batchSize = 8;

    public UDPSender(DatagramSocket socket, ProcessManager processManager) {
        // Create a Datagram Socket
        clientSocket = socket;
        this.processManager = processManager;
        for (Host h: processManager.getHostsList()) {
            messagesByReceiver.put(h.getId(), Collections.synchronizedList(new ArrayList<>()));
            nextBebIndex.put(h.getId(), 0);
//            nextUrbIndex.put(h.getId(), 1);
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
            for (Host h: processManager.getHostsList()) {
                minLastBeb = Math.min(minLastBeb, nextBebIndex.get(h.getId()));
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
//            synchronized (stubbornMessagesUrb) {
//                if (minLastUrb > 0) {
//                    stubbornMessagesUrb.subList(0, minLastUrb).clear();
//                    synchronized (nextUrbIndex) {
//                        int finalMinLastUrb = minLastUrb;
//                        nextUrbIndex.replaceAll((id, v) -> v - finalMinLastUrb);
//                    }
//                }
//            }
        }
    }

    private void sendBatch() throws IOException, ClassNotFoundException {
        for (Host host: processManager.getHostsList()) {
            List<String> messagesToSend = messagesByReceiver.get(host.getId());
            StringBuilder concatMessages = new StringBuilder();
            concatMessages.setLength(0);
            int curBatchSize = 0;
            int batchNumber = 0;
            synchronized (messagesToSend) {
                for(String m: messagesToSend) {
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
                    concatMessages.append(m).append("&&");
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
            List<String> messagesToSend = messagesByReceiver.get(h.getId());
            synchronized (messagesToSend) {
                synchronized (stubbornMessagesBeb) {
                    int next = nextBebIndex.get(h.getId());
                    while (messagesToSend.size() < throughput) {
                        if (stubbornMessagesBeb.size() <= next) {
                            break;
                        }
                        messagesToSend.add(stubbornMessagesBeb.get(next).getText());
                        next += 1;
                        nextBebIndex.put(h.getId(), next);
                    }
                }
            }
        }
    }

//    public void addBebMessageToStubbornList(LightMessage m) {
//        synchronized (stubbornMessagesBeb) {
//            stubbornMessagesBeb.add(m);
//        }
//    }
    public void deleteMessageFromStubbornList(String message, int receiverId) {

        synchronized (messagesByReceiver) {
            messagesByReceiver.get(receiverId).remove(message);
        }
    }
    public void UdpSend(String messageText, String receiverIp, int receiverPort) throws IOException {
        byte[] sendData = messageText.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(receiverIp), receiverPort);
        clientSocket.send(sendPacket);
//        System.out.println("SEND " + messageText + " " + receiverPort);
    }


    public void addBebMessageToStubbornList(LAMessage message) {
        synchronized (stubbornMessagesBeb) {
            stubbornMessagesBeb.add(message);
        }
    }

    public void addMessageToStubbornList(Message message) {
        List<String> messages = messagesByReceiver.get(message.getReceiver().getId());
        synchronized (messages) {
            messages.add(message.getText());
        }
    }
}