package cs451;

import java.util.Objects;

public class LAMessage {
    private final Integer senderId;
    private final String text;

    public LAMessage(Integer senderId, String text) {
        this.senderId = senderId;
        this.text = text;
    }

    public LAMessage(Game game, Integer senderId) {
        this.senderId = senderId;
        this.text = game.getText();
    }

    public Integer getSenderId() {
        return senderId;
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
                        Objects.equals(senderId, m.senderId);
    }
    public int hashCode() {
        return (senderId.toString() + '$' + text + '$').hashCode();
    }
}
