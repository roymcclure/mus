package roymcclure.juegos.mus.server.logic;

import roymcclure.juegos.mus.common.logic.Language;
import roymcclure.juegos.mus.common.network.ClientMessage;


// Contains data representing the state of a game in a particular moment in time.

public class ServerGameState {

	
	private byte estado_partida, id_ronda, tipo_ronda, piedras_norte, piedras_este, piedras_sur, piedras_oeste, juegos_nortesur, juegos_esteoeste, vacas_nortesur, vacas_esteoeste, 
		id_jugador_norte, id_jugador_este, id_jugador_sur, id_jugador_oeste,
		player_in_turn, piedras_envidadas_ronda_actual;
	
	private byte[][] cartas;

	// player_ids[0] is NOT the player in the north but the player in thread 0
	private String[] player_ids; 
	
	// mesa[0] is the place where player with thread_id 0 is seated (north, etc)
	private byte[] mesa;
	
	
	public ServerGameState() {
		cartas = new byte[Language.GameDefinitions.MAX_CLIENTS][Language.GameDefinitions.CARDS_PER_HAND];
		player_ids = new String[Language.GameDefinitions.MAX_CLIENTS];
		// initially nobody is seated
		mesa = new byte[Language.GameDefinitions.MAX_CLIENTS];
		for (int i=0; i<Language.GameDefinitions.MAX_CLIENTS; i++) {
			mesa[i] = -1;
		}
		
	}
	
	
	public byte getEstado_partida() {
		return estado_partida;
	}

	public void setEstado_partida(byte estado_partida) {
		this.estado_partida = estado_partida;
	}

	public byte getId_ronda() {
		return id_ronda;
	}

	public void setId_ronda(byte id_ronda) {
		this.id_ronda = id_ronda;
	}

	public byte getTipo_ronda() {
		return tipo_ronda;
	}

	public void setTipo_ronda(byte tipo_ronda) {
		this.tipo_ronda = tipo_ronda;
	}

	public byte getPiedras_norte() {
		return piedras_norte;
	}

	public void setPiedras_norte(byte piedras_norte) {
		this.piedras_norte = piedras_norte;
	}

	public byte getPiedras_este() {
		return piedras_este;
	}

	public void setPiedras_este(byte piedras_este) {
		this.piedras_este = piedras_este;
	}

	public byte getPiedras_sur() {
		return piedras_sur;
	}

	public void setPiedras_sur(byte piedras_sur) {
		this.piedras_sur = piedras_sur;
	}

	public byte getPiedras_oeste() {
		return piedras_oeste;
	}

	public void setPiedras_oeste(byte piedras_oeste) {
		this.piedras_oeste = piedras_oeste;
	}

	public byte getJuegos_nortesur() {
		return juegos_nortesur;
	}

	public void setJuegos_nortesur(byte juegos_nortesur) {
		this.juegos_nortesur = juegos_nortesur;
	}

	public byte getJuegos_esteoeste() {
		return juegos_esteoeste;
	}

	public void setJuegos_esteoeste(byte juegos_esteoeste) {
		this.juegos_esteoeste = juegos_esteoeste;
	}

	public byte getVacas_nortesur() {
		return vacas_nortesur;
	}

	public void setVacas_nortesur(byte vacas_nortesur) {
		this.vacas_nortesur = vacas_nortesur;
	}

	public byte getVacas_esteoeste() {
		return vacas_esteoeste;
	}

	public void setVacas_esteoeste(byte vacas_esteoeste) {
		this.vacas_esteoeste = vacas_esteoeste;
	}

	public byte getCarta(byte id_player, byte pos_carta) {
		return cartas[id_player][pos_carta];
	}
	
	public void setCarta(byte id_player, byte pos_carta, byte id_carta) {
		cartas[id_player][pos_carta] = id_carta;
	}
	
	public byte getPiedras_envidadas_ronda_actual() {
		return piedras_envidadas_ronda_actual;
	}

	public void setPiedras_envidadas_ronda_actual(byte piedras_envidadas_ronda_actual) {
		this.piedras_envidadas_ronda_actual = piedras_envidadas_ronda_actual;
	}

	public byte getGameState() {
		return estado_partida;
	}

	public byte getPlayer_in_turn() {
		return player_in_turn;
	}

	public void setPlayer_in_turn(byte player_in_turn) {
		this.player_in_turn = player_in_turn;
	}

	public void setGameState(byte s) {
		this.estado_partida = s;
	}
	
	// the expected action from a client depends on the game state
	public byte getExpectedAction(String player_id) {
		switch(estado_partida) {
			case Language.ServerGameState.WAITING_ALL_PLAYERS_TO_SEAT:
				return getActionByPlayer(player_id);
			case Language.ServerGameState.PLAYING:
				return playing(player_id);
			case Language.ServerGameState.END_OF_ROUND:
				return endOfRound(player_id);
			case Language.ServerGameState.GAME_FINISHED:
				return gameFinished(player_id);
		}
		return -1;
	}
	
	public synchronized void updateGameStateWith(ClientMessage cm, byte thread_id) {
		// si cliente solicita información del mundo, realmente no hacemos gran cosa.
		if (cm.getAction() == Language.PlayerActions.REQUEST_GAME_STATE) {
			// player can pass their name here
			String playerName = cm.getInfo();
			player_ids[thread_id] = playerName;
			System.out.println("SERVER: player " + playerName.toString() + " connected.");
		}
		if (cm.getAction() == Language.PlayerActions.REQUEST_SEAT) {
			// we try to seat the player in the requested seat
			System.out.println("PLAYER " + player_ids[thread_id] + " requested the SEAT " + cm.getQuantity());
			byte requested_seat = cm.getQuantity();
			takeAseat(requested_seat, thread_id);
		}				
	}
	
	public synchronized void takeAseat(byte seat_id, byte thread_id) {
		// if requested seat is empty
		if (mesa[seat_id] == -1) {
			if (!isSeated(thread_id)) {
				mesa[thread_id] = seat_id;
				System.out.println("Thread id "+ thread_id + " took seat in " + seat_id);
			}
		}
	}
	
	public synchronized void clearSeat(byte seat_id) {
		mesa[seat_id] = -1;
	}	
	

	private boolean isSeated(byte thread_id) {
		return mesa[thread_id] != -1;
	}
	
	private boolean isSeated(String player_id) {
		// find thread_id for that player
		for (byte i = 0; i<Language.GameDefinitions.MAX_CLIENTS;i++) {
			if (player_ids[i].equals(player_id)) {
				if(mesa[i]==-1) {
					return false;
				} else return true;
			}
		}
		return false;
	}
	
	private byte getActionByPlayer(String player_id) {
		// si el jugador no está sentado,
		// 
		if (!isSeated(player_id)) {
			return Language.ConnectionState.READ_FROM_CLIENT;
		} else {
			// player is seated, so he must wait until everyone else is
			return Language.ConnectionState.WAIT_EXTERNAL;
		}
	}
	
	private byte playing(String player_id) {
		return 0;
	}
	
	private byte endOfRound(String player_id) {
		return 0;
	}
	
	private byte gameFinished(String player_id) {
		return 0;
	}

	public String getPlayerID_by_Seat(int seat_id) {
		for (byte thread_id = 0; thread_id<Language.GameDefinitions.MAX_CLIENTS; thread_id++) {
			// find thread corresponding to that seat_id
			if (mesa[thread_id] == seat_id) {
				return player_ids[thread_id];
			}
		}
		return "empty";
	}	
	
}
