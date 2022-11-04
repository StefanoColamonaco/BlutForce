package it.unibo.ai.didattica.competition.tablut.BlutForce.search.heuristics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unibo.ai.didattica.competition.tablut.BlutForce.search.BlutForceGame;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;

public class BlackHeuristics extends Heuristics {
    
    private final String ESCAPE_FREE = "escapeFree";
	private final String KING_IN_CASTLE = "kingInCastle";
    private final String KING_NEAR_CASTLE = "kingNearCastle";
    private final String KING_UNDER_ATTACK = "kingUnderAttack";
    private final String KING_ALMOST_CAPTURED = "kingAlmostCaptured";
    private final String WHITE_EATEN = "whiteEaten";
    private final String BLACK_LOSED = "blackLosed";

	private final Map<String,Double> weights;

    public BlackHeuristics(BlutForceGame game){
        super(game);
        this.weights=new HashMap<String,Double>();
        this.weights.put(this.KING_ALMOST_CAPTURED, 80.0); // only one more black pawn to capture the king
        this.weights.put(this.KING_UNDER_ATTACK, 60.0);    // king have almost one black pawn around
        this.weights.put(this.WHITE_EATEN, 20.0);          // for each eaten white pawn
        this.weights.put(this.BLACK_LOSED, -5.0);          // for each losed black pawn
        this.weights.put(this.KING_IN_CASTLE, 35.0);       // king positioned in castle
        this.weights.put(this.KING_NEAR_CASTLE, 25.0);     // king positioned in one of the four cells around the castle
        this.weights.put(this.ESCAPE_FREE, 20.0);          // no pawns between king and one of every escape cells       
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

    public Boolean isKingUnderAttack(State state){
        int[] kingCoords = this.getKing(state);
        int kingRow = kingCoords[0];
        int kingColumn = kingCoords[1];
        return numberOfColorPawnAroundCoords(state, kingRow, kingColumn, Pawn.BLACK) >= 1;
    }

    @Override
    public double evaluateState(State state) {      
        double num_black = (startingWhitePawns - state.getNumberOf(State.Pawn.WHITE)) * weights.get(WHITE_EATEN);
        double num_white = (startingBlackPawns - state.getNumberOf(State.Pawn.BLACK)) * weights.get(BLACK_LOSED);
        return num_black + num_white;
    }
    
}
