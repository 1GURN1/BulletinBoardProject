public class Pin {
    private final int x;
    private final int y;
    private final Note note;

    public Pin(int x, int y, Note note) {
        this.x = x;
        this.y = y;
        this.note = note;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Note getNote() {
        return note;
    }
}