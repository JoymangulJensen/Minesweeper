/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.util.Observable;
import java.util.Random;
import static javax.swing.Spring.height;
import static javax.swing.Spring.width;

/**
 *
 * @author p1508754
 */
public class Board extends Observable {

    private Case[][] board;
    private int row;
    private int col;
    private int nbBomb;

    public Case[][] getBoard() {
        return board;
    }

    public void setBoard(Case[][] board) {
        this.board = board;
    }

    public int getRow()
    {
        return row;
    }

    public void setRow(int lig)
    {
        this.row = lig;
    }

    public int getCol()
    {
        return col;
    }

    public void setCol(int col)
    {
        this.col = col;
    }
    
    /**
     * Constructor
     * @param row Number of rows in the playing grid
     * @param col Number of columns in the playing grid
     * @param bomb Number of bomb to be generated on the grid
     */
    public Board(int row, int col, int bomb) {
        this.board = new Case[row][col];
        this.setCol(col);
        this.setRow(row);
        this.nbBomb = bomb;
        
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                board[i][j] = new Case();
            }
        }
        generateBomb();
    }

    /**
     * Set a flog on the grid according to the coordinates entered in the arguments
     */
    public void rightClick(int row, int col) 
    {
        if(!this.getCase(row, col).isVisible())
        {
            this.getCase(row, col).setFlag();
            this.update();
        }
    }
    
    public void leftClick(int row, int col)
    {
        this.getCase(row, col).setVisible(true);
        this.update();
    }
    
    public void generateBomb()
    {
        Random r = new Random();
		
        int i_random;
        int j_random;
        for (int i=0; i<nbBomb; i++)
        {
            do
            {
                i_random = r.nextInt(this.getRow());
                j_random = r.nextInt(this.getCol());
            } while(board[i_random][j_random].isTrap());
            System.out.println(i_random + "   " + j_random);
            board[i_random][j_random].setTrap(true);
        }
    }

    /**
     * 
     * @return The square according to the coordinates
     */
    public Case getCase(int i, int j) {
        return this.board[i][j];
    }

    public void update() {
        // Notify the view to update
        setChanged();
        notifyObservers();
    }

}
