package roymcclure.juegos.mus.common.logic;

public class Language {

	// client actions
	public class PlayerActions {
		
		public static final byte PASS = 0,
								ENVITE = 1,
								ACCEPT = 2,
								ORDAGO = 3,
								REQUEST_SEAT = 5;
	
	}
	
	// game state
	public class GameState {
		public static final byte WAITING_ALL_PLAYERS_TO_SEAT = 0,
				PLAYING = 1,
				END_OF_ROUND = 2,
				GAME_FINISHED = 3;
	}

	// connection state
	public class ConnectionState {
		public static final byte READ_FROM_CLIENT = 0,
				WRITE_TO_CLIENT = 1,
				WAIT_EXTERNAL = 2;
	}

	
	public class GameDefinitions {
		public static final byte CARDS_PER_SUIT = 12,
								TOTAL_CARDS = 48,
								MAX_CLIENTS = 4,
								STONES_TO_ROUND = 30, // cuantas piedras hacen falta para ganar una vaca?
								ROUNDS_TO_COW = 3, // cuantos juegos para ganar una vaca?
								COWS_TO_GAME = 1; // cuantas vacas hacen falta para ganar la partida?
	}
		
	
}
