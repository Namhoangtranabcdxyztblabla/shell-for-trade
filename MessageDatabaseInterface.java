import java.io.*;
import java.util.ArrayList;

public interface MessageDatabaseInterface {

    void saveFile() throws IOException;

    File findMessageFile(String userId1, String userId2);

    boolean sendMessage(String sendUserId, String receiveUserId, String messageContent);

    ArrayList<String> getMessageHistory(String user1, String user2);

    Message fromFileString(String fileString);

    String displayedMessage(Message message);
}
