package it.unibo.ai.didattica.competition.tablut.BlutForce.search.heuristics;

import java.util.HashMap;
import java.util.Map;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.BlutForce.search.BlutForceGame;

public class WhiteHeuristics extends Heuristics{
    
    private final String ESCAPE_FREE = "escapeFree";
    private final String ESCAPE_PATH_FREE = "escapePathFree";
    private final String BEST_POSITIONS = "bestPositions";
	private final String KING_IN_CASTLE = "kingInCastle";
    private final String KING_NEAR_CASTLE = "kingNearCastle";
    private final String KING_PROTECTED = "kingProtected";
    private final String BLACK_EATEN = "blackEaten";
    private final String WHITE_LOSED = "whiteLosed";
    private final String KING_ALMOST_CAPTURED = "kingAlmostCaptured";
    private final String PAWNS_UNDER_ATTACK = "pawnsUnderAttack";

    private final int blackEatenTreshold = 5;

	private final Map<String,Double> weights;

    public WhiteHeuristics(BlutForceGame game){
        super(game);
        this.weights=new HashMap<String,Double>();
        this.weights.put(this.KING_PROTECTED, 130.0);     // king have >= 3 white pawns in the four adjacent cells
        this.weights.put(this.KING_IN_CASTLE, 75.0);      // king positioned in castle
        this.weights.put(this.BEST_POSITIONS, 70.0);      // treshold (between 4 and 7 included..?) <= number of white pawns positioned in: "e (3,4,6,7)", "(c,d,f,g) 5"
        this.weights.put(this.KING_NEAR_CASTLE, 25.0);    // king positioned in one of the four cells around the castle
        this.weights.put(this.WHITE_LOSED, -90.0);         // for each losed white pawn
        this.weights.put(this.BLACK_EATEN, 100.0);          // for each eaten black pawn
        this.weights.put(this.ESCAPE_FREE, 80.0);         // no pawns between king and one of: "a3","a7" "c1","c9","g1","g9","i3","i7"
        this.weights.put(this.ESCAPE_PATH_FREE, 75.0);    // no pawns between king and transition cells (one of): "c5","g5","e3","e7"
        this.weights.put(this.KING_ALMOST_CAPTURED, -80.0);
        this.weights.put(this.PAWNS_UNDER_ATTACK, -20.0);
    }
    /* Considerations:
     *  king near castle or in castle + king protected -> bonus
     *  black eaten >= treshold + one between escape free and escape path free -> bonus
     */

    @Override
    public double evaluateState(State state) {
        
        double king_protected = conversion(this.isKingProtected(state)) * weights.get(KING_PROTECTED);
        double king_near_castle = conversion(this.isKingNearCastle(state)) * weights.get(KING_NEAR_CASTLE);
        double best_positions = conversion(this.isBestPosition(state)) * weights.get(BEST_POSITIONS) * (conversion(this.isKingNearCastle(state)) + conversion(this.isKingInCastle(state))); 
        double king_in_castle = conversion(this.isKingInCastle(state)) * weights.get(KING_IN_CASTLE);
        double escape_free = conversion(this.isEscapeFree(state)) * weights.get(ESCAPE_FREE) * conversion(isKingUnderAttack(state));
        double escape_path_free = conversion(this.isEscapePathFree(state)) * weights.get(ESCAPE_PATH_FREE);
        double num_white = (startingWhitePawns - state.getNumberOf(State.Pawn.WHITE)) * weights.get(WHITE_LOSED);
        double num_black = (startingBlackPawns - state.getNumberOf(State.Pawn.BLACK)) * weights.get(BLACK_EATEN);
        double king_almost_captured = conversion(this.isKingAlmostCaptured(state)) * weights.get(KING_ALMOST_CAPTURED);
        double pawns_under_attack = this.numPawnsUnderAttack(state) * weights.get(KING_ALMOST_CAPTURED);

        double bonus = 0;
        if( king_near_castle > 0 && king_protected > 0){
            bonus = bonus + 40;
        }
        if(  (startingBlackPawns - state.getNumberOf(State.Pawn.BLACK)) >= blackEatenTreshold && escape_free > 0){
            bonus = bonus + 70;
            this.weights.put(this.KING_IN_CASTLE, 35.0); 
            this.weights.put(this.ESCAPE_FREE, 100.0);
            this.weights.put(this.BEST_POSITIONS, 10.0);
            this.weights.put(this.KING_NEAR_CASTLE, 15.0); 
            this.weights.put(this.BLACK_EATEN, 45.0);
        }

        return king_protected + best_positions + king_in_castle + king_near_castle + escape_free + escape_path_free + num_white + num_black + king_almost_captured + pawns_under_attack +bonus;
    }

    public Boolean isKingProtected(State state) {
        int[] kingCoord = this.getKing(state);
        int row;
        int column;
        row = kingCoord[0];
        column = kingCoord[1];
        return this.numberOfColorPawnAroundCoords(state, row, column, Pawn.WHITE) >= 2;
    }
    
    public Boolean isBestPosition(State state){
        int goodPosition = 0;
        for (String box : this.best_positions) {
            int[] coords = getCoordsFromBox(box);
            if(isPawnInCoords(state, coords[0], coords[1], Pawn.WHITE))
                goodPosition++;
        }
        return goodPosition >= best_positions_threshold;
    }

    public Boolean isKingInCoords(State state, int row, int column){
        int[] kingCoord = this.getKing(state);
        return kingCoord[0] == row && kingCoord[1] == column;
    }

    public Boolean isFreeCell(State state, int row, int column){
        if (row<0 || row>8 || column<0 || column>8)
            return false;
        return !isPawnInCoords(state, row, column, Pawn.BLACK) && !isPawnInCoords(state, row, column, Pawn.WHITE);
    }

    public Boolean isBlackCamp(State state, int row, int column){
        for (String box : this.black_camps) {
            if (box.equals(state.getBox(row, column)))
                return true;
        }
        return false;
    }

    public Boolean blackCanAttack(State state, int row, int column){
        for (int c = 0; c < 9; c++) {
                if (isPawnInCoords(state, row, c, Pawn.BLACK)){
                    String from = state.getBox(row, c); 
                    String to = state.getBox(row, column);
                    try {
                        Action a = new Action(from, to, state.getTurn());
                        this.game.checkPossibleMove(state, a);
                        return true;
                    } catch (Exception e) {
                        continue;
                    }
                }
        }
        for (int r = 0; r < 9; r++) {
            if (isPawnInCoords(state, r, column, Pawn.BLACK)){
                String from = state.getBox(r, column); 
                String to = state.getBox(row, column);
                try {
                    Action a = new Action(from, to, state.getTurn());
                    this.game.checkPossibleMove(state, a);
                    return true;
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return false;    
    }

    public Integer numPawnsUnderAttack(State state){ // base version
        int num = 0;
        Boolean isUnderAttack = false;
        for (int row = 0; row < 9; row++) {
            for (int column = 0; column < 9; column++) {
                isUnderAttack = false;
                if(isPawnInCoords(state, row, column, Pawn.WHITE) && !isKingInCoords(state, row, column)){
                    if(row<8 
                    && (isPawnInCoords(state, row+1, column, Pawn.BLACK) || isPawnInCoords(state, row+1, column, Pawn.THRONE) || (isPawnInCoords(state,row+1,column,Pawn.KING) && isKingInCastle(state)) || isBlackCamp(state, row+1, column))
                    && isFreeCell(state, row-1, column)
                    && blackCanAttack(state, row-1, column))
                        isUnderAttack = true;
                    if(row>0 
                    && (isPawnInCoords(state, row-1, column, Pawn.BLACK) || isPawnInCoords(state, row-1, column, Pawn.THRONE) || (isPawnInCoords(state,row-1,column,Pawn.KING) && isKingInCastle(state)) || isBlackCamp(state, row-1, column))
                    && isFreeCell(state, row+1, column)
                    && blackCanAttack(state, row+1, column))
                        isUnderAttack = true;
                    if(column<8
                    && (isPawnInCoords(state, row, column+1, Pawn.BLACK) || isPawnInCoords(state, row, column+1, Pawn.THRONE) || (isPawnInCoords(state,row,column+1,Pawn.KING) && isKingInCastle(state)) || isBlackCamp(state, row, column+1)) 
                    && isFreeCell(state, row, column-1)
                    && blackCanAttack(state, row, column-1))
                        isUnderAttack = true;
                    if(column>0
                    && (isPawnInCoords(state, row, column-1, Pawn.BLACK) || isPawnInCoords(state, row, column-1, Pawn.THRONE) || (isPawnInCoords(state,row,column-1,Pawn.KING) && isKingInCastle(state)) || isBlackCamp(state, row, column-1))
                    && isFreeCell(state, row, column+1)
                    && blackCanAttack(state, row, column+1))
                        isUnderAttack = true;
                }
                if (isUnderAttack)
                    num++;
            }
        }        
        return num;
    }
}

