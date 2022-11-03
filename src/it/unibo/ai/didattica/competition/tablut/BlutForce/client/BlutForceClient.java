package it.unibo.ai.didattica.competition.tablut.BlutForce.client;

import java.io.IOException;
import java.net.UnknownHostException;

import it.unibo.ai.didattica.competition.tablut.client.TablutClient;
import it.unibo.ai.didattica.competition.tablut.domain.*;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.BlutForce.search.Player;
import it.unibo.ai.didattica.competition.tablut.BlutForce.search.BlutForceGame;


public class BlutForceClient extends TablutClient {
    
    private int game;
	private boolean debug;
	private Player player;

    public BlutForceClient(String player, String name, int gameChosen, int timeout, String ipAddress, boolean deb) throws UnknownHostException, IOException {
		super(player, name, timeout, ipAddress);
		this.game = gameChosen;
		this.debug = deb;
	}

    public BlutForceClient(String player, String name, int timeout, String ipAddress) throws UnknownHostException, IOException {
		this(player, name, 4, timeout, ipAddress, false);
	}

    public BlutForceClient(String player, int timeout, String ipAddress) throws UnknownHostException, IOException {
		this(player, "BlutForce", 4, timeout, ipAddress, false);
	}

    public BlutForceClient(String player) throws UnknownHostException, IOException {
		this(player, "BlutForce", 4, 60, "localhost", false);
	}

    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
        int gametype = 4;
		String role = "";
		String name = "BlutForce";
		String ipAddress = "localhost";
		int timeout = 60;
		boolean debug = false;

		if (args.length < 1) {
			System.out.println("You must specify which player you are (WHITE or BLACK)");
			System.exit(-1);
		} else {
			System.out.println(args[0]);
			role = (args[0]);
		}
		if (args.length == 2) {
			System.out.println(args[1]);
			timeout = Integer.parseInt(args[1]);
		}
		if (args.length == 3) {
			ipAddress = args[2];
		}
		if (args.length == 4) {
			debug = Boolean.parseBoolean(args[3]);
		}
		System.out.println("Selected client: " + args[0]);

		BlutForceClient client = new BlutForceClient(role, name, gametype, timeout, ipAddress, debug);
		client.run();
    }

    @Override
	public void run() {
        
        try {
			this.declareName();
		} catch (Exception e) {
			e.printStackTrace();
		}

        State state;
		BlutForceGame rules = null;
        
        if(this.game == 4){
            state = new StateTablut();
			state.setTurn(State.Turn.WHITE);
			rules = new BlutForceGame(99, 0, "garbage", "fake", "fake");
			System.out.println("Ashton Tablut game");
        }else{
            System.out.println("Error in game selection");
			System.exit(4);
        } 

		player = new Player(rules, Double.MIN_VALUE,Double.MAX_VALUE,super.getTimeout()-2,debug);
        System.out.println("You are player " + this.getPlayer().toString() + "!");
        
        while(true) {
	        try {
	            this.read();
	        } catch (ClassNotFoundException | IOException e1) {
	            e1.printStackTrace();
	            System.exit(1);
	        }
	
	        // print current state
	        state = (State) this.getCurrentState();
	        System.out.println("Current state:");
	        System.out.println(state.toString());
	
	        // if WHITE
	        if (this.getCurrentState().getTurn().equals(StateTablut.Turn.WHITE)) {
		        if (this.getPlayer().equals(State.Turn.WHITE)) {
		            // my turn
	                Action a = player.makeDecision(state, rules);
		            System.out.println(a);
	                try {
	                    this.write(a);
	                } catch (ClassNotFoundException | IOException e) {
	                    e.printStackTrace();
	                }
	            }
	
	            // Oppenent (BLACK)
	            else {
	                System.out.println("Waiting for your opponent move...\n");
	            }
	        }
	        // if BLACK
	        else if (this.getCurrentState().getTurn().equals(StateTablut.Turn.BLACK)) {
	        	// my turn
	        	if (this.getPlayer().equals(State.Turn.BLACK)) {
	                Action a = player.makeDecision(state, rules);
	                try {
	                    this.write(a);
	                } catch (ClassNotFoundException | IOException e) {
	                    e.printStackTrace();
	                }
	            }

				// Oppenent (WHITE)
	            else {
	                System.out.println("Waiting for your opponent move...\n");
	            }
	        } 
	
	        // if WHITE WIN 
	        else if (state.getTurn().equals(StateTablut.Turn.WHITEWIN)) {
	            System.out.println(this.getPlayer().equals(State.Turn.WHITE)?"YOU WON!":"YOU LOSE!");
	            System.exit(0);
	        }
	
	        // if BLACK WIN
	        else if (state.getTurn().equals(StateTablut.Turn.BLACKWIN)) {
	            System.out.println(this.getPlayer().equals(State.Turn.BLACK)?"YOU WON!":"YOU LOSE!");
	            System.exit(0);
	        }
	
	        // if DRAW
	        else if (state.getTurn().equals(StateTablut.Turn.DRAW)) {
	            System.out.println("DRAW!");
	            System.exit(0);
	        }
		}
	}
}