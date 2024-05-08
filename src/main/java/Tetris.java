import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class Tetris {
    private static BufferedImage dynamicImage;
    
    private static int displacementX = 3, displacementY = 0;

    private static final int WINDOW_WIDTH   = 260,
                             WINDOW_HEIGHT  = 460,
                             CELL_SIZE      = 22,
                             LINE_NUMBERS   = 16,
                             COLUMN_NUMBERS = 10; 

    private static int xOffset;
    private static int yOffset;

    private static int scoreCount;
    private static int linesCount = 0;

    private static int linesCountInRound = 0;
    private static int colourLinesCountInRound = 0;

    private static int gameSpeed = 900;

    private static int[] currentXCord = new int[4],
                         currentYCord = new int[4];

    private static boolean[] markedLines = new boolean[LINE_NUMBERS];

    private static JFrame window;
    private static FieldPanel field;
    private static JButton startGame, exit;
    private static JLabel score = new JLabel(), 
                          lines = new JLabel();

    private static Timer timer = new Timer();

    private SpeedMonitor speedMonitor = new SpeedMonitor();
    private AtomicBoolean active = new AtomicBoolean(true);

    private static Cell[][] staticField,  // Field that contains already placed figures. Fixed shape positions.
                            dynamicField; // And this is dynamic field which holds only currently moving figure.
    
    private static Shape shape;

    private static File scoresResulFile = new File("./resultScores.txt");


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        new Tetris();
    }

    Tetris() {
        window = new JFrame("Tetris");
        window.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        window.setResizable(false);

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (dim.width - WINDOW_WIDTH) / 2,
            y = (dim.height - WINDOW_HEIGHT) / 2;

        window.setLocation(x, y);
        window.setFocusable(false);
        window.getContentPane().add(setGUI());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }

    private JPanel setGUI() {
        JPanel main = new JPanel(new BorderLayout());
        
        field = new FieldPanel();
        field.setBorder(new TitledBorder(""));
        field.setFocusable(true);

        createField();
        main.add(field, BorderLayout.CENTER);

        startGame = new JButton("Start game");
        startGame.addActionListener(new ButtonListener());
        startGame.setFocusable(false);
        exit = new JButton("Exit");
        exit.addActionListener(new ButtonListener());
        exit.setFocusable(false);

        JPanel buttons = new JPanel();
        buttons.add(startGame);
        buttons.add(exit);

        main.add(buttons, BorderLayout.SOUTH);

        return main;
    }

    class FieldPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(dynamicImage, null, xOffset, yOffset);
            
            score.setText("Score : " + scoreCount);
            lines.setText("Number of lines : " + getCountOfLines());

            g2d.drawString(score.getText(), xOffset + 150, yOffset + 370);
            g2d.drawString(lines.getText(), xOffset      , yOffset + 370);
        }
    }

    private void createField() {
        dynamicImage = new BufferedImage(CELL_SIZE * COLUMN_NUMBERS + 2, CELL_SIZE * LINE_NUMBERS, BufferedImage.TYPE_INT_RGB);

        xOffset = (WINDOW_WIDTH - CELL_SIZE * COLUMN_NUMBERS) / 3 - 2;
        yOffset = (WINDOW_HEIGHT - CELL_SIZE * LINE_NUMBERS) / 9;

        dynamicField = new Cell[COLUMN_NUMBERS][LINE_NUMBERS];
        staticField  = new Cell[COLUMN_NUMBERS][LINE_NUMBERS];

        for(int i = 0; i < COLUMN_NUMBERS; i++) {
            for(int j = 0; j < LINE_NUMBERS; j++) {
                dynamicField[i][j] = new Cell();
                staticField[i][j] = new Cell();
            }
        }

        drawField();
    }

    private void drawField() {
        Graphics dynamicGraph = dynamicImage.getGraphics();

        for(int x = 0; x < COLUMN_NUMBERS; x++) {
            for(int y = 0; y < LINE_NUMBERS; y++) {
                dynamicGraph.setColor(dynamicField[x][y].getColor());
                dynamicGraph.fillRect(x * CELL_SIZE + 2, y * CELL_SIZE, CELL_SIZE - 2, CELL_SIZE - 2);
            }
        }

        field.repaint();
    }

    private class TetrisKeyListener extends KeyAdapter
    {
        public void keyPressed(KeyEvent e)
        {
            if(e.getKeyCode() == KeyEvent.VK_LEFT)
            {
                moveLeft();
                checkLeftBorder();
                updateShapeInField();
            }

            if(e.getKeyCode() == KeyEvent.VK_RIGHT)
            {
                moveRight();
                checkRightBorder();
                updateShapeInField();
            }

            if(e.getKeyCode() == KeyEvent.VK_UP)
            {
                int countOfTurns = shape.getCountOfTurn(); // save previous state

                shape.incrementCountOfTurn();
                shape.rotateShape();

                if( checkRotate() == false )
                {
                    // restore previous shape form on the field
                    shape.setCountOfTurn(countOfTurns);
                    shape.rotateShape();
                }
                drawShape();
                drawField();
	        }

            if(e.getKeyCode() == KeyEvent.VK_DOWN)
            {
                //updateShapeInField();
                moveDown();
                checkBottomCollision();
                updateShapeInField();
            }

            if(e.getKeyCode() == KeyEvent.VK_SPACE)
            {
            	dropShapeDown();
            	for(int y = 0; y < LINE_NUMBERS; y++) {
            		getCountOfLines();
            		deleteLines();
            	}

            	switch(colourLinesCountInRound) {
            		case 1: scoreCount += 40; break;
            		case 2: scoreCount += 100; break;
            		case 3: scoreCount += 400; break;
            		case 4: scoreCount += 1000; break;
            	}
            	
            	linesCountInRound = colourLinesCountInRound = 0;
                
                createFigure();
                setDisplacementToDefault();
                updateShapeInField();
            }
        }
    }

    private class ButtonListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if(e.getSource() == startGame)
            {
                field.addKeyListener(new TetrisKeyListener());
                createFigure();
                timer.schedule(new MyTimer(), 0, gameSpeed);
                speedMonitor.start();
                startGame.setEnabled(false);
            }
            else
            {
                System.exit(0);
            }
        }
    }

    private void createFigure() {
        int rnd = ThreadLocalRandom.current().nextInt(Shape.ShapeType.values().length);

        shape = new Shape(Shape.ShapeType.values()[rnd]);
    }

    private void drawShape() {
        int[][] space = shape.getShapeSpace();

        for(int i = 0; i < dynamicField.length; i++) {
            for(int j = 0; j < dynamicField[i].length; j++) {
                dynamicField[i][j].setColor(staticField[i][j].getColor());
            }
        }

        /*
            Since shape of figure represent as int[][] array like
            {
                {1,0,0},
                {1,1,0},
                {0,1,0}
            }
            then we need to detect first collision with bottom line.
            If found => decrement displacementY (i.e. lift figure by 1 cell up)
         */
        exit:
        for(int x = 0; x < space.length; x++) {
            for (int y = 0; y < space[x].length; y++) {
                if (space[y][x] == 1) {
                    if (y + displacementY >= LINE_NUMBERS) {
                        displacementY--;
                        break exit;
                    }
                }
            }
        }

        int curCordIndex = 0;
        for(int x = 0; x < space.length; x++) {
            for(int y = 0; y < space[x].length; y++) {
                if(space[y][x] == 1) {
                    dynamicField[x + displacementX][y + displacementY].setColor(shape.getShapeColor());

                    currentXCord[curCordIndex] = x + displacementX;
                    currentYCord[curCordIndex++] = y + displacementY;
                }
            }
        }
    }

    private void moveDown() {
        displacementY++;
    }

    private void dropShapeDown() {
        int maxY = currentYCord[0];

        for(int i = 1; i < currentYCord.length; i++) {
            if(maxY < currentYCord[i]) {
                maxY = currentYCord[i];
            }
        }

        for(int out = maxY; out < LINE_NUMBERS; out++) {
            for(int y = 0; y < currentYCord.length; y++) {
                currentYCord[y]++;
            }

            for(int i = 0; i < currentYCord.length; i++) {
                if(currentYCord[i] == LINE_NUMBERS) {
                    for(int j = 0; j < currentYCord.length; j++) {
                        staticField[currentXCord[j]][currentYCord[j] - 1].setStatus(false);
                        staticField[currentXCord[j]][currentYCord[j] - 1].setColor(shape.getShapeColor());
                    }
                    return;
                }
                if(staticField[currentXCord[i]][currentYCord[i]].isEmpty() == false) {
                    for(int j = 0; j < currentYCord.length; j++) {
                        staticField[currentXCord[j]][currentYCord[j] - 1].setStatus(false);
                        staticField[currentXCord[j]][currentYCord[j] - 1].setColor(shape.getShapeColor());
                    }
                    return;
                }
            }
        }
    }

    private void moveLeft() {
        displacementX--;
    }

    private void moveRight() {
        displacementX++;
    }

    private void setDisplacementToDefault() {
        displacementX = 3;
        displacementY = 0;
    }

    private void updateShapeInField() {
        drawShape();
	    drawField();
    }

    private void checkLeftBorder() {
        int[][] space = shape.getShapeSpace();

        for (int x = 0; x < space.length; x++) {
            for (int y = 0; y < space[x].length; y++) {
                if(space[y][x] == 0) continue; // skip empty shape space

                if(x + displacementX < 0) {
                    displacementX++;
                    return;
                }

                if (staticField[x + displacementX][y + displacementY].isEmpty() == false) {
                    displacementX++;
                    return;
                }
            }
        }
    }
    
    private void checkRightBorder() {
        int[][] space = shape.getShapeSpace();

        for (int x = 0; x < space.length; x++) {
            for (int y = 0; y < space[x].length; y++) {
                if(space[y][x] == 0) continue; // skip empty shape space

                if(x + displacementX >= COLUMN_NUMBERS)
                {
                    displacementX--;
                    return;
                }

                if (staticField[x + displacementX][y + displacementY].isEmpty() == false) {
                    displacementX--;
                    return;
                }
            }
        }
    }

    private boolean checkRotate()
    {
        int[][] space = shape.getShapeSpace();

        for (int x = 0; x < space.length; x++) {
            for (int y = 0; y < space[x].length; y++) {
                if (space[y][x] == 1) {
                    // we have reached border
                    if( (x + displacementX >= 10) || (x + displacementX < 0) ) {
                        return false;
                    }
                    // or we stumbled upon an occupied cell
                    if( !staticField[x + displacementX][y + displacementY].isEmpty() ) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void checkBottomCollision()
    {
        int[][] space = shape.getShapeSpace();

        for (int x = 0; x < space.length; x++) {
            for (int y = 0; y < space[x].length; y++) {
                if (space[y][x] == 1) {
                    if(y + displacementY >= LINE_NUMBERS) {
                        displacementY--;
                        return;
                    }

                    if( !staticField[x + displacementX][y + displacementY].isEmpty() ) {
                        displacementY--;
                        return;
                    }
                }
            }
        }
    }

    private boolean isDescendingMoveGood()
    {
        for(int i = 0; i < currentXCord.length; i++)
        {
            if(currentYCord[i]+1 >= LINE_NUMBERS) return false;
            if(staticField[currentXCord[i]][currentYCord[i]+1].isEmpty() == false) return false;
        }

        return true;
}

    private void saveLastShapeCoords() {
        for(int index = 0; index < currentXCord.length; index++) {
            staticField[currentXCord[index]][currentYCord[index]].setColor(shape.getShapeColor());
            staticField[currentXCord[index]][currentYCord[index]].setStatus(false);
        }
    }

    private int getCountOfLines() {
        int size = 0;
        int scoreInRound = 0;

        toNewLine:
        for(int y = 0; y < LINE_NUMBERS; y++) {
            for(int x = 0; x < COLUMN_NUMBERS; x++) {
                if(staticField[x][y].isEmpty() == false) {
                    size++;
                } else {
                    size = 0;
                    continue toNewLine;
                }
                if(size == COLUMN_NUMBERS) {
                    size = 0;
                    markedLines[y] = true;
                    
                    boolean yn = true;
                    Color c = staticField[0][y].getColor();
                    for(int x_ = 1; x_ < COLUMN_NUMBERS; x_++) {
                    	if(staticField[x_][y].getColor().equals(c)) continue;
                    	yn = false; break;
                    }
                    
                    if(yn == true) colourLinesCountInRound++; 
                    
                    linesCount++; 
                    linesCountInRound++;
                    
                    if(linesCountInRound == 1) {
                    	scoreInRound = 10;
                    	continue toNewLine;
                    }
                    
                    scoreInRound = scoreInRound + scoreInRound;
                }
            }
        }
        
        scoreCount += scoreInRound;
        
        return linesCount;
    }

    private void deleteLines() {
        for(int i = markedLines.length - 1; i >= 0; i--) {
            if(markedLines[i] == true) {
            	reload:
                for(int y = LINE_NUMBERS - 1; y > 0; y--) {
                    for(int x = 0; x < COLUMN_NUMBERS; x++) {
                        if(staticField[x][y].isEmpty() == false) {
                            staticField[x][y].setColor(staticField[x][y - 1].getColor());
                            staticField[x][y].setStatus(staticField[x][y - 1].isEmpty());
                        }
                    }

                    markedLines[i] = false;
                    continue reload; 
                }
            }
        }
    }

    private void checkGameOver()
    {
        int[][] space = shape.getShapeSpace();

        for (int x = 0; x < space.length; x++) {
            for (int y = 0; y < space[x].length; y++) {
                if (space[y][x] == 1) {
                    if(staticField[x + displacementX][y + displacementY].isEmpty() == false) {
                        speedMonitor.stopSpeedMonitor();

                        JLabel message = new JLabel("<html><p align=center>" + "<b>Game Over</b><br>Your score: " + scoreCount + "<br>Lines: " + linesCount);
                        message.setHorizontalAlignment(SwingConstants.CENTER);

                        JOptionPane.showMessageDialog(window, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
                        timer.cancel();
                        saveGameResults();

                        return;
                    }
                }
            }
        }
    }

    private void saveGameResults()
    {
        try
        {
            if (!scoresResulFile.exists())
            {
                scoresResulFile.createNewFile();

                try (BufferedWriter bw = new BufferedWriter(new FileWriter(scoresResulFile)))
                {
                    bw.write("Date/Time\t\tLines\tScore");
                    bw.newLine();
                    bw.write("---------\t\t-----\t-----\t");
                    bw.newLine();
                }
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formatDateTime = LocalDateTime.now().format(formatter);

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(scoresResulFile, true)))
            {
                bw.write(formatDateTime + "\t" + linesCount + "\t" + scoreCount);
                bw.newLine();
            }
        }
        catch (IOException e)
        {
            System.err.println("Unable to save game results into file");
        }
    }

    class MyTimer extends TimerTask
    {
        public void run()
        {
            if(isDescendingMoveGood() == false) {
                saveLastShapeCoords();

                // Check again that all lines were deleted
                getCountOfLines();
                deleteLines();
                //

                createFigure();
                setDisplacementToDefault();
                checkGameOver();
                updateShapeInField();
            }
            
            updateShapeInField();
            moveDown();
        }
    }

    class SpeedMonitor extends Thread
    {
        private int secondsHavePassed = 0;

        @Override
        public void run()
        {
            while (active.get())
            {
                checkAndIncreaseSpeed();

                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                secondsHavePassed++;
            }
        }

        private void checkAndIncreaseSpeed()
        {
            // increase speed every minute
            if(secondsHavePassed != 0 && secondsHavePassed % 60 == 0)
            {
                gameSpeed -= 100;
                timer.cancel();

                timer = new Timer();
                timer.schedule(new MyTimer(), 0, gameSpeed);
            }
        }

        private void stopSpeedMonitor()
        {
            active.set(false);
        }
    }
}