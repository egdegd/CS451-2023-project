package cs451;

import java.io.Serializable;
import java.util.Objects;

public class Message implements Serializable {
    private final Host sender;
    private final Host receiver;
    private final String text;


    boolean isAck = false;

    public Message(String text, Host sender, Host receiver) {
        this.text = text;
        this.sender = sender;
        this.receiver = receiver;
    }

    public Message(boolean isAck, int messageId, Host sender, Host receiver){
        this.sender = sender;
        this.receiver = receiver;
        this.isAck = isAck;
        text = Integer.toString(messageId);
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

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Message)) {
            return false;
        }
        Message m = (Message) o;
        return
                Objects.equals(text, m.getText()) &&
                sender.getId() == m.getSender().getId()
                && receiver.getId() == m.getReceiver().getId();
    }
}

