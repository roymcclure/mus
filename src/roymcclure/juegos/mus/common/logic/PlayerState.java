package roymcclure.juegos.mus.common.logic;

import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;

import java.io.Serializable;

import roymcclure.juegos.mus.common.logic.cards.Baraja;
import roymcclure.juegos.mus.common.logic.cards.Carta;

public class PlayerState implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5763365704321501068L;
	// nombre. empty string is not seated.
	private String playerID="";
	// cartas
	private Carta[] cartas;
	// 
	private boolean[] reemplazar;
	// equipo al que pertenece es implícito. site_id in [0,2] es equipo norte/sur, else este/oeste
	// las piedras, juego y etc no va aqui. pertenece a gameState
	// el conteo tb es implícito. se contean siempre jugadores en 0, 1
	
	public PlayerState() {
		playerID = "";
		cartas = new Carta[MAX_CLIENTS];
		for (int i = 0; i<CARDS_PER_HAND; i++) {
			cartas[i] = new Carta(ID_CARTA_DORSO);
		}
		reemplazar = new boolean[MAX_CLIENTS];
		for (int i = 0; i<MAX_CLIENTS;i++) {
			reemplazar[i] = true;
		}
	}
	
	public boolean descartar(int i) {
		return reemplazar[i];
	}

	public PlayerState(PlayerState state) {
		Carta[] cartas2 = state.getCartas();
		playerID = state.getID();
		cartas = new Carta[MAX_CLIENTS];
		for (int i = 0; i<CARDS_PER_HAND; i++) {
			cartas[i] = new Carta(cartas2[i].getId());
		}
		reemplazar=new boolean[MAX_CLIENTS];
		for (int i = 0; i<CARDS_PER_HAND; i++) {
			reemplazar[i] = this.reemplazar[i];
		}		
	}

	public String getID() {
		return playerID;
	}
	
	public void setID(String id) {
		playerID = id;
	}
	
	public String getName() {
		if (playerID.contains(":")) {
			return playerID.substring(0, playerID.indexOf(':'));			
		}
		return playerID;
	}

	public Carta[] getCartas() {
		return cartas;
	}

	public void setCartas(Carta[] cartas) {
		this.cartas = cartas;
	}

	public void printCartas() {
		for (int i = 0; i< CARDS_PER_HAND; i++) {
			Baraja.print(cartas[i].getId());
			if (i != CARDS_PER_HAND - 1)
				System.out.print(",");
		}
	}

	public Carta getCarta(int i) {
		return cartas[i];
	}

}
