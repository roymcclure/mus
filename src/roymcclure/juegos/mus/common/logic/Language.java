package roymcclure.juegos.mus.common.logic;

public class Language {

	// client actions
	public class PlayerActions {
		
		public static final byte PASS = 0,
								ENVITE = 1, // ENVIDAR is basically the same operation as ENVIDAR_MAS, placing a bet on top of what is currently bet 
								ACCEPT = 2, // CALL the envite
								ORDAGO = 3,
								MUS = 4,
								CORTO_MUS = 5,
								DESCARTAR = 6, // must be accompanied by four booleans to indicate which cards to discard
								HANDSHAKE = 9,
								REQUEST_GAME_STATE = 10,
								REQUEST_SEAT = 11,
								LEAVE_SEAT = 12,
								HABLO_PARES = 13,
								HABLO_JUEGO = 14,
								NO_SE_JUEGA_RONDA = 15,
								CLOSE_CONNECTION = 20;
	
	}
	
	public class GamePhase {
		public static final byte 	MUS = 0, // players are deciding whether they want mus or not
									DESCARTE = 1, // all players decided they want mus, then select cards
									GRANDE = 2,
									CHICA = 3,
									PARES = 4,
									JUEGO = 5,									
									FIN_RONDA = 6; 
	}
	
	// game state
	public class ServerGameState {
		public static final byte	WAITING_ALL_PLAYERS_TO_CONNECT = 0, 
									WAITING_ALL_PLAYERS_TO_SEAT = 1,
									DEALING = 2, 
									PLAYING = 3,
									END_OF_ROUND = 4,
									GAME_FINISHED = 5;
	}

	// connection state
	public class ConnectionState {
		public static final byte READ = 0,
				WRITE = 1,
				WRITE_READ = 2;
	}
	
	public class NodeState {
		public static final byte 	SERVER = 0,
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
	
	public class ButtonIndices {
		public static final byte 	BUTTON_MUS = 0,
									BUTTON_CORTO_MUS = 1,
									BUTTON_ACEPTO_ORDAGO = 0,
									BUTTON_ME_CAGO = 1,
									BUTTON_ENVIDAR_2 = 0,									
									BUTTON_ENVIDAR_5 = 1,									
									BUTTON_ORDAGO = 2,
									BUTTON_PASO = 3,
									BUTTON_ACEPTAR = 4;									
	}
	
	public class MouseInputType {
		public static final byte MOUSE_CLICK = 0,
								 MOUSE_MOVE = 1,
								 MOUSE_ENTERED_CARD = 2,
								 MOUSE_EXITED_CARD = 3;
	}
		
	// legacy code from previous version
	public enum TipoPares {
		PAR,
		MEDIAS,
		DUPLES,
		NO_PAR
	}
		
	
}
