import java.util.*;
import java.io.*;
import java.net.*;

public class ClientConnection{
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private final ArrayList<String> handshakeResponse = new ArrayList<>();

    public ClientConnection(String host, int port)
            throws IOException {
                socket = new Socket(host, port);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);


                //initial handshake
                for (int i = 0; i < 3; i++) {
                    String response = input.readLine();
                    if (response == null){
                        throw new IOException("ERROR SERVER_ERROR during connection. not client side error");
                    }
                    handshakeResponse.add(response);
                }
                
                
            }
    public boolean validateConnection(){
        boolean result = socket != null && socket.isConnected() && !socket.isClosed();
        return result;
    }

    public ArrayList<String> getHandshakeResponse(){
        return new ArrayList<>(handshakeResponse);
    }

    // send command and return response
    public ArrayList<String> sendCommand(String command) throws IOException{
        output.println(command);
        String response = input.readLine();
        ArrayList<String> result = new ArrayList<>();
        if (response == null) throw new IOException("ERROR SERVER_ERROR");
        result.add(response);
        
        boolean isGetResponse = command.equals("GET") || command.startsWith("GET ");
        if (isGetResponse && response.startsWith("OK ")){
            int n = parseLineCount(response);
            for (int i = 0; i < n; i++){
                String line = input.readLine();
                if (line == null)
                    throw new IOException("ERROR INVALID_RESPONSE");
                result.add(line);
            }
        }

        return result;
    }

    private int parseLineCount(String response){
        String[] parts = response.split("\\s+");
        if (parts.length < 2){
            return 0;
        }
        try{
            int n = Integer.parseInt(parts[1]);
            return n;
        }
        catch (NumberFormatException e){
            return 0;
        }
    }

    public void disconnect() throws IOException{
        //if connection is open, close it
        if (validateConnection()){
            socket.close();
        }
    }
}
