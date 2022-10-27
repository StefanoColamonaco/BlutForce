package it.unibo.ai.didattica.competition.tablut.BlutForce.search;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.IOException;
import java.lang.Object;


import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;

import it.unibo.ai.didattica.competition.tablut.exceptions.*;


public class BlutForceGame implements aima.core.search.adversarial.Game<StateTablut, Action, State.Turn>{
    private int repeated_moves_allowed;
    private int cache_size;
    private String logs_folder;
    private String whiteName;
    private String blackName;
    private GameAshtonTablut rules;
    private List<String> citadels;
    
    public BlutForceGame (int repeated_moves_allowed, int cache_size, String logs_folder, String whiteName, String blackName) {
        this.repeated_moves_allowed = repeated_moves_allowed;
        this.cache_size = cache_size;
        this.logs_folder = logs_folder;
        this.whiteName = whiteName;
        this.blackName = blackName;
        this.rules = new GameAshtonTablut(repeated_moves_allowed, cache_size, logs_folder, whiteName, blackName);

        this.citadels = new ArrayList<String>();
		this.citadels.add("a4");
		this.citadels.add("a5");
		this.citadels.add("a6");
		this.citadels.add("b5");
		this.citadels.add("d1");
		this.citadels.add("e1");
		this.citadels.add("f1");
		this.citadels.add("e2");
		this.citadels.add("i4");
		this.citadels.add("i5");
		this.citadels.add("i6");
		this.citadels.add("h5");
		this.citadels.add("d9");
		this.citadels.add("e9");
		this.citadels.add("f9");
		this.citadels.add("e8");
    }


    public List<int[]> getPlayerPawns(Pawn[][] board, StateTablut state){
        List<int[]> playerPawns = new ArrayList<int[]>();
        String searching = state.getTurn().equals(Turn.BLACK) ? Pawn.BLACK.toString() : Pawn.WHITE.toString();

        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board.length; column++) {
                Pawn pawn = state.getPawn(row, column);
                if (pawn.equalsPawn(searching) || (pawn.equals(Pawn.KING) && searching.equals("W"))) {
                   int[] coordinates = new int[2];
                   coordinates[0] = row;
                   coordinates[1] = column;
                   playerPawns.add(coordinates); 
                }
            }
        }
        return playerPawns;
    }

    private void checkPossibleMove(State state, Action a)
            throws BoardException, ActionException, StopException, PawnException, DiagonalException, ClimbingException,
            ThroneException, OccupitedException, ClimbingCitadelException, CitadelException {
        // this.loggGame.fine(a.toString());
        // controllo la mossa
        if (a.getTo().length() != 2 || a.getFrom().length() != 2) {
            // this.loggGame.warning("Formato mossa errato");
            throw new ActionException(a);
        }
        int columnFrom = a.getColumnFrom();
        int columnTo = a.getColumnTo();
        int rowFrom = a.getRowFrom();
        int rowTo = a.getRowTo();

        // controllo se sono fuori dal tabellone
        if (columnFrom > state.getBoard().length - 1 || rowFrom > state.getBoard().length - 1
                || rowTo > state.getBoard().length - 1 || columnTo > state.getBoard().length - 1 || columnFrom < 0
                || rowFrom < 0 || rowTo < 0 || columnTo < 0) {
            // this.loggGame.warning("Mossa fuori tabellone");
            throw new BoardException(a);
        }

        // controllo che non vada sul trono
        if (state.getPawn(rowTo, columnTo).equalsPawn(State.Pawn.THRONE.toString())) {
            // this.loggGame.warning("Mossa sul trono");
            throw new ThroneException(a);
        }

        // controllo la casella di arrivo
        if (!state.getPawn(rowTo, columnTo).equalsPawn(State.Pawn.EMPTY.toString())) {
            // this.loggGame.warning("Mossa sopra una casella occupata");
            throw new OccupitedException(a);
        }
        if (this.citadels.contains(state.getBox(rowTo, columnTo))
                && !this.citadels.contains(state.getBox(rowFrom, columnFrom))) {
            // this.loggGame.warning("Mossa che arriva sopra una citadel");
            throw new CitadelException(a);
        }
        if (this.citadels.contains(state.getBox(rowTo, columnTo))
                && this.citadels.contains(state.getBox(rowFrom, columnFrom))) {
            if (rowFrom == rowTo) {
                if (columnFrom - columnTo > 5 || columnFrom - columnTo < -5) {
                    // this.loggGame.warning("Mossa che arriva sopra una citadel");
                    throw new CitadelException(a);
                }
            } else {
                if (rowFrom - rowTo > 5 || rowFrom - rowTo < -5) {
                    // this.loggGame.warning("Mossa che arriva sopra una citadel");
                    throw new CitadelException(a);
                }
            }

        }

        // controllo se cerco di stare fermo
        if (rowFrom == rowTo && columnFrom == columnTo) {
            // this.loggGame.warning("Nessuna mossa");
            throw new StopException(a);
        }

        // controllo se sto muovendo una pedina giusta
        if (state.getTurn().equalsTurn(State.Turn.WHITE.toString())) {
            if (!state.getPawn(rowFrom, columnFrom).equalsPawn("W")
                    && !state.getPawn(rowFrom, columnFrom).equalsPawn("K")) {
                // this.loggGame.warning("Giocatore " + a.getTurn() + " cerca di muovere una
                // pedina avversaria");
                throw new PawnException(a);
            }
        }
        if (state.getTurn().equalsTurn(State.Turn.BLACK.toString())) {
            if (!state.getPawn(rowFrom, columnFrom).equalsPawn("B")) {
                // this.loggGame.warning("Giocatore " + a.getTurn() + " cerca di muovere una
                // pedina avversaria");
                throw new PawnException(a);
            }
        }

        // controllo di non muovere in diagonale
        if (rowFrom != rowTo && columnFrom != columnTo) {
            // this.loggGame.warning("Mossa in diagonale");
            throw new DiagonalException(a);
        }

        // controllo di non scavalcare pedine
        if (rowFrom == rowTo) {
            if (columnFrom > columnTo) {
                for (int i = columnTo; i < columnFrom; i++) {
                    if (!state.getPawn(rowFrom, i).equalsPawn(State.Pawn.EMPTY.toString())) {
                        if (state.getPawn(rowFrom, i).equalsPawn(State.Pawn.THRONE.toString())) {
                            // this.loggGame.warning("Mossa che scavalca il trono");
                            throw new ClimbingException(a);
                        } else {
                            // this.loggGame.warning("Mossa che scavalca una pedina");
                            throw new ClimbingException(a);
                        }
                    }
                    if (this.citadels.contains(state.getBox(rowFrom, i))
                            && !this.citadels.contains(state.getBox(a.getRowFrom(), a.getColumnFrom()))) {
                        // this.loggGame.warning("Mossa che scavalca una citadel");
                        throw new ClimbingCitadelException(a);
                    }
                }
            } else {
                for (int i = columnFrom + 1; i <= columnTo; i++) {
                    if (!state.getPawn(rowFrom, i).equalsPawn(State.Pawn.EMPTY.toString())) {
                        if (state.getPawn(rowFrom, i).equalsPawn(State.Pawn.THRONE.toString())) {
                            // this.loggGame.warning("Mossa che scavalca il trono");
                            throw new ClimbingException(a);
                        } else {
                            // this.loggGame.warning("Mossa che scavalca una pedina");
                            throw new ClimbingException(a);
                        }
                    }
                    if (this.citadels.contains(state.getBox(rowFrom, i))
                            && !this.citadels.contains(state.getBox(a.getRowFrom(), a.getColumnFrom()))) {
                        // this.loggGame.warning("Mossa che scavalca una citadel");
                        throw new ClimbingCitadelException(a);
                    }
                }
            }
        } else {
            if (rowFrom > rowTo) {
                for (int i = rowTo; i < rowFrom; i++) {
                    if (!state.getPawn(i, columnFrom).equalsPawn(State.Pawn.EMPTY.toString())) {
                        if (state.getPawn(i, columnFrom).equalsPawn(State.Pawn.THRONE.toString())) {
                            // this.loggGame.warning("Mossa che scavalca il trono");
                            throw new ClimbingException(a);
                        } else {
                            // this.loggGame.warning("Mossa che scavalca una pedina");
                            throw new ClimbingException(a);
                        }
                    }
                    if (this.citadels.contains(state.getBox(i, columnFrom))
                            && !this.citadels.contains(state.getBox(a.getRowFrom(), a.getColumnFrom()))) {
                        // this.loggGame.warning("Mossa che scavalca una citadel");
                        throw new ClimbingCitadelException(a);
                    }
                }
            } else {
                for (int i = rowFrom + 1; i <= rowTo; i++) {
                    if (!state.getPawn(i, columnFrom).equalsPawn(State.Pawn.EMPTY.toString())) {
                        if (state.getPawn(i, columnFrom).equalsPawn(State.Pawn.THRONE.toString())) {
                            // this.loggGame.warning("Mossa che scavalca il trono");
                            throw new ClimbingException(a);
                        } else {
                            // this.loggGame.warning("Mossa che scavalca una pedina");
                            throw new ClimbingException(a);
                        }
                    }
                    if (this.citadels.contains(state.getBox(i, columnFrom))
                            && !this.citadels.contains(state.getBox(a.getRowFrom(), a.getColumnFrom()))) {
                        // this.loggGame.warning("Mossa che scavalca una citadel");
                        throw new ClimbingCitadelException(a);
                    }
                }
            }
        }
    }

    
    

    @Override
    public List<Action> getActions(StateTablut state) {
        Pawn [][] board = state.getBoard();
        
        List<Action> actions = new ArrayList<Action>();

        // Search for all the players pawn
        List<int[]> playerPawns = getPlayerPawns(board, state);

        // Search all the possible moves
        for (int[] coords : playerPawns) {
            int x = coords[0];
            int y = coords[1];
            String from = state.getBox(x, y);
            for (int index = 0; index < board.length; index++) {
                int x_to = index;
                int y_to = index;
                String to_movex = state.getBox(x_to, y);
                String to_movey = state.getBox(x, y_to); 
                Action a_movex, a_movey;
                try {
                    a_movex = new Action(from, to_movex, state.getTurn());
                    this.checkPossibleMove(state, a_movex);
                    actions.add(a_movex);
                } catch (Exception e1) {
                }
                try {
                    a_movey = new Action(from, to_movey, state.getTurn());
                    this.checkPossibleMove(state, a_movey);
                    actions.add(a_movey);

                } catch (Exception e1) {
                }
            }
        }
		return actions;
    }


	@Override
	public StateTablut getInitialState() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Turn getPlayer(StateTablut state) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Turn[] getPlayers() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public StateTablut getResult(StateTablut state, Action action) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public double getUtility(StateTablut state, Turn action) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public boolean isTerminal(StateTablut state) {
		// TODO Auto-generated method stub
		return false;
	}

}