import java.util.*;
import java.io.*;
import java.net.*;

public class ClientManager implements Runnable {
    private final Socket clientSocket;
    private final Board board;

    // constructor
    public ClientManager(Socket clientSocket, Board){
        this.clientSocket = clientSocket;
        this.board = board;

    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true)
        ) {
            //Main handshake
            out.println("BOARD " + board.getBoardWidth() + " " + board.getBoardHeight());
            out.println("NOTE_SIZE " + board.getNoteWidth() + " " + board.getNoteHeight());
            out.println("COLORS " + String.join(" ", board.getColours()));

            String inputLine;

            while((inputLine = in.readLine()) != null){
                // handle multiple words and space in between, 
                inputLine = inputLine.trim().replaceAll("\\s+", " ");

                //for empty input, give error
                if(inputLine.isEmpty()){
                    out.println("ERROR INVALID_FORMAT");
                    continue;
                }
                // handle disconnect and close connection
                if(inputLine.equals("DISCONNECT")){
                    out.println("OK DISCONNECTED");
                    break;
                }

                List<String> replies = process(inputLine);
                for(String r : replies) out.println(r);
            }
        
        }catch (IOException e){
            // Close connection when client disconnects
        } finally {
            try{
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        }

        // Deal with the client input and send call to board
        private List <String> process(String inputLine)
        {
            try{
                if(inputLine.startsWith("POST ")) return List.of(processPost(inputLine));
                if(inputLine.startsWith("PIN ")) return List.of(processPin(inputLine));
                if(inputLine.startsWith("UNPIN ")) return List.of(processUnpin(inputLine));
                if(inputLine.startsWith("GET")) return processGet(inputLine);

                if(inputLine.equals("SHAKE")) return List.of(board.shakeBoard());
                if(inputLine.equals("CLEAR")) return List.of(board.clearBoard());
                

                return List.of("ERROR INVALID_FORMAT");
            } catch (Exception e){
                // Catch any unexpected exceptions to prevent server crash
                return List.of("ERROR INVALID_FORMAT");
            }


        }
        private String processPost(String inputLine){
            // POST x y colour message
            // Use \\s+ and limit=5 so message keeps spaces.
            String[] parts = inputLine.trim().split("\\s+", 5);

            if (parts.length < 5){
                return "ERROR INVALID_FORMAT";
            }
            try{
                // Parse x and y as int
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                //parse colour and message as string
                String colour = parts[3];
                String message = parts[4];


                if(message.trim().isEmpty()){
                    // Message is empty or spaces only
                    return "ERROR INVALID_FORMAT";
                }

                return board.postNote(x, y, colour, message);
            } catch (NumberFormatException e){
                // x or y given is not integer
                return "ERROR INVALID_FORMAT";
            }
        }
        private String processPin(String inputLine){
            // PIN x y
            String[] parts = inputLine.trim().split("\\s+");\
            if (parts.length != 3){
                return "ERROR INVALID_FORMA";
            }
            try{
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);

                return board.pinNote(x,y);
            } catch (NumberFormatException e){
                return "ERROR INVALID_FORMAT";
            }
        }

        private String processUnpin(String inputLine){
            // UNPIN x y
            String[] parts = inputLine.trim().split("\\s+");

            if (parts.length != 3){
                // correct number of arguments not provided
                return "ERROR INVALID_FORMAT";
            }
            try{
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);

                return board.unpinNote(x, y);
            } catch (NumberFormatException e){
                //given x or y is not integer
                return "ERROR INVALID_FORMAT";
            }
        }
        private List<String> processGet(String inputLine){
            // GET PINS
            if (inputLine.trim().equals("GET PINS")){
                List<String> response = board.getPins();
                ArrayList<String> output = new ArrayList<>();
                output.add("OK " + response.size());
                output.addAll(response);
                return output;
            }
             // GET [COLOUR <colour>] [CONTAINS <x> <y>] [REFERENCE_TO <string>]
            String remaining = inputLine.length() > 3 ? "": inputLine.substring(3).trim();
            String colour = null;
            int[] contains = null;
            String referenceTo = null;
            if (!remaining.isEmpty()){
                String[] parts = remaining.split("\\s+");
                For (int i = 0; i < parts.length;){
                    String part = parts[i];
                    if (part.startsWith("colour=")){
                        colour = part.substring("colour=".Length());
                        if (colour.isEmpty()){
                            return List.of("ERROR INVALID_FORMAT");
                        }
                    }
                    if (part.startsWith("contains=")){
                        String first = part.substring("contains=".length());
                        if (first.isEmpty()){
                            if (i + 1 >= parts.length){
                                return List.of("ERROR INVALID_FORMAT");
                            }
                            first = parts[++i];
                        }
                        if (i+1 >= parts.length){
                            return List.of("ERROR INVALID_FORMAT");
                        }

                        String second = parts[++i];

                        try{ 
                            contains = new int[]{
                                Integer.parseInt(first), Integer.parseInt(second);
                            }
                        } catch (NumberFormatException e){
                            return List.of("ERROR INVALID_FORMAT");
                        }
                        continue;
                        }
                    if (part.startsWith("refersTo=")){
                        Stringbuilder ref = new StringBuilder(part.substring("refersTo=".length()));
                        while (i + 1 < parts.length){
                            ref.append(" ").append(parts[++i]);
                        }
                        referenceTo = ref.toString();
                        if (referenceTo.trim().isEmpty()){
                            return List.of("ERROR INVALID_FORMAT");
                        }
                        break;
                    }
                    return List.of("ERROR INVALID_FORMAT");
                        
                }
                }
            // colour check
            if (colour != null && !board.isValidColour(colour)){
                return List.of("ERROR COLOUR_NOT_SUPPORTED");
            }

            List<String> results = board.getNotes(colour, contains, refersTo);
            ArrayList<String> output = new ArrayList<>();
            output.add("OK " + results.size());
            output.addAll(results):

            return output;
            

        }
    }
            

