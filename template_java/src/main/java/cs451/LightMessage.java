package cs451;

import java.util.Objects;

public class LightMessage {
    private final Integer SenderId;
    private final String text;
    private final Integer id;

    public LightMessage(Integer senderId, String text, Integer id) {
        SenderId = senderId;
        this.text = text;
        this.id = id;
    }

    public Integer getSenderId() {
        return SenderId;
    }

    public String getText() {
        return text;
    }
    public Integer getMessageId() {
        return  id;
    }
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof LightMessage)) {
            return false;
        }
        LightMessage m = (LightMessage) o;
        return
                Objects.equals(id, m.getMessageId()) &&
                        Objects.equals(SenderId, m.SenderId);
    }
    public int hashCode() {
        return (SenderId.toString() + '$' + text + '$' + id.toString()).hashCode();
    }
}
