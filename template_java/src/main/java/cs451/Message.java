package cs451;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Message implements Serializable {
    private final Host sender;
    private final Host receiver;
    private final String text;

    private static final AtomicInteger count = new AtomicInteger(0);

    private final int messageID;

    boolean isAck = false;

    public Message(String content, Host sender, Host receiver) {
        this.text = content;
        this.sender = sender;
        this.receiver = receiver;
        messageID = count.incrementAndGet();
    }

    public Message(boolean isAck, int messageId, Host sender, Host receiver){
        this.sender = sender;
        this.receiver = receiver;
        this.isAck = isAck;
        text = Integer.toString(messageId);
        messageID = count.incrementAndGet();
    }

    public Host getReceiver() {
        return receiver;
    }

    public Host getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }

    public int getMessageID() {
        return messageID;
    }
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Message)) {
            return false;
        }
        Message m = (Message) o;
        return messageID == m.getMessageID()
                && sender.getId() == m.getSender().getId()
                && receiver.getId() == m.getReceiver().getId();
    }
}
