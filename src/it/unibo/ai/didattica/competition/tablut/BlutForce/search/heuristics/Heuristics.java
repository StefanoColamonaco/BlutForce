package it.unibo.ai.didattica.competition.tablut.BlutForce.search.heuristics;

import it.unibo.ai.didattica.competition.tablut.domain.State;

public abstract class Heuristics {

    // Evaluate the given state
    abstract public double evaluateState(State s);
}