package roymcclure.juegos.mus.common.logic;

import java.io.Serializable;
import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;

/*
 * 
 * State of game data unrelated to connection.
 * 
 */

public class TableState implements Serializable {

	private static final long serialVersionUID = 1L;

	
	private PlayerState[] clients;

	// domain specific data
	private byte 	piedras_norte_sur = 0, piedras_oeste_este = 0, 
					juegos_norte_sur = 0, juegos_oeste_este = 0,
					vacas_norte_sur = 0, vacas_oeste_este = 0,
					jugador_debe_hablar = -1,
					piedras_envidadas_ronda_actual = 0,
					id_ronda = 0,
					tipo_ronda = 0;
	
	public TableState() {
		clients = new PlayerState[MAX_CLIENTS];
		for (int i=0; i < MAX_CLIENTS; i++) {
			clients[i] = new PlayerState();
		}
		
		
	}

	public PlayerState getClient(int i) {
		return clients[i];
	}

	public byte getPiedras_norte_sur() {
		return piedras_norte_sur;
	}

	public byte getPiedras_oeste_este() {
		return piedras_oeste_este;
	}

	public byte getJuegos_norte_sur() {
		return juegos_norte_sur;
	}

	public byte getJuegos_oeste_este() {
		return juegos_oeste_este;
	}

	public byte getVacas_norte_sur() {
		return vacas_norte_sur;
	}

	public byte getVacas_oeste_este() {
		return vacas_oeste_este;
	}

	public byte getJugador_debe_hablar() {
		return jugador_debe_hablar;
	}

	public byte getPiedras_envidadas_ronda_actual() {
		return piedras_envidadas_ronda_actual;
	}

	public byte getId_ronda() {
		return id_ronda;
	}

	public byte getTipo_ronda() {
		return tipo_ronda;
	}
	
	public boolean isSeatOccupied(int i) {
		return !clients[i].getID().equals("");
		
	}
	
	public TableState(PlayerState[] clients, byte piedras_norte_sur, byte piedras_oeste_este, byte juegos_norte_sur,
			byte juegos_oeste_este, byte vacas_norte_sur, byte vacas_oeste_este, byte jugador_debe_hablar,
			byte piedras_envidadas_ronda_actual, byte id_ronda, byte tipo_ronda) {
		super();
		this.clients = new PlayerState[MAX_CLIENTS];		
		for (int i = 0; i < MAX_CLIENTS; i++) {

			this.clients[i] = new PlayerState();
			this.clients[i].setID(clients[i].getID());
		}
		this.piedras_norte_sur = piedras_norte_sur;
		this.piedras_oeste_este = piedras_oeste_este;
		this.juegos_norte_sur = juegos_norte_sur;
		this.juegos_oeste_este = juegos_oeste_este;
		this.vacas_norte_sur = vacas_norte_sur;
		this.vacas_oeste_este = vacas_oeste_este;
		this.jugador_debe_hablar = jugador_debe_hablar;
		this.piedras_envidadas_ronda_actual = piedras_envidadas_ronda_actual;
		this.id_ronda = id_ronda;
		this.tipo_ronda = tipo_ronda;
	}

	public boolean isSeatEmpty(byte seat_id) {
		return clients[seat_id].getID().equals("");
	}
	
	public void clearSeat(int seat_id) {
		if (seat_id >=0 && seat_id < MAX_CLIENTS)
			clients[seat_id].setID("");
	}
	
	public int getSeatOf(String playerID) {
		
		for (int i = 0; i < MAX_CLIENTS; i++) {
			if (clients[i].getID().equals(playerID)) {
				return i;
			}
		}
		return -1;
	}
	
	public boolean takeAseat(byte seat_id, String playerID) {
		// if requested seat is empty
		if (!isSeatOccupied(seat_id)) {
			if (!isSeated(playerID)) {
				getClient(seat_id).setID(playerID);
				System.out.println("Player id "+ playerID + " took seat in " + seat_id);
				return true;
			}
		}
		return false;
	}	

	private boolean isSeated(String playerID) {
		// buscamos player_ID de ese thread en tableState y recuperamos el indice de su sitio
		
		int seat_id = getSeatOf(playerID);
		return seat_id != -1;
	}

	public void printContent() {
		for (int i = 0; i < MAX_CLIENTS; i++) {
			System.out.println("In seat_id " + i + " i have " + clients[i].getID());
		}
		
	}
	
	@Override
	public TableState clone() {
		return new TableState(clients, piedras_norte_sur, piedras_oeste_este, 
				juegos_norte_sur, juegos_oeste_este,
				vacas_norte_sur, vacas_oeste_este,jugador_debe_hablar, piedras_envidadas_ronda_actual,
				id_ronda,tipo_ronda);
	}

}
