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
    public final LatticeAgreement la;
    public Map<Integer, Integer> lastFifoDeliver = new HashMap<>();
    public final List<String> logs = Collections.synchronizedList(new LinkedList<>());
    private Map<Integer, String> results = new HashMap<>();
    private int lastResult = 0;

    public ProcessManager(Host host, List<Host> hostsList) throws IOException {
        DatagramSocket socket = new DatagramSocket(host.getPort());
        this.hostsList = hostsList;
        this.host = host;
        la = new LatticeAgreement(this);
        receiver = new UDPReceiver(host.getPort(), socket, this);
        sender = new UDPSender(socket, this);
        receiver.start();
        sender.start();
    }

    public void sendAck(Message message) throws IOException {
        sender.UdpSend("is_ack&&" + message.getText(), message.getSender().getIp(), message.getSender().getPort());
    }
    public void deleteMessageFromStubbornList(String message, Host receiver) {
        sender.deleteMessageFromStubbornList(message, receiver.getId());
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

    public void addToStubbornLink(Message message) {
        sender.addMessageToStubbornList(message);
    }
    public void decide(Game game) {
        StringBuilder concatPropose = new StringBuilder();
        for(int pr : game.proposedValue) {
            concatPropose.append(pr).append(" ");
        }
        concatPropose.deleteCharAt(concatPropose.length() - 1);
        results.put(game.gameNumber, concatPropose.toString());
        synchronized (logs) {
            while (results.containsKey(lastResult)) {
                logs.add(results.get(lastResult));
                results.remove(lastResult);
                lastResult++;
            }
        }
    }
}
