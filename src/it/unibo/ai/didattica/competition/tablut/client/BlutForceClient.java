package it.unibo.ai.didattica.competition.tablut.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.*;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class BlutForceClient extends TablutClient {
    
    private int game;

    public BlutForceClient(String player, String name, int gameChosen, int timeout, String ipAddress) throws UnknownHostException, IOException {
		super(player, name, timeout, ipAddress);
		game = gameChosen;
	}

    public BlutForceClient(String player, String name, int timeout, String ipAddress) throws UnknownHostException, IOException {
		this(player, name, 4, timeout, ipAddress);
	}

    public BlutForceClient(String player, int timeout, String ipAddress) throws UnknownHostException, IOException {
		this(player, "BlutForce", 4, timeout, ipAddress);
	}

    public BlutForceClient(String player) throws UnknownHostException, IOException {
		this(player, "BlutForce", 4, 60, "localhost");
	}

    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
        int gametype = 4;
		String role = "";
		String name = "BlutForce";
		String ipAddress = "localhost";
		int timeout = 60;

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
		System.out.println("Selected client: " + args[0]);

		BlutForceClient client = new BlutForceClient(role, name, gametype, timeout, ipAddress);
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
		Game rules = null;
        
        if(this.game == 4){
            state = new StateTablut();
			state.setTurn(State.Turn.WHITE);
			rules = new GameAshtonTablut(99, 0, "garbage", "fake", "fake");
			System.out.println("Ashton Tablut game");
        }else{
            System.out.println("Error in game selection");
			System.exit(4);
        } 

        List<int[]> pawns = new ArrayList<int[]>();
		List<int[]> empty = new ArrayList<int[]>();

        System.out.println("You are player " + this.getPlayer().toString() + "!");
        
        while(true){
            
        }
    }
}
