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
        this.hostsList = hostsList;
        this.host = host;
        receiver = new UDPReceiver(host.getPort(), socket, this);
        sender = new UDPSender(socket, this);
        receiver.start();
        sender.start();
    }

    public void PLSend(Message m) {
        sender.addMessageToStubbornList(m);
    }
    public void send(boolean isAck, Message message) throws IOException {
        if (isAck) {
            sender.UdpSend("is_ack&&" + message.getText(), message.getSender().getIp(), message.getSender().getPort());
        } else {
            sender.UdpSend(message.getText(), message.getReceiver().getIp(), message.getReceiver().getPort());
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
    public void urbDeliver(LightMessage message) {
        synchronized (logs) {
            logs.add("d " + message.getSenderId() + " " + message.getText());
        }
    }
    public void FIFODeliver(LightMessage message) {
        synchronized (logs) {
            logs.add("d " + message.getSenderId() + " " + message.getText());
        }
    }

    public Host getHostByIpAndPort(String ip, int port) {
        return hostsList.stream().filter(x -> (Objects.equals(x.getIp(), ip)) && (x.getPort() == port) ).findAny().orElse(null);
    }
    public void bestEffortBroadCast(LightMessage m) {
        String text = m.getSenderId() + "@@" + m.getText() + "@@" + m.getMessageId();
        for (Host reciverHost: hostsList) {
            if (reciverHost.getId() == m.getSenderId()) {
                continue;
            }
            PLSend(new Message(text, host, reciverHost));
        }
    }
    public void uniformReliableBroadcast(LightMessage m) {
        bestEffortBroadCast(m);
        synchronized (logs) {
            logs.add("b " + m.getText());
        }
    }
    public List<Host> getHostsList() {
        return hostsList;
    }

    public Host getHostById(Integer id) {
        return hostsList.stream().filter(x -> Objects.equals(x.getId(), id)).findAny().orElse(null);
    }
    public Integer numberOfHosts() {
        return hostsList.size();
    }
}
