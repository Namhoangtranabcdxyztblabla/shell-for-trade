import java.time.Instant;
import java.util.Calendar;
/**
 * Represents a message exchanged between users in the marketplace system.
 * Contains information about the sender, receiver, content, and timestamp of the message.
 */
public class Message {
    private String senderId;
    private String receiverId;
    private String messageContent;
    private String timestamp; // the time when the message is sent

    /**
     * Constructs a Message object with a specified timestamp.
     *
     * @param senderId the ID of the user sending the message
     * @param receiverId the ID of the user receiving the message
     * @param messageContent the content of the message
     * @param timestamp the time when the message was sent, in string format
     */
    public Message(String senderId, String receiverId, String messageContent, String timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageContent = messageContent;
        this.timestamp = timestamp;
    }

    /**
     * Constructs a Message object with the current time as the timestamp.
     *
     * @param senderId the ID of the user sending the message
     * @param receiverId the ID of the user receiving the message
     * @param messageContent the content of the message
     */
    public Message(String senderId, String receiverId, String messageContent) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageContent = messageContent;
        timestamp = createTimestamp();
    }

    /**
     * Creates a timestamp string based on the current system time.
     * Format: "MM/DD/YYYY HH:MM:SS"
     * This method is synchronized to prevent concurrent access issues.
     *
     * @return a formatted string representation of the current time
     */
    public synchronized String createTimestamp() {
        Calendar calendar = Calendar.getInstance();
        return String.format("%02d/%02d/%04d %02d:%02d:%02d",
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
    }


    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Converts the message object to a string format suitable for file storage.
     * Format: "timestamp;senderId;receiverId;messageContent"
     *
     * @return a string representation of the message for file storage
     */
    public String toFileString() {
        return timestamp + ";" + senderId + ";" + receiverId + ";" + messageContent;
    }



}
