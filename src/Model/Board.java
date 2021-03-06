/*
 * Polytech Lyon - 2016
 * Jensen JOYMANGUL & Gaetan MARTIN
 * Projet Informatique 3A - Creation d'un demineur MVC
 */
package Model;

import static Model.CaseState.DISCOVERED;
import static Model.CaseState.EMPTY;
import static Model.CaseState.FLAGGED;
import static Model.CaseState.TRAPPED;
import static Model.CaseState.UNDISCOVERED;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Random;

/**
 * Class Board2D representing the model side of the game
 */
public abstract class Board extends Observable {

    protected List<List<Case>> board;
    protected int nbBomb; //Number of bomb
    protected int nbFlag;
    private GameTimer timer;
    private int score;

    /**
     * Represents the state of the game
     */
    protected GameState state;

    public GameState getState() {
        return state;
    }

    public List<List<Case>> getBoard() {
        return board;
    }

    public int getNbFlag() {
        return nbFlag;
    }

    public GameTimer getTimer() {
        return timer;
    }

    public int getScore() {
        return score;
    }

    /**
     * Constructor
     *
     * @param row Number of rows in the playing grid
     * @param col Number of columns in the playing grid
     * @param bomb Number of bomb to be generated on the grid
     */
    public Board(int row, int col, int bomb) {
        this.nbBomb = bomb;
        this.nbFlag = 0;
        this.state = GameState.RUNNING;
        this.board = createBoard(row, col);
        this.timer = new GameTimer();

        generateBomb(board);
        setUpNeighbours();
    }

    /**
     * Create the board
     *
     * @param row
     * @param col
     * @return
     */
    protected List<List<Case>> createBoard(int row, int col) {
        List<List<Case>> grid = new ArrayList<>();
        for (int i = 0; i < row; i++) {
            grid.add(new ArrayList<>());
            for (int j = 0; j < col; j++) {
                grid.get(i).add(new Case());
            }
        }
        return grid;
    }

    /**
     * Reset the board to the initial state (empty, ready to play)
     */
    public void resetBoard() {
        for (List<Case> list : board) {
            for (Case c : list) {
                c.reset();
            }
        }
        this.nbFlag = 0;
        this.state = GameState.RUNNING;
        generateBomb(board);
        setUpNeighbours();
        this.update();
    }

    /**
     * Methode to redine the board properties when changing the difficulty level
     *
     * @param row New number of row
     * @param col New number of column
     * @param nbomb New nulber of bomb
     */
    public void changeLevel(int row, int col, int nbomb) {
        this.nbBomb = nbomb;
        this.nbFlag = 0;
        this.state = GameState.RUNNING;
        this.board = createBoard(row, col);
        generateBomb(board);
        setUpNeighbours();
    }

    /**
     * Set the neighbours for every cases
     */
    public abstract void setUpNeighbours();

    /**
     * Set a flag on the grid according to the coordinates entered in the
     * arguments
     *
     * @param row the row id of the case clicked
     * @param col the col id of the case clicked
     */
    public void rightClick(int row, int col) {

        if (gameFinished()) {
            return; // Do Nothing
        }
        Case c = this.getCase(row, col);

        switch (c.getState()) {
            case FLAGGED:
                c.setFlag(false);
                this.nbFlag--;
                break;
            case UNDISCOVERED:
                c.setFlag(true);
                this.nbFlag++;
                break;
            case TRAPPED:
                c.setFlag(true);
                this.nbFlag++;
                break;
            default:
                return;
        }
        if (gameWon()) {
            this.state = GameState.WON;
            this.manageWin();
        }
        this.update();
    }

    /**
     * Manage the left click on a case
     *
     * @param row : the row id of the case clicked
     * @param col : the col id of the case clicked
     */
    public void leftClick(int row, int col) {

        Case c = this.getCase(row, col);

        switch (c.getState()) {
            case DISCOVERED:
                return; // Do Nothing
            case EMPTY:
                return; // Do Nothing
            case TRAPPED:
                this.state = GameState.LOST;
                c.setState(CaseState.TRIGGERED);
                this.manageDefeat();
                break;
            case UNDISCOVERED:
                c.discover();
                if (!(c.computeNbBomb() > 0)) {
                    c.discoverNeighbours();
                }
                if (this.gameWon()) {
                    this.state = GameState.WON;
                    manageWin();
                }
                break;
            default:
                break;
        }
        this.update();
    }

    /**
     * Game won if all bombs are flagged or if every case undiscovered remaining
     * are bombs
     *
     * @return true if the game is won, false if not yet
     */
    protected boolean gameWon() {
        int nbUndiscovered = this.nbAllUndiscovered();
        return (nbUndiscovered == this.nbBomb
                || (nbBombsFlagged() == this.nbBomb && this.nbFlag == this.nbBomb)); // Maybe remove this
    }

    /**
     * Return the number of bombs flagged
     *
     * @return the number of bombs flagged
     */
    protected int nbBombsFlagged() {
        int nb = 0;
        for (int row = 0; row < this.getBoard().size(); row++) {
            for (int col = 0; col < this.getBoard().get(row).size(); col++) {
                Case c = this.getCase(row, col);
                if (c.isTrap() && c.isFlag()) {
                    nb++;
                }
            }
        }
        return nb;
    }

    /**
     * Test if the game is finished
     *
     * @return false if the game is still running, true else
     */
    public boolean gameFinished() {

        return (!(this.state == GameState.RUNNING));
    }

    /**
     * Manage the defeat
     */
    protected void manageDefeat() {
        if (this.state == GameState.LOST) {
            discoverAll();
        }
    }

    /**
     * Manage the victory
     */
    protected void manageWin() {
        this.score = this.getTimer().getValueInt();
        if (this.state == GameState.WON) {
            this.discoverAll();
        }
    }

    /**
     * Discover all the cases
     */
    protected void discoverAll() {
        for (int row = 0; row < this.getBoard().size(); row++) {
            for (int col = 0; col < this.getBoard().get(row).size(); col++) {
                this.getCase(row, col).discover();
            }
        }
    }

    /**
     * Return the number of cases not visible
     *
     * @return
     */
    protected int nbAllUndiscovered() {
        int counter = 0;
        for (int row = 0; row < this.getBoard().size(); row++) {
            for (int col = 0; col < this.getBoard().get(row).size(); col++) {
                if (!this.getCase(row, col).isVisible()) {
                    counter++;
                }
            }
        }
        return counter;
    }

    /**
     * Method to generate randomly a list of bombs and put it on the grid
     *
     * @param board
     */
    protected void generateBomb(List<List<Case>> board) {
        Random r = new Random();
        int i_random, j_random;
        for (int i = 0; i < nbBomb; i++) {
            do {
                i_random = r.nextInt(board.size());
                j_random = r.nextInt(board.get(i_random).size());
            } while (board.get(i_random).get(j_random).isTrap());
            System.out.println(i_random + "   " + j_random);
            board.get(i_random).get(j_random).setTrap(true);
        }
        System.out.println("");
    }

    /**
     * Get the case specified
     *
     * @param i
     * @param j
     * @return The case according to the coordinates
     */
    public Case getCase(int i, int j) {
        return this.board.get(i).get(j);
    }

    /**
     * Ask the view to update the GUI content
     */
    public void update() {
        // Notify the view to update
        setChanged();
        notifyObservers();
    }

    /**
     * Methode to return the total number of case
     *
     * @return
     */
    public int getNbCase() {
        int cmpt = 0;
        for (int i = 0; i < this.board.size(); i++) {
            for (int j = 0; j < this.board.get(i).size(); j++) {
                cmpt++;
            }
        }
        return cmpt;
    }

    @Override
    public String toString() {
        String r = "";
        for (List<Case> row : board) {
            r = row.stream().map((c) -> c + " ").reduce(r, String::concat);
            r += "\n";
        }
        return r;
    }

}
