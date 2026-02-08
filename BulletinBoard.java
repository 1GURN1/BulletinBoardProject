import java.net.*;
import java.io.*;
import java.util.*;

public class BulletinBoard {
    public static Board BOARD;

    public static void main(String[] args) {
        if (args.length < 6) {
            System.out.println("Usage: java BulletinBoard <port> <boardWidth> <boardHeight> <noteWidth> <noteHeight> <colour1> <colour2> ...");
            return;
        }

        int port = Integer.parseInt(args[0]);
        int boardWidth = Integer.parseInt(args[1]);
        int boardHeight = Integer.parseInt(args[2]);
        int noteWidth = Integer.parseInt(args[3]);
        int noteHeight = Integer.parseInt(args[4]);

        // invalid arguments check
        if (port <= 0 || boardWidth <= 0 || boardHeight <= 0 || noteWidth <= 0 || noteHeight <= 0) {
            System.out.println("ERROR INVALID_ARGUMENTS");
            return;
        }

        // Use Linked Hash Set to maintain insertion order and avoid duplicates for the colours
        Set<String> colours = new LinkedHashSet<>();
        for (int i = 5; i < args.length; i++) {
            String colour = args[i].trim();
            if (!colour.isEmpty()) {
                colours.add(colour);
            }
        }

        BOARD = new Board(boardWidth, boardHeight, noteWidth, noteHeight, colours);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Bulletin Board Server is running on port " + port);
            System.out.println("Board dimensions: " + boardWidth + "x" + boardHeight + "|| Note dimensions: " + noteWidth + "x" + noteHeight + "|| Supported colours: " + colours);    
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread thread = new Thread(new ClientManager(clientSocket, BOARD));
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("ERROR SERVER_ERROR");
            //e.printStackTrace() can be used for more detailed debugging
        }
    }
}   