import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
/**
 * Manages the storage and retrieval of messages between users in the marketplace system.
 * This class handles the persistence of message history to files and provides methods
 * for sending messages and retrieving conversation history between users.
 */
public class MessageDatabase {
    public ArrayList<String> fileNameList;

    //using username as id
    public HashMap<String, ArrayList<String>> userMessageList;

    public Database database; //we may need this later

    private final String fileNameForFileNameList = "fileNameList.txt";

    /**
     * Constructs a new MessageDatabase instance.
     * Initializes data structures and loads existing message history from files.
     * Creates empty data structures if files don't exist or cannot be read.
     */
    public MessageDatabase() {
        fileNameList = new ArrayList<>();
        userMessageList = new HashMap<>();
        try (BufferedReader bfr = new BufferedReader(new FileReader(fileNameForFileNameList))) {
            String line;
            while ((line = bfr.readLine()) != null) {
                fileNameList.add(line);
                String[] parts = line.split("-");
                if (!userMessageList.containsKey(parts[0])) {
                    userMessageList.put(parts[0], new ArrayList<>());
                }
                if (!userMessageList.containsKey(parts[1])) {
                    userMessageList.put(parts[1], new ArrayList<>());
                }

                userMessageList.get(parts[0]).add(parts[1]);
                userMessageList.get(parts[1]).add(parts[0]);
            }
        } catch (IOException e) {
            System.out.println("IO Exception");
        } finally {
            database = new Database();
            database.loadDatabase();
        }
    }

    /**
     * Saves the list of message file names to the file system.
     *
     * @throws IOException if an I/O error occurs during file writing
     */
    public void saveFile() throws IOException {
        try (BufferedWriter bfw = new BufferedWriter(new FileWriter(fileNameForFileNameList))) {
            for (String fileName: fileNameList) {
                bfw.write(fileName);
                bfw.newLine();
            }
        }
    }

    /**
     * Finds or creates a file for storing messages between two users.
     * Checks for existing files in both possible name combinations (userId1-userId2 or userId2-userId1).
     * Creates a new file if no existing conversation is found.
     *
     * @param userId1 the ID of the first user in the conversation
     * @param userId2 the ID of the second user in the conversation
     * @return the File object representing the message history between the users
     */
    public synchronized File findMessageFile(String userId1, String userId2) {
        String directoryPath = "Message and photos database";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();  // Create directory if it doesn't exist
        }
        String fileName = userId1 + "-" + userId2;
        String fileName2 = userId2 + "-" + userId1;
        File file = new File(directoryPath, fileName2);
        if (!file.exists()) {
            file = new File(directoryPath, fileName);
            if (!fileNameList.contains(fileName)) {
                fileNameList.add(fileName);
            }
        }
        return file;
    }

    /**
     * Sends a message from one user to another and stores it in the appropriate file.
     * Creates the message with a timestamp and adds both users to each other's message lists
     * if they aren't already present. This method is synchronized to prevent concurrent access issues.
     *
     * @param sendUserId the ID of the user sending the message
     * @param receiveUserId the ID of the user receiving the message
     * @param messageContent the content of the message being sent
     * @return true if the message was sent successfully, false otherwise
     */
    public synchronized boolean sendMessage(String sendUserId, String receiveUserId, String messageContent) {
        //check the messageContent
        if (messageContent == null) {
            return false;
        }
        // Initialize user message lists if they don't exist
        if (!userMessageList.containsKey(sendUserId)) {
            userMessageList.put(sendUserId, new ArrayList<>());
        }
        if (!userMessageList.containsKey(receiveUserId)) {
            userMessageList.put(receiveUserId, new ArrayList<>());
        }
        Message message = new Message(sendUserId, receiveUserId, messageContent);
        File file = findMessageFile(sendUserId, receiveUserId);
        // write to the file
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(file, true))) {
            pw.println(message.toFileString());
            pw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //add senderId to the message list of userId and vice versa
        if (!userMessageList.get(sendUserId).contains(receiveUserId)) {
            userMessageList.get(sendUserId).add(receiveUserId);
        }
        if (!userMessageList.get(receiveUserId).contains(sendUserId)) {
            userMessageList.get(receiveUserId).add(sendUserId);
        }
        return true;
    }


    /**
     * Retrieves the message history between two users.
     * Reads all messages from the appropriate file and formats them for display.
     *
     * @param user1 the ID of the first user in the conversation
     * @param user2 the ID of the second user in the conversation
     * @return an ArrayList of formatted message strings in chronological order
     */
    public ArrayList<String> getMessageHistory(String user1, String user2) {
        File f = findMessageFile(user1, user2);
        ArrayList<String> historyMessage = new ArrayList<>();
        try (BufferedReader bfr = new BufferedReader(new FileReader(f))) {
            String line = bfr.readLine();
            while (line != null) {
                Message message = fromFileString(line);
                historyMessage.add(displayedMessage(message));
                line = bfr.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return historyMessage;
    }
    /**
     * Creates a Message object from its file string representation.
     * Parses the components of the stored message string and constructs a new Message object.
     *
     * @param fileString the string representation of a message from the file "fileName.txt"
     * @return a Message object with the parsed properties
     */
    public Message fromFileString(String fileString) {
        String[] parts = fileString.split(";");
        String timestamp = parts[0];
        String senderId = parts[1];
        String receiverId = parts[2];
        String content = parts[3];
        return new Message(senderId, receiverId, content, timestamp);
    }

    /**
     * Formats a Message object into a human-readable display string.
     * The format is: "senderUsername: content (timestamp)"
     *
     * @param message the Message object to format
     * @return a formatted string representation of the message
     */
    public String displayedMessage(Message message) {
        String senderUsername = message.getSenderId();
        String timestamp = message.getTimestamp();
        String content = message.getMessageContent();
        return senderUsername + ": " +
                content + " (" + timestamp + ")";
    }

}
