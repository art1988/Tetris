import java.awt.Color;

class Cell {
    private Color   color;
    private boolean empty;

    Cell() {
        this.color = Color.BLACK;
        this.empty = true;
    }

    public Color getColor() {
        return color;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setStatus(boolean stat) {
        this.empty = stat;
    }
}