package cs451;

import java.util.Objects;

public class LAMessage {
    private final Integer SenderId;
    private final String text;

    public LAMessage(Integer senderId, String text) {
        SenderId = senderId;
        this.text = text;
    }

    public LAMessage(Game game) {
    }

    public Integer getSenderId() {
        return SenderId;
    }

    public String getText() {
        return text;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof LAMessage)) {
            return false;
        }
        LAMessage m = (LAMessage) o;
        return
                Objects.equals(text, m.getText()) &&
                        Objects.equals(SenderId, m.SenderId);
    }
    public int hashCode() {
        return (SenderId.toString() + '$' + text + '$').hashCode();
    }
}
