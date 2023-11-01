package cs451;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ProcessManager {
    private final UDPReceiver receiver;
    private final UDPSender sender;
    private final Host host;
    private final List<Host> hostsList;
    private final List<String> logs = Collections.synchronizedList(new ArrayList<>());

    public ProcessManager(Host host, List<Host> hostsList) throws IOException, ClassNotFoundException {
        DatagramSocket socket = new DatagramSocket(host.getPort());
        receiver = new UDPReceiver(host.getPort(), socket, this);
        sender = new UDPSender(socket);
        this.hostsList = hostsList;
        this.host = host;
        receiver.start();
        sender.start();
    }

    public void PLSend(Message m) {
        sender.addMessageToList(m);
        synchronized (logs) {
            logs.add("b " + m.getText());
        }
    }
    public void send(boolean isAck, Message message) throws IOException {
        if (isAck) {
            sender.send("is_ack&&" + message.getText(), message.getSender().getIp(), message.getSender().getPort());
        } else {
            sender.send(message.getText(), message.getReceiver().getIp(), message.getReceiver().getPort());
        }
    }
    public void deleteMessageFromStubbornList(Message message) {
        sender.deleteMessageFromStubbornList(message);
    }
    public Host getHost() {
        return host;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void deliver(Message message) {
        synchronized (logs) {
            logs.add("d " + message.getSender().getId() + " " + message.getText());
        }
    }

    public Host getHostByIpAndPort(String ip, int port) {
        return hostsList.stream().filter(x -> (Objects.equals(x.getIp(), ip)) && (x.getPort() == port) ).findAny().orElse(null);
    }
}
