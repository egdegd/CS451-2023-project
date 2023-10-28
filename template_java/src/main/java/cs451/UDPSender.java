package cs451;
import java.io.*;
import java.net.*;
import java.util.*;


public class UDPSender extends Thread{
    private final DatagramSocket clientSocket;
    private final List<Message> StubbornMessages = Collections.synchronizedList(new ArrayList<>());
    private final Map<Integer, LinkedList<Message>> messagesByReceiver = new HashMap<>();
    public UDPSender(ProcessManager processManager) throws IOException {
        // Create a Datagram Socket
        clientSocket = new DatagramSocket();
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
            LinkedList<Message> ms = messagesByReceiver.get(id);
            StringBuilder concatMessages = new StringBuilder();
            concatMessages.setLength(0);
            int curBatchSize = 0;
            int batchnumber = 0;
            while (!ms.isEmpty()) {
                Host receiverHost = ms.getFirst().getReceiver();
                Host senderHost = ms.getFirst().getSender();
                if (curBatchSize == batchSize) {
                    Message m = new Message(concatMessages.toString(), senderHost, receiverHost);
                    try {
                        send(m);
                        batchnumber += 1;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    curBatchSize = 0;
                    concatMessages.setLength(0);
                }
                try {
                    concatMessages.append(serializeToString(ms.remove())).append("&&");
                    curBatchSize += 1;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
                Message mFirst = deserializeMessageFromString(concatMessages.toString().split("&&")[0]);
                Message m = new Message(concatMessages.toString(), mFirst.getSender(), mFirst.getReceiver());
                try {
                    send(m);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void combineMessagesByReceiver() {
        synchronized (StubbornMessages) {
//            messagesByReceiver.clear();
            for (Message m: StubbornMessages) {
//                messagesByReceiver.getOrDefault(m.getReceiver().getId(), new LinkedList<>()).add(m);
                if (!messagesByReceiver.containsKey(m.getReceiver().getId())) {
                    messagesByReceiver.put(m.getReceiver().getId(), new LinkedList<>());
                }
                messagesByReceiver.get(m.getReceiver().getId()).add(m);
            }
        }
    }

    public void addMessageToList(Message m) {
        synchronized (StubbornMessages) {
            StubbornMessages.add(m);
        }
    }
    public void deleteMessageFromStubbornList(int messageID) {
        synchronized (StubbornMessages) {
            StubbornMessages.removeIf(m -> m.getMessageID() == messageID);
        }
    }
    public void send(Message m) throws IOException {
        byte[] sendData = serializeMessage(m);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(m.getReceiver().getIp()), m.getReceiver().getPort());
        clientSocket.send(sendPacket);
        System.out.println("SEND " + m.getSender().getId() + " " + m.getReceiver().getId());
    }
    private byte[] serializeMessage(Message m) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        out = new ObjectOutputStream(bos);
        out.writeObject(m);
        out.flush();
        return bos.toByteArray();
    }
    private String serializeToString(Message m) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        out = new ObjectOutputStream(bos);
        out.writeObject(m);
        out.flush();
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }
    private Message deserializeMessageFromString(String stringMessage) throws IOException, ClassNotFoundException {
        byte [] data = Base64.getDecoder().decode(stringMessage);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        Message m  = (Message) ois.readObject();
        ois.close();
        return m;
    }
}