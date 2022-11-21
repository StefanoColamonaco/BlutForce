package it.unibo.ai.didattica.competition.tablut.BlutForce.search.heuristics;

import it.unibo.ai.didattica.competition.tablut.BlutForce.search.BlutForceGame;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import java.util.Arrays;
import java.util.List;

public abstract class Heuristics {

    protected final static double startingBlackPawns = 16.0;
    protected final static double startingWhitePawns = 8.0;
    protected final static int best_positions_threshold = 3;

    protected final List<String> good_escape_cells = Arrays.asList("a3", "a7", "c1", "c9", "g1", "g9", "i3", "i7");
    protected final List<String> transition_cells = Arrays.asList("c5", "g5", "e3", "e7");
    protected List<String> nearCastle = Arrays.asList("e4", "e6", "f5", "d5");
    protected List<String> best_positions = Arrays.asList("e3", "e4", "e6", "e7", "c5", "d5", "f5", "g5");
    protected List<String> black_camps = Arrays.asList("a4","a5","a6","b5","d1","e1","f1","e2","d9","e9","f9","e8","i4","i5","i6","h5");
    
    // Evaluate the given state
    abstract public double evaluateState(State s);
    protected BlutForceGame game;


    public Heuristics(BlutForceGame game) {
        this.game = game;
    }
    

    public int capturedOf(State state, Pawn color) {
        double starting = startingWhitePawns;
        if (color == Pawn.BLACK){
            starting = startingBlackPawns;
        }
        return (int) starting - state.getNumberOf(color);
    }

    public int[] getKing(State state) {
        String board = state.boardString();
        String rows[] = board.split("\n");
        int column = -1;
        int[] kingCoord = new int[]{-1, -1};
        for (int row = 0; row < rows.length; row++) {
            column = rows[row].indexOf(Pawn.KING.toString());
            if(column != -1) {
                kingCoord[0] = row;
                kingCoord[1] = column; 
                break;
            }
        }

        return kingCoord;
    }

    public static int conversion(Boolean boolVal) {
        int val = boolVal ? 1 : 0;
        return val;
    }

    public static Boolean isPawnInCoords(State state, int row, int column,  Pawn pawn) {
        return state.getPawn(row, column).equalsPawn(pawn.toString());
    }

    public static int[] getCoordsFromBox(String box){
        int[] coords = new int[2];
        char r = box.charAt(0);
        char c = box.charAt(1);
        coords[0] = r - 97; // a = 97
        coords[1] = c - 48; // 0 = 48
        return coords;
    }

    // Given a state, a row, a column and a pawn, it returns the number of pawns of the same color that are around the coordinates (row, column)
    public int numberOfColorPawnAroundCoords(State state, int row, int column, Pawn pawn){
        int count = 0;
        if (row > 0 && isPawnInCoords(state, row - 1, column, pawn))
            count++;
        if (row < 9 && isPawnInCoords(state, row + 1, column, pawn))
            count++;
        if (column > 0 && isPawnInCoords(state, row, column - 1, pawn))
            count++;
        if (column < 9 && isPawnInCoords(state, row, column + 1, pawn))
            count++;
        return count;
    }

    public Boolean isKingInCastle(State state) {
        return  state.getPawn(4,4).equalsPawn(Pawn.KING.toString());
    }

    public Boolean isKingNearCastle(State state){
        int[] kingCoord = getKing(state);
        return this.nearCastle.contains(state.getBox(kingCoord[0], kingCoord[1]));
    }

    // Given a state and a List of possible boxes, return true iff the king can reach one of the boxes
    public Boolean isReachable(State state, List<String> positions) {
        int[] coords = this.getKing(state);
        String from = state.getBox(coords[0], coords[1]); 
        for (String to : positions) {
            // int[] coords = getCoordsFromBox(box);
            try {
                Action a = new Action(from, to, state.getTurn());
                this.game.checkPossibleMove(state, a);
                return true;
            } catch (Exception e) {
                continue;
            }
        }
        return false;           
    }
    
    public Boolean isEscapeFree(State state) {
        return isReachable(state, this.good_escape_cells);
    }

    public Boolean isEscapePathFree(State state) {
        return isReachable(state, this.transition_cells);
    }

    public Boolean isKingUnderAttack(State state){
        int[] kingCoords = this.getKing(state);
        int kingRow = kingCoords[0];
        int kingColumn = kingCoords[1];
        return numberOfColorPawnAroundCoords(state, kingRow, kingColumn, Pawn.BLACK) >= 1;
    }

    public Boolean isKingAlmostCaptured(State state){
        int[] kingCoords = this.getKing(state);
        int kingRow = kingCoords[0];
        int kingColumn = kingCoords[1];
        if (this.isKingInCastle(state)){
            return numberOfColorPawnAroundCoords(state, kingRow, kingColumn, Pawn.BLACK) == 3;
        }
        else if (this.isKingNearCastle(state)){
            return numberOfColorPawnAroundCoords(state, kingRow, kingColumn, Pawn.BLACK) == 2;
        }
        else {
            return false;
        }
    }
    
}