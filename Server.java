import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * This is the Server class of the Project. It is where all
 * the clients connect to.
 *
 * @author Anchit, Nam, Terry, Garv
 * @version April 20 2025
 *
 */


public class Server {
    ServerSocket serverSocket;
    private Database database;
    private MessageDatabase messageDatabase;
    boolean running;
//    private Set<ClientHandler> clientHandlers; //to control all the clientHandler connecting to the server

    /**
     * Constructs a new Server with the specified server socket.
     * Initializes the databases and sets up auto-save functionality.
     *
     * @param serverSocket The server socket to accept client connections
     */
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.database = new Database();
        this.messageDatabase = new MessageDatabase();
        this.running = true;
//        this.clientHandlers = Collections.synchronizedSet(new HashSet<>());
        database.setupAutoSave();

        // Add shutdown hook to save data when server exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Server shutting down, saving data...");
            closeServerSocket();
        }));
    }
    /**
     * Starts the server and begins accepting client connections.
     * Creates a new thread with each client connection to the server.
     */
    public void startServer() {
        try {
            while (running && !serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected");
                ClientHandler clientHandler = new ClientHandler(socket , database, messageDatabase);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            if (running) {
                System.out.println("Server error: " + e.getMessage());
                e.printStackTrace();
            } else {
                System.out.println("Server closed");
            }
        }
    }

    /**
     * closes the server socket
     * Called when the server is shutting down either manually or by the JVM shutdown hook.
     */
    public void closeServerSocket() {
        try {
            running = false;

            //save data before shutdown
            database.writeToFile();
            messageDatabase.saveFile();

            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(4242);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
