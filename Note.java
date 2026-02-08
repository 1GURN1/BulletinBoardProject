public class Note {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final String colour;
    private final String message;

    public Note(int x, int y, int width, int height, String colour, String message) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.colour = colour;
        this.message = message;
    }

    public boolean contains(int x, int y) {
        return x >= this.x && x < this.x + width && y >= this.y && y < this.y + height;
    }

    // Getters for the field variables
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getColour() {
        return colour;
    }

    public String getMessage() {
        return message;
    }
}

