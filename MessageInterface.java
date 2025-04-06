/**
 * Interface defining the core functionality for message objects
 * in the marketplace messaging system.
 */
public interface MessageInterface {

    String createTimestamp();
    String getSenderId();
    void setSenderId(String senderId);
    String getReceiverId();
    void setReceiverId(String receiverId);
    String getMessageContent();
    void setMessageContent(String messageContent);
    String getTimestamp();
    void setTimestamp(String timestamp);
    String toFileString();
}
