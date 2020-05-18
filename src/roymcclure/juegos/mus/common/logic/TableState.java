package roymcclure.juegos.mus.common.logic;

import java.io.Serializable;

import roymcclure.juegos.mus.common.logic.cards.Baraja;
import roymcclure.juegos.mus.common.logic.cards.Carta;

import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;

/*
 * 
 * State of game data unrelated to connection.
 * 
 */

public class TableState implements Serializable {

	/**
	 * DATA THAT CHANGES ALONG THE GAME
	 */
	private static final long serialVersionUID = -1307767440829066871L;

	private PlayerState[] clients;
	private Baraja baraja;
	private Baraja barajaDescartes;
	
	// domain specific data
	private byte 	piedras_norte_sur = 0, piedras_oeste_este = 0, 
					juegos_norte_sur = 0, juegos_oeste_este = 0,
					vacas_norte_sur = 0, vacas_oeste_este = 0,
					jugador_debe_hablar = -1, // index of the SEAT_ID not THREAD_ID
					piedras_envidadas_ronda_actual = 0,
					piedras_envidadas_a_grande = 0,
					piedras_envidadas_a_chica = 0,
					piedras_envidadas_a_pares = 0,
					piedras_envidadas_a_juego = 0,
					jugadores_hablado_en_turno_actual = 0,
					id_ronda = 0, //number that increases with each new round
					tipo_lance = 0; //identifier of the round type
	
	public byte getPiedras_enviadas_a_grande() {
		return piedras_envidadas_a_grande;
	}


	public byte getJugadores_hablado_en_turno_actual() {
		return jugadores_hablado_en_turno_actual;
	}


	public void setJugadores_hablado_en_turno_actual(byte jugadores_hablado_en_turno_actual) {
		this.jugadores_hablado_en_turno_actual = jugadores_hablado_en_turno_actual;
	}


	public void setPiedras_enviadas_a_grande(byte piedras_envidadas_a_grande) {
		this.piedras_envidadas_a_grande = piedras_envidadas_a_grande;
	}


	public byte getPiedras_envidadas_a_chica() {
		return piedras_envidadas_a_chica;
	}


	public void setPiedras_envidadas_a_chica(byte piedras_envidadas_a_chica) {
		this.piedras_envidadas_a_chica = piedras_envidadas_a_chica;
	}


	public byte getPiedras_envidadas_a_pares() {
		return piedras_envidadas_a_pares;
	}


	public void setPiedras_envidadas_a_pares(byte piedras_envidadas_a_pares) {
		this.piedras_envidadas_a_pares = piedras_envidadas_a_pares;
	}


	public byte getPiedras_envidadas_a_juego() {
		return piedras_envidadas_a_juego;
	}


	public void setPiedras_envidadas_a_juego(byte piedras_envidadas_a_juego) {
		this.piedras_envidadas_a_juego = piedras_envidadas_a_juego;
	}


	public TableState() {
		clients = new PlayerState[MAX_CLIENTS];
		for (int i=0; i < MAX_CLIENTS; i++) {
			clients[i] = new PlayerState();
		}
		baraja = new Baraja();		
		baraja.rellenar();
		System.out.println("Creada en TAbleState");
		baraja.print();
		barajaDescartes = new Baraja();
		
	}

	
	public Baraja getBaraja() {
		return baraja;
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

	public byte getTipo_Lance() {
		return tipo_lance;
	}
	
	public boolean isSeatOccupied(int i) {
		return !clients[i].getID().equals("");
		
	}
	
	public TableState(PlayerState[] clients, byte piedras_norte_sur, byte piedras_oeste_este, byte juegos_norte_sur,
			byte juegos_oeste_este, byte vacas_norte_sur, byte vacas_oeste_este, byte jugador_debe_hablar,
			byte piedras_envidadas_ronda_actual, byte id_ronda, byte tipo_lance, Baraja baraja,
			byte piedras_envidadas_a_grande, byte piedras_envidadas_a_chica, byte piedras_envidadas_a_pares, byte piedras_envidadas_a_juego,
			byte jugadores_hablado_en_turno_actual) {
		super();
		this.clients = new PlayerState[MAX_CLIENTS];		
		for (int i = 0; i < MAX_CLIENTS; i++) {

			this.clients[i] = new PlayerState(clients[i]);
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
		this.tipo_lance = tipo_lance;
		this.piedras_envidadas_a_grande = piedras_envidadas_a_grande;
		this.piedras_envidadas_a_chica = piedras_envidadas_a_chica;
		this.piedras_envidadas_a_pares = piedras_envidadas_a_pares;
		this.piedras_envidadas_a_juego = piedras_envidadas_a_juego;	
		this.jugadores_hablado_en_turno_actual =jugadores_hablado_en_turno_actual; 
		this.baraja = baraja.clone();
	}

	public boolean isSeatEmpty(byte seat_id) {
		return clients[seat_id].getID().equals("");
	}
	
	public void clearSeat(int seat_id) {
		assert(seat_id >=0 && seat_id < MAX_CLIENTS);
		clients[seat_id].setID("");
	}
	
	public byte getSeatOf(String playerID) {
		
		for (byte i = 0; i < MAX_CLIENTS; i++) {
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
			System.out.print("seat_id[" + i + "]:" + clients[i].getID() + " | ");
		}
		System.out.println("piedras norte/sur:" + piedras_norte_sur);
		System.out.println("piedras oeste/este:" + piedras_oeste_este);

		for (int i = 0; i < MAX_CLIENTS; i++) {
			System.out.println("CArtas de " + i + ":");
			clients[i].printCartas();
			System.out.println("--------------------");
		}
		System.out.println("En baraja hay:");
		baraja.print();
		
	}
	
	public void setJugador_debe_hablar(byte jugador_debe_hablar) {
		this.jugador_debe_hablar = jugador_debe_hablar;
	}


	// clone the TableState for a particular player
	public TableState clone(String playerID) {
		TableState tableCopy =new TableState(clients, piedras_norte_sur, piedras_oeste_este, 
				juegos_norte_sur, juegos_oeste_este,
				vacas_norte_sur, vacas_oeste_este,jugador_debe_hablar, piedras_envidadas_ronda_actual,
				id_ronda,tipo_lance, baraja, piedras_envidadas_a_grande, piedras_envidadas_a_chica, 
				piedras_envidadas_a_pares, piedras_envidadas_a_juego, jugadores_hablado_en_turno_actual);
		// remove all data not pertinent to player in [thread_id]

		tableCopy.baraja.conceal();

		for (int i = 0; i < MAX_CLIENTS; i++) {
			if (clients[i] != null && !clients[i].getID().equals(playerID)) {
				Carta[] cartas = new Carta[CARDS_PER_HAND];
				for (int j = 0; j< CARDS_PER_HAND;j++) {
					cartas[j] = new Carta(ID_CARTA_DORSO);
				}
				tableCopy.getClient(i).setCartas(cartas);
			}			
		}

		return  tableCopy;
	}

	// en la mano de cada jugador necesitaremos un flag 
	public void repartir() {
		// cogemos de la baraja mientras queden cartas
		try {
			for (int i = 0; i<MAX_CLIENTS;i++) {
				Carta[] cartas = new Carta[CARDS_PER_HAND];
				for (int j = 0; j<CARDS_PER_HAND;j++) {
					if (clients[i].descartar(j))
						cartas[j] = this.baraja.sacarCarta(0);
					else cartas[j] = clients[i].getCarta(j);
				}
				clients[i].setCartas(cartas);
			}

		} catch (Exception e) {
			System.out.println("No deberia estar aqui; en el reparto inicial deberia ser posible repartir CARDS_PER_HAND * MAX_CLIENTS cartas");
			e.printStackTrace();
		}

		// si repartimos 16, wonderful.
		// si no, tenemos que coger del montón de descartes
		
	}


	public boolean allSeated() {
		for (byte i = 0; i < MAX_CLIENTS; i++) {
			if (isSeatEmpty(i))
				return false;
		}
		return true;
	}

	public void advanceTurn() {
		this.jugador_debe_hablar-=1;
		if (this.jugador_debe_hablar==-1)
			this.jugador_debe_hablar=3;
		
	}

}
