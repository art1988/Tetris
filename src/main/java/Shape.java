import java.awt.Color;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

class Shape {
    public enum ShapeType {
        LINE, SQUARE, HALF_PLUS, L, S, Z
    };

    private ShapeType shapeType;

    private int[][] shapeSpace;

    private Color color;

    private int countOfTurn;


    Shape(ShapeType type) {
        shapeType = type;
        switch(type) {
            case LINE: createLine(); break;
            case SQUARE: createSquare(); break;
            case HALF_PLUS: createHalfPlus(); break;
            case L: createL(); break;
            case S: createS(); break;
            case Z: createZ(); break;
        }
        setColor();
    }

    public int[][] getShapeSpace() {
        return shapeSpace;
    }

    public Color getShapeColor() {
        return color;
    }

    private void createLine() {
        shapeSpace = new int[][] {
                     {1,0,0,0},
                     {1,0,0,0},
                     {1,0,0,0},
                     {1,0,0,0},
        };
    }

    private void createSquare() {
        shapeSpace = new int[][] {
                     {1,1,0,0},
                     {1,1,0,0},
                     {0,0,0,0},
                     {0,0,0,0},
        };
    }

    private void createHalfPlus() {
        shapeSpace = new int[][] {
                     {0,1,0,0},
                     {1,1,1,0},
                     {0,0,0,0},
                     {0,0,0,0},
        };
    }

    private void createL()
    {
        shapeSpace = new int[][] {
                {0,1,0,0},
                {0,1,0,0},
                {0,1,1,0},
                {0,0,0,0},
        };
    }

    private void createS()
    {
        shapeSpace = new int[][] {
                {0,1,1,0},
                {1,1,0,0},
                {0,0,0,0},
                {0,0,0,0},
        };
    }

    private void createZ()
    {
        shapeSpace = new int[][] {
                {1,1,0,0},
                {0,1,1,0},
                {0,0,0,0},
                {0,0,0,0},
        };
    }

    public void incrementCountOfTurn() {
        countOfTurn++;
    }

    public int getCountOfTurn() {
        return countOfTurn;
    }

    public void setCountOfTurn(int countOfTurn) {
        this.countOfTurn = countOfTurn;
    }

    public void rotateShape() {
        switch(shapeType) {
            case LINE:
                changeLineSpace();
                if(countOfTurn == 2) {
                    countOfTurn = 0;
                    changeLineSpace();
                }
                break;

            case SQUARE: break;

            case HALF_PLUS:
                changeHalfPlusSpace();
                if(countOfTurn == 4) {
                    countOfTurn = 0;
                    changeHalfPlusSpace();
                }
                break;

            case L:
                changeLSpace();
                if(countOfTurn == 4) {
                    countOfTurn = 0;
                    changeLSpace();
                }
                break;

            case S:
                changeSSpace();
                if(countOfTurn == 2) {
                    countOfTurn = 0;
                    changeSSpace();
                }
                break;

            case Z:
                changeZSpace();
                if(countOfTurn == 2) {
                    countOfTurn = 0;
                    changeZSpace();
                }
                break;
        }
    }

    private void changeLineSpace() {
        switch(countOfTurn) {
            case 0:
                shapeSpace = new int[][] {
                             {1,0,0,0},
                             {1,0,0,0},
                             {1,0,0,0},
                             {1,0,0,0},
                };
                break;

            case 1:
                shapeSpace = new int[][] {
                             {1,1,1,1},
                             {0,0,0,0},
                             {0,0,0,0},
                             {0,0,0,0},
                };
                break;
        }
    }

    private void changeHalfPlusSpace() {
        switch(countOfTurn) {
           case 0:
               shapeSpace = new int[][] {
                            {0,1,0,0},
                            {1,1,1,0},
                            {0,0,0,0},
                            {0,0,0,0},
               };
               break;
           case 1:
               shapeSpace = new int[][] {
                            {0,1,0,0},
                            {0,1,1,0},
                            {0,1,0,0},
                            {0,0,0,0},
               };
               break;
           case 2:
               shapeSpace = new int[][] {
                            {0,0,0,0},
                            {1,1,1,0},
                            {0,1,0,0},
                            {0,0,0,0},
               };
               break;
           case 3:
               shapeSpace = new int[][] {
                            {0,1,0,0},
                            {1,1,0,0},
                            {0,1,0,0},
                            {0,0,0,0},
               };
               break;
        }
    }

    private void changeLSpace() {
        switch (countOfTurn) {
            case 0:
                shapeSpace = new int[][] {
                        {0,1,0,0},
                        {0,1,0,0},
                        {0,1,1,0},
                        {0,0,0,0},
                };
                break;

            case 1:
                shapeSpace = new int[][] {
                        {0,0,0,0},
                        {1,1,1,0},
                        {1,0,0,0},
                        {0,0,0,0},
                };
                break;

            case 2:
                shapeSpace = new int[][] {
                        {1,1,0,0},
                        {0,1,0,0},
                        {0,1,0,0},
                        {0,0,0,0},
                };
                break;

            case 3:
                shapeSpace = new int[][] {
                        {0,0,1,0},
                        {1,1,1,0},
                        {0,0,0,0},
                        {0,0,0,0},
                };
                break;
        }
    }

    private void changeSSpace()
    {
        switch (countOfTurn)
        {
            case 0:
                shapeSpace = new int[][] {
                        {0,1,1,0},
                        {1,1,0,0},
                        {0,0,0,0},
                        {0,0,0,0},
                };
                break;

            case 1:
                shapeSpace = new int[][] {
                        {1,0,0,0},
                        {1,1,0,0},
                        {0,1,0,0},
                        {0,0,0,0},
                };
                break;
        }
    }

    private void changeZSpace()
    {
        switch (countOfTurn)
        {
            case 0:
                shapeSpace = new int[][] {
                        {1,1,0,0},
                        {0,1,1,0},
                        {0,0,0,0},
                        {0,0,0,0},
                };
                break;

            case 1:
                shapeSpace = new int[][] {
                        {0,0,1,0},
                        {0,1,1,0},
                        {0,1,0,0},
                        {0,0,0,0},
                };
                break;
        }
    }

    private void setColor() {
        Random rnd = new Random();
        switch(rnd.nextInt(4)) {
            case 0: color = Color.RED; break;
            case 1: color = Color.GREEN; break;
            case 2: color = Color.BLUE; break;
            case 3: color = Color.ORANGE; break;
        }
    }
}