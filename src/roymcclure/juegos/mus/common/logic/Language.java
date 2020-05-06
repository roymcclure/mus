package roymcclure.juegos.mus.common.logic;

public class Language {

	// client actions
	public class PlayerActions {
		
		public static final byte PASS = 0,
								ENVITE = 1,
								ACCEPT = 2,
								ORDAGO = 3,
								HANDSHAKE = 9,
								REQUEST_GAME_STATE = 10,
								REQUEST_SEAT = 11,
								CLOSE_CONNECTION = 20;
	
	}
	
	// game state
	public class ServerGameState {
		public static final byte WAITING_ALL_PLAYERS_TO_SEAT = 0,
				PLAYING = 1,
				END_OF_ROUND = 2,
				GAME_FINISHED = 3;
	}

	public class ClientGameState {
		public static final byte 	AWAITING_GAME_STATE = 0,
									PICKING_SEAT = 1,
									AWAITING_ALL_SEATED = 2,
									PLAYING = 3,
									DISCONNECTED = -1;
	}
	
	// connection state
	public class ConnectionState {
		public static final byte READ = 0,
				WRITE = 1,
				WRITE_READ = 2;
	}
	
	public class NodeState {
		public static final byte 	SERVER=0,
									CLIENT = 1;
	}
	
	
	public class ServerReply {
			public static final byte OK = 0,
									ERROR = 1,
									ACTION_DENIED = 2;
	}

	// TODO: these should be server-defined
	// dont really want to hard-code them
	public class GameDefinitions {
		public static final String NO_PLAYER="";
		public static final byte CARDS_PER_SUIT = 12,
								 CARDS_PER_HAND = 4,
								TOTAL_CARDS = 48,
								MAX_CLIENTS = 4,
								STONES_TO_ROUND = 30, // cuantas piedras hacen falta para ganar una vaca?
								ROUNDS_TO_COW = 3,
								ID_CARTA_DORSO = 49,// cuantos juegos para ganar una vaca?
								COWS_TO_GAME = 1,
								UNSEATED = -1; // cuantas vacas hacen falta para ganar la partida?
		
		public static final byte ID_NORTH_SEAT = 0,
								 ID_EAST_SEAT = 1,
								 ID_SOUTH_SEAT = 2,
								 ID_WEST_SEAT = 3;
	}
	
	
		
	
}
