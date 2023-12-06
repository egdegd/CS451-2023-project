package cs451;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.*;

public class ProcessManager {
    private final UDPReceiver receiver;
    private final UDPSender sender;
    private final Host host;
    private final List<Host> hostsList;
    public int lastFifoBroadcast = 0;
    public Map<Integer, Integer> lastFifoDeliver = new HashMap<>();


    public ProcessManager(Host host, List<Host> hostsList) throws IOException {
        DatagramSocket socket = new DatagramSocket(host.getPort());
        this.hostsList = hostsList;
        this.host = host;
        for (Host h: this.hostsList) {
            lastFifoDeliver.put(h.getId(), 0);
        }
        receiver = new UDPReceiver(host.getPort(), socket, this);
        sender = new UDPSender(socket, this);
        receiver.start();
        sender.start();
    }

//    public void PLSend(Message m) {
//        sender.addBebMessageToStubbornList(m);
//    }
    public void send(boolean isAck, Message message) throws IOException {
        if (isAck) {
            sender.UdpSend("is_ack&&" + message.getText(), message.getSender().getIp(), message.getSender().getPort());
        } else {
//            Useless condition
            sender.UdpSend(message.getText(), message.getReceiver().getIp(), message.getReceiver().getPort());
        }
    }
    public void deleteMessageFromStubbornList(Message message) {
        sender.deleteMessageFromStubbornList(message);
    }
    public Host getHost() {
        return host;
    }

    public void FIFODeliver(LightMessage message) {
        lastFifoDeliver.put(message.getSenderId(), Math.max(lastFifoDeliver.get(message.getSenderId()), message.getMessageId()));
    }

    public Host getHostByIpAndPort(String ip, int port) {
        return hostsList.stream().filter(x -> (Objects.equals(x.getIp(), ip)) && (x.getPort() == port) ).findAny().orElse(null);
    }
    public void bestEffortBroadCast(LightMessage m) {
        sender.addBebMessageToStubbornList(m);
//        for (Host reciverHost: hostsList) {
//            PLSend(new Message(text, host, reciverHost));
//        }
    }
    public void uniformReliableBroadcast(LightMessage m) {
        lastFifoBroadcast = Math.max(lastFifoBroadcast, m.getMessageId());
//        bestEffortBroadCast(m);
        sender.addUrbMessageToStubbornList(m);
//        String text = m.getSenderId() + "@@" + m.getText() + "@@" + m.getMessageId();
//        for (Host reciverHost: hostsList) {
//            Message new_m = new Message(text, host, reciverHost);
//            sender.addUrbMessageToStubbornList(new_m);
//        }
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

    public void bestEffortBroadCast(LAMessage m) {
        sender.addBebMessageToStubbornList(m);
    }
}
