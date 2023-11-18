package cs451;
import java.io.*;
import java.net.*;
import java.util.*;


public class UDPSender extends Thread{
    private final DatagramSocket clientSocket;
    private final List<Message> StubbornMessages = Collections.synchronizedList(new ArrayList<>());
    private final Map<Integer, LinkedList<Message>> messagesByReceiver = new HashMap<>();
    private final ProcessManager processManager;
    public UDPSender(DatagramSocket socket, ProcessManager processManager) {
        // Create a Datagram Socket
        clientSocket = socket;
        this.processManager = processManager;
    }

    public void run() {
        while (true) {
            combineMessagesByReceiver();
            try {
                sendBatch(8);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendBatch(int batchSize) throws IOException, ClassNotFoundException {
        for (int id : messagesByReceiver.keySet()) {
            Host receiverHost = null;
            LinkedList<Message> ms = messagesByReceiver.get(id);
            StringBuilder concatMessages = new StringBuilder();
            concatMessages.setLength(0);
            int curBatchSize = 0;
            int batchnumber = 0;
            while (!ms.isEmpty()) {
                receiverHost = ms.getFirst().getReceiver();
                if (curBatchSize == batchSize) {
                    try {
                        UdpSend(concatMessages.toString(),receiverHost.getIp(), receiverHost.getPort());
                        batchnumber += 1;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    curBatchSize = 0;
                    concatMessages.setLength(0);
                }
                concatMessages.append(ms.remove().getText()).append("&&");
                curBatchSize += 1;
                if (batchnumber == 100) {
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    batchnumber = 0;
                }

            }
            if (concatMessages.length() > 0) {
                try {
                    assert receiverHost != null;
                    UdpSend(concatMessages.toString(), receiverHost.getIp(), receiverHost.getPort());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void combineMessagesByReceiver() {
        synchronized (StubbornMessages) {
            for (Message m: StubbornMessages) {
                if (!messagesByReceiver.containsKey(m.getReceiver().getId())) {
                    messagesByReceiver.put(m.getReceiver().getId(), new LinkedList<>());
                }
                messagesByReceiver.get(m.getReceiver().getId()).add(m);
            }
        }
    }

    public void addMessageToStubbornList(Message m) {
        synchronized (StubbornMessages) {
            StubbornMessages.add(m);
        }
    }
    public void deleteMessageFromStubbornList(Message message) {
        synchronized (StubbornMessages) {
            StubbornMessages.remove(message);
        }
    }
    public void UdpSend(String messageText, String receiverIp, int receiverPort) throws IOException {
        byte[] sendData = messageText.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(receiverIp), receiverPort);
        clientSocket.send(sendPacket);
//        System.out.println("SEND " + messageText + " " + receiverPort);
    }

//    public void bestEffortBroadCast(Message m) {
//        String text = m.getSender().getId() + "$" + m.getText();
//        for (Host host: processManager.getHostsList()) {
//
//        }
//    }
}