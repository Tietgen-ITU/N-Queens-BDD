import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import net.sf.javabdd.*;

public class ANTIQueenLogic implements IQueensLogic {

    private int[][] board;
    private BDD mainBDD;
    private BDD[] variables;
    private BDD TRUE;
    private BDD FALSE;

    private BDDFactory factory;
    private int size;


    @Override
    public void initializeBoard(int size) {
        this.size = size;
        board = new int[this.size][this.size];
        mainBDD = initializeBDD(this.size);
    }

    @Override
    public int[][] getBoard() {
        updateBoard();
        return board;
    }

    @Override
    public void insertQueen(int column, int row) {

        placeQueen(column, row);
        System.out.println("Clicked");
    }

    private void updateBoard() {
        for(int column = 0; column < board.length; column++)
            for (int row = 0; row < board[column].length; row++) 
                if (board[column][row] == 0)
                    board[column][row]  = evaluatePosition(column, row);
    }

    private BDD initializeBDD(int size) {

        // Init variables
        factory = JFactory.init(size*size, size);
        TRUE = factory.one();
        FALSE = factory.zero();
        variables = new BDD[size*size];
        factory.setVarNum(size*size);
        BDD temp = TRUE;
        
        // Create variables
        // for (int i = 0; i < size; i++)
        //     for (int j = 0; j < size; j++)  
        //         variables[translatePosition(i, j)] = factory.ithVar(translatePosition(i, j));
                
        
        for (int column = 0; column < size; column++) {

            for (int row = 0; row < size; row++) {

                System.out.println(translatePosition(column, row));
                BDD current = createHorizontalAndVerticalRules(column, row);

                temp = temp.apply(current, BDDFactory.and);
                variables[translatePosition(column, row)] = current;
            }
        }


        return temp;
    }

    private int evaluatePosition(int column, int row) {
        BDD testBDD = mainBDD.restrict(factory.ithVar(translatePosition(column, row)));

        if(testBDD.isOne()) {
            return 1;
        } else if (testBDD.isZero()) {
            return -1;
        }

        return 0;
    }

    private BDD createHorizontalAndVerticalRules(int column, int row) {
        BDD queenHasBeenPlaced = FALSE;
        BDD columnAndRowFalseRule = TRUE;

        columnAndRowFalseRule = createRule(getVariablesFromSameRow(column, row), columnAndRowFalseRule, (acc, val) -> acc != null ? acc.and(factory.nithVar(val)) : factory.nithVar(val));
        columnAndRowFalseRule = createRule(getVariablesFromSameColumn(column, row), columnAndRowFalseRule, (acc, val) -> acc != null ? acc.and(factory.nithVar(val)) : factory.nithVar(val));
        
        queenHasBeenPlaced.orWith(factory.nithVar(translatePosition(column, row)));
        queenHasBeenPlaced.orWith(columnAndRowFalseRule);
        return queenHasBeenPlaced;
    }

    private void placeQueen(int row, int column) {
        mainBDD.restrictWith(factory.ithVar(translatePosition(column, row)));
        board[row][column] = 1;
    }

    /* UTIL FUNCTIONS */

    /**
     * Translate the position to an interger representation
     * @param column
     * @param row
     * @return Returns an integer value
     */
    private int translatePosition(int column, int row) {

        return (size * column) + row;
    }

    private BDD createRule(IntStream varIds, BDD acc, Accumulater<BDD, Integer> accumulater) {
        
        BDD tempBody = acc;

        for (int i : varIds.toArray()) {
            tempBody = accumulater.apply(tempBody, i);
        }

        return tempBody;
    }

    private BDD createRule(IntStream varIds, Accumulater<BDD, Integer> accumulater) {
        
        return createRule(varIds, null, accumulater);
    }

    private IntStream getVariablesFromSameRow(int row, int column) {

        return IntStream.range(0, size).filter(i -> i != column).map(i -> translatePosition(i, row));
    }

    private IntStream getVariablesFromSameColumn(int row, int column) {

        return IntStream.range(0, size).filter(i -> i != row).map(i -> translatePosition(column, i));
    }

    private interface Accumulater<T, U> {
    
        T apply(T acc, U val);
    }
}
