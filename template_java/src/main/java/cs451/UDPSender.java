package cs451;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class UDPSender extends Thread{
    private final DatagramSocket clientSocket;
    private final List<Message> StubbornMessages = Collections.synchronizedList(new ArrayList<>());
    public UDPSender(ProcessManager processManager) throws IOException {
        // Create a Datagram Socket
        clientSocket = new DatagramSocket();
    }

    public void run() {
        while (true) {
            synchronized (StubbornMessages) {
                for (Message m : StubbornMessages) {
                    try {
                        send(m);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
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
    }
    private byte[] serializeMessage(Message m) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        out = new ObjectOutputStream(bos);
        out.writeObject(m);
        out.flush();
        return bos.toByteArray();
    }
}