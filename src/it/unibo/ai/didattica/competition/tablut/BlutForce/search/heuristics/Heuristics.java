package it.unibo.ai.didattica.competition.tablut.BlutForce.search.heuristics;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import java.util.Arrays;
import java.util.List;

public abstract class Heuristics {

    public final static double startingBlackPawns = 16.0;
    public final static double startingWhitePawns = 8.0;
    public final static int best_positions_threshold = 3;

    private final List<String> good_escape_cells = Arrays.asList("a3", "a7", "c1", "c9", "g1", "g9", "i3", "i7");
    private final List<String> transition_cells = Arrays.asList("c5", "g5", "e3", "e7");
    private List<String> nearCastle = Arrays.asList("e4", "e6", "f5", "d5");
    private List<String> best_positions = Arrays.asList("e3", "e4", "e6", "e7", "c5", "d5", "f5", "g5");

    // Evaluate the given state
    abstract public double evaluateState(State s);
    

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

    public static Boolean equalsPawn(State state, Pawn pawn, int row, int column) {
        return state.getPawn(row, column).equalsPawn(Pawn.WHITE.toString());
    }

    public Boolean isKingProtected(State state) {
        int[] kingCoord = this.getKing(state);
        int row;
        int column;
        row = kingCoord[0];
        column = kingCoord[1];
        int surround = 0;
        if(equalsPawn(state, Pawn.WHITE, row - 1, column))
            surround++;
        if(equalsPawn(state, Pawn.WHITE, row + 1, column))
            surround++;
        if(equalsPawn(state, Pawn.WHITE, row, column - 1))
            surround++;
        if(equalsPawn(state, Pawn.WHITE, row, column + 1))
            surround++;
        return surround>=3;
    }

    public Boolean isKingInCastle(State state) {
        return  state.getPawn(4,4).equalsPawn(Pawn.KING.toString());
    }

    public static int[] getCoordsFromBox(String box){
        int[] coords = new int[2];
        char r = box.charAt(0);
        char c = box.charAt(1);
        coords[0] = r - 97;
        coords[1] = c - 1;
        return coords;
    }
    public Boolean isBestPosition(State state){
        int goodPosition = 0;
        for (String box : this.best_positions) {
            int[] coords = getCoordsFromBox(box);
            if(equalsPawn(state, Pawn.WHITE, coords[0], coords[1]))
                goodPosition++;
        }
        return goodPosition >= best_positions_threshold;
    }

    public Boolean isKingNearCastle(State state) {
        for (String box : this.nearCastle) {
            int[] coords = getCoordsFromBox(box);
            if(equalsPawn(state, Pawn.KING, coords[0], coords[1]))
                return true;
        }
        return false;
    }

    public Boolean isPathFree() {
        return true;
    }
}