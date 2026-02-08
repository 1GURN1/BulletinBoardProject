import java.util.*;
import java.util.concurrent.locks.*;

public class Board {
    private final int boardWidth;
    private final int boardHeight;
    private final int noteWidth;
    private final int noteHeight;
    private final List<Note> notes = new ArrayList<>();
    private final List<Pin> pins = new ArrayList<>();
    private final List<String> colours;
    private final Set<String> colourSet;
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public Board(int boardWidth, int boardHeight, int noteWidth, int noteHeight, Collection<String> colours) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.noteWidth = noteWidth;
        this.noteHeight = noteHeight;
        this.colours = new ArrayList<>(colours);
        this.colourSet = new HashSet<>(colours);
    }
    //helper access methods
    public int getBoardWidth() {
        return boardWidth;
    }
    public int getBoardHeight() {
        return boardHeight;
    }
    public int getNoteWidth() {
        return noteWidth;
    }
    public int getNoteHeight() {
        return noteHeight;
    }
    public List<String> getColours() {
        return Collections.unmodifiableList(colours);
    }
    public boolean isValidColour(String colour) {
        return colourSet.contains(colour);
    }
    
    // Post a note to the board
    public String postNote(int x, int y, String colour, String message){
        writeLock.lock();
        try{
            // Validate position and colour
            if(x < 0 || x + noteWidth > boardWidth || y < 0 || y + noteHeight > boardHeight){
                return "ERROR OUT_OF_BOUNDS";
            }
            if(!isValidColour(colour)){
                return "ERROR COLOR_NOT_SUPPORTED";
            }
            // Can not overlap with existing notes
            for(Note note : notes){
                if(note.getX() == x && note.getY() == y){
                    return "ERROR COMPLETE_OVERLAP";
                }
            }
            // add the note
            Note newNote = new Note(x, y, noteWidth, noteHeight, colour, message);
            notes.add(newNote);
            return "OK NOTE_POSTED";
        } finally {
            writeLock.unlock();
        }
    }

    // Pin a note to the board
    public String pinNote(int x, int y){
        writeLock.lock();
        try{
            // validate position
            if(x < 0 || x >=boardWidth || y < 0 || y >= boardHeight){
                return "ERROR OUT_OF_BOUNDS";
            }
            // find the note at the position
            List<Note> present = new ArrayList<>();
            for(Note note : notes){
                if(note.contains(x, y)){
                    present.add(note);
                }
            }
            if(present.isEmpty()){
                return "ERROR NOTE_NOT_FOUND";
            }
            // pin the note if not already pinned
            for(Note note : present){
                if(!noteHasPin(note, x, y)){
                    pins.add(new Pin(x, y, note));
                }
            }
            return "OK PIN_ADDED"; //is this what I called it in the doc
        } finally {
            writeLock.unlock();
        }
    }

    // Unpin a note from the board
    public String unpinNote(int x, int y){
        writeLock.lock();
        try{
            Iterator<Pin> iter = pins.iterator();
            boolean found = false;
            while(iter.hasNext()){
                Pin pin = iter.next();
                if(pin.getX() == x && pin.getY() == y){
                    iter.remove();
                    found = true;
                }
            }
            if(found){
                return "OK PIN_REMOVED";
            } else {
                return "ERROR PIN_NOT_FOUND";
            }
        } finally {
            writeLock.unlock();
        }
    }

    // Shake the board and remove unpinned notes
    public String shakeBoard(){
        writeLock.lock();
        try{
            Iterator<Note> iter = notes.iterator();
            while(iter.hasNext()){
                Note note = iter.next();
                if(!isNotePinned(note)){
                    removePinRefs(note);
                    iter.remove();
                }

            }
            return "OK SHAKE_COMPLETE";
        } finally {
            writeLock.unlock();
        }


    }

    // Clear the board of all notes and pins
    public String clearBoard(){
        writeLock.lock();
        try{
            notes.clear();
            pins.clear();
            
            return "OK BOARD_CLEARED";
        } finally {
            writeLock.unlock();
        }
    }
    public List<String> getPins(){
        readLock.lock();
        try{
            List<String> output = new ArrayList<>();
            for(Pin pin : pins){
                Note note = pin.getNote();
                output.add(String.format("PIN %d %d", pin.getX(), pin.getY()));
            }
            return output;
            
        }finally{
            readLock.unlock();
        }
    }

    public List<String> getNotes(String colour, int[] contains, String referenceTo){
        readLock.lock();
        try{
            List<String> output = new ArrayList<>();
            for(Note note : notes){
                if (colour != null && !note.getColour().equals(colour)) continue;
                if (contains != null && !note.contains(contains[0], contains[1])) continue;
                if (referenceTo != null){
                    String message = note.getMessage().toLowerCase();
                    String ref = referenceTo.toLowerCase();
                    if (!message.contains(ref)) continue;
                }
                boolean pinned = isNotePinned(note);
                output.add(String.format("NOTE %d %d %s %s PINNED=%s", 
                note.getX(), note.getY(), note.getColour(), note.getMessage(), 
                pinned ? "true" : "false"));
                
            }
            return output;
        } finally {
            readLock.unlock();
        }
    }

    private boolean isNotePinned(Note note){
        for(Pin pin : pins){
            if(pin.getNote() == note){
                return true;
            }
        }
        return false;
    }

    private boolean noteHasPin(Note note, int x, int y){
        for(Pin pin : pins){
            if(pin.getNote() == note && pin.getX() == x && pin.getY() == y){
                return true;
            }
        }
        return false;
    }

    private void removePinRefs(Note note){
        Iterator<Pin> iter = pins.iterator();
        while(iter.hasNext()){
            Pin pin = iter.next();
            if(pin.getNote() == note){
                iter.remove();
            }
        }
    }
}