package it.unibo.ai.didattica.competition.tablut.BlutForce.search.heuristics;

import java.util.HashMap;
import java.util.Map;

import it.unibo.ai.didattica.competition.tablut.domain.State;

public class WhiteHeuristics extends Heuristics{
    
    private final String ESCAPE_FREE = "escapeFree";
    private final String ESCAPE_PATH_FREE = "escapePathFree";
    private final String BEST_POSITIONS = "bestPositions";
	private final String KING_IN_CASTLE = "kingInCastle";
    private final String KING_NEAR_CASTLE = "kingNearCastle";
    private final String KING_PROTECTED = "kingProtected";
    private final String BLACK_EATEN = "blackEaten";
    private final String WHITE_LOSED = "whiteLosed";

	private final Map<String,Double> weights;

    public WhiteHeuristics(){
        super();
        this.weights=new HashMap<String,Double>();
        this.weights.put(this.KING_PROTECTED, 100.0);     // king have >= 3 white pawns in the four adjacent cells
		this.weights.put(this.BEST_POSITIONS, 60.0);      // treshold (between 4 and 7 included..?) <= number of white pawns positioned in: "e (3,4,6,7)", "(c,d,f,g) 5"
        this.weights.put(this.KING_IN_CASTLE, 35.0);      // king positioned in castle
        this.weights.put(this.KING_NEAR_CASTLE, 25.0);    // king positioned in one of the four cells around the castle
        this.weights.put(this.ESCAPE_FREE, 20.0);         // no pawns between king and one of: "a3","a7" "c1","c9","g1","g9","i3","i7"
        this.weights.put(this.ESCAPE_PATH_FREE, 10.0);    // no pawns between king and transition cells (one of): "c5","g5","e3","e7"
        this.weights.put(this.WHITE_LOSED, -7.0);         // for each losed white pawn
        this.weights.put(this.BLACK_EATEN, 5.0);          // for each eaten black pawn
    }
    /* Considerations:
     *  king near castle or in castle + king protected -> bonus                                 + 10
     *  black eaten >= treshold + one between escape free and escape path free -> bonus         + 10
     *  
     * RANGE:
     *  <= 0: bad
     *  350: max 
     * 
     */

    @Override
    public double evaluateState(State state) {
        
        double king_protected = conversion(isKingProtected(state)) * weights.get(KING_PROTECTED);
        double best_positions = conversion(isBestPosition(state)) * weights.get(BEST_POSITIONS);
        double king_in_castle = conversion(isKingInCastle(state)) * weights.get(KING_IN_CASTLE);
        double king_near_castle = conversion(isKingNearCastle(state)) * weights.get(KING_NEAR_CASTLE);
        double escape_free = 0.0 * weights.get(ESCAPE_FREE);
        double escape_path_free = 0.0 * weights.get(ESCAPE_PATH_FREE);
        double num_white = (startingWhitePawns - state.getNumberOf(State.Pawn.WHITE)) * weights.get(WHITE_LOSED);
        double num_black = (startingBlackPawns - state.getNumberOf(State.Pawn.BLACK)) * weights.get(BLACK_EATEN);;
        
        return king_protected + best_positions + king_in_castle + king_near_castle + escape_free + escape_path_free + num_white + num_black;
    }
}
