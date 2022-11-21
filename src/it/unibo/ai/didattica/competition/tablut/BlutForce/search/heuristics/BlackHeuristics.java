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
	// private final String KING_IN_CASTLE = "kingInCastle";
    // private final String KING_NEAR_CASTLE = "kingNearCastle";
    private final String KING_UNDER_ATTACK = "kingUnderAttack";
    private final String KING_ALMOST_CAPTURED = "kingAlmostCaptured";
    private final String WHITE_EATEN = "whiteEaten";
    private final String BLACK_LOSED = "blackLosed";
    private final String ESCAPE_PATH_FREE = "escapePathFree";

	private final Map<String,Double> weights;

    public BlackHeuristics(BlutForceGame game){
        super(game);
        this.weights=new HashMap<String,Double>();
        this.weights.put(this.KING_ALMOST_CAPTURED, 80.0); // only one more black pawn to capture the king
        this.weights.put(this.KING_UNDER_ATTACK, 60.0);    // king have almost one black pawn around
        this.weights.put(this.WHITE_EATEN, 60.0);          // for each eaten white pawn
        this.weights.put(this.BLACK_LOSED, -5.0);          // for each losed black pawn
        //this.weights.put(this.KING_IN_CASTLE, 35.0);       // king positioned in castle
        //this.weights.put(this.KING_NEAR_CASTLE, 25.0);     // king positioned in one of the four cells around the castle
        this.weights.put(this.ESCAPE_FREE, -90.0);          // no pawns between king and one of every escape cells  
        this.weights.put(this.ESCAPE_PATH_FREE, -40.0);     
    }

    @Override
    public double evaluateState(State state) {    
        double king_almost_captured = conversion(isKingAlmostCaptured(state)) * weights.get(KING_ALMOST_CAPTURED);
        double king_under_attack = conversion(isKingUnderAttack(state)) * weights.get(KING_UNDER_ATTACK);
        double num_black = (startingWhitePawns - state.getNumberOf(State.Pawn.WHITE)) * weights.get(WHITE_EATEN);
        double num_white = (startingBlackPawns - state.getNumberOf(State.Pawn.BLACK)) * weights.get(BLACK_LOSED);
        double escape_free = conversion(this.isEscapeFree(state)) * weights.get(ESCAPE_FREE);
        double escape_path_free = conversion(this.isEscapePathFree(state)) * weights.get(ESCAPE_PATH_FREE);

        return king_almost_captured + king_under_attack + num_black + num_white + escape_free + escape_path_free;
    }
    
}
