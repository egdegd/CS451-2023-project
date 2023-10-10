package cs451;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProcessManager {
    private final UDPReceiver receiver;
    private final UDPSender sender;
    private final Host host;
    private final List<String> logs = Collections.synchronizedList(new ArrayList<>());

    public ProcessManager(Host host) throws IOException, ClassNotFoundException {
        receiver = new UDPReceiver(host.getPort(), this);
        sender = new UDPSender(this);
        this.host = host;
        receiver.start();
        sender.start();
    }

    public void PLSend(Message m) throws IOException {
        sender.addMessageToList(m);
        synchronized (logs) {
            logs.add("b " + m.getText());
        }
    }
    public void send(Message m) throws IOException {
        sender.send(m);
    }
    public void deleteMessageFromStubbornList(int messageId) {
        sender.deleteMessageFromStubbornList(messageId);
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
}
