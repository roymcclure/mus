package roymcclure.juegos.mus.common.logic;

import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import roymcclure.juegos.mus.common.logic.Language.TipoPares;
import roymcclure.juegos.mus.common.logic.cards.Baraja;
import roymcclure.juegos.mus.common.logic.cards.Carta;
import roymcclure.juegos.mus.common.logic.cards.Jugadas;

public class PlayerState implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5763365704321501068L;
	// nombre. empty string is not seated.
	private String playerID="";
	// cartas
	private Carta[] cartas;
	// indicates what cards should be discarded
	private boolean[] reemplazar;
	private boolean commitedToDiscard = false;
	// equipo al que pertenece es implícito. site_id in [0,2] es equipo norte/sur, else este/oeste
	// las piedras, juego y etc no va aqui. pertenece a gameState
	// el conteo tb es implícito. se contean siempre jugadores en 0, 1

	public boolean isCommitedToDiscard() {
		return commitedToDiscard;
	}

	public void setCommitedToDiscard(boolean commitedToDiscard) {
		this.commitedToDiscard = commitedToDiscard;
	}

	public PlayerState() {
		playerID = "";
		cartas = new Carta[CARDS_PER_HAND];
		for (int i = 0; i<CARDS_PER_HAND; i++) {
			cartas[i] = new Carta(ID_CARTA_DORSO);
		}
		reemplazar = new boolean[CARDS_PER_HAND];
		for (int i = 0; i<CARDS_PER_HAND;i++) {
			// initially we want to ask for all cards
			reemplazar[i] = true;
		}
	}
	// tried to call it isMarkedForDiscard()
	// but it's actually markedForReplacement
	public boolean isMarkedForReplacement(int i) {
		return reemplazar[i];
	}

	public PlayerState(PlayerState state) {
		Carta[] cartas2 = state.getCartas();
		playerID = state.getID();
		cartas = new Carta[CARDS_PER_HAND];
		for (int i = 0; i<CARDS_PER_HAND; i++) {
			cartas[i] = new Carta(cartas2[i].getId());
		}
		reemplazar=new boolean[CARDS_PER_HAND];
		for (int i = 0; i<CARDS_PER_HAND; i++) {
			reemplazar[i] = this.reemplazar[i];
		}
		commitedToDiscard = state.isCommitedToDiscard();
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

	public void markForReplacement(byte i) {
		reemplazar[i] = true;

	}

	public void unmarkAll() {
		for (int i = 0; i< CARDS_PER_HAND; i++) {
			reemplazar[i] = false;
		}

	}

	public byte numeroCerdos() {
		byte cerdos = 0;
		for (int i = 0; i < CARDS_PER_HAND; i++) {
			if (cartas[i].isCerdo())
				cerdos++;
		}
		return cerdos;
	}

	// for legacy compatibility
	public ArrayList<Carta> getMano() {
		ArrayList<Carta> ret = new ArrayList<Carta>();
		for (int i = 0; i < CARDS_PER_HAND; i++) {
			ret.add(this.getCarta(i));
		}
		return ret;
	}

	public int numeroPitos() {
		byte pitos = 0;
		for (int i = 0; i < CARDS_PER_HAND; i++) {
			if (cartas[i].isPito())
				pitos++;
		}
		return pitos;
	}

	public TipoPares valorPares() {

		// we only want the values. so we get rid of the palo
		// so we can use the frequency method later
		ArrayList<Integer> mano = new ArrayList<Integer>();

		for (int i= 0; i <CARDS_PER_HAND; i++) {
			if (cartas[i]!= null)
				mano.add((int) cartas[i].valorPares());
		}

		// in another arraylist we will store frequency of 
		// elements
		// possibilities: {1, 1, 1, 1}, {2,1,1},{3,1},{2,2},{4}


		ArrayList<Integer> conjuntoPares = new ArrayList<Integer>();    	    	
		ArrayList<Integer> yaLeidos = new ArrayList<Integer>();

		for (int v : mano) {
			if (!yaLeidos.contains(v)) {
				System.out.println("añadiendo conjunto:" + Collections.frequency(mano, v));
				conjuntoPares.add(Collections.frequency(mano, v));
				yaLeidos.add(v);
			}


		}


		if ((conjuntoPares.get(0)==2 && conjuntoPares.get(1)==2) || (conjuntoPares.get(0)==4)) {
			return TipoPares.DUPLES;
		} 
		else
			if ((conjuntoPares.get(0)==3 && conjuntoPares.get(1)==1) || (conjuntoPares.get(0)==1 && conjuntoPares.get(1)==3)) {
				return TipoPares.MEDIAS;
			}
			else
				if ((conjuntoPares.get(0)==2) || (conjuntoPares.get(1)==2) || (conjuntoPares.get(2)==2)) {
					return TipoPares.PAR;
				}
				else
					return TipoPares.NO_PAR;
	}

	public int valorJuego() {
		int valor = 0;
		for (int i = 0; i < CARDS_PER_HAND; i++) {
			valor+=cartas[i].valor();			
		}
		return valor;
	}

	public Carta getCartaByCount(int n) {
		if ((n > CARDS_PER_HAND ) || n < 1) {
			throw new IllegalArgumentException();
		}
		// lets make it simple
		// array of four integers, store in each index freq of the card
		// return first pos with that freq
		int[] frequency = new int[4];
		for (int i=0;i<CARDS_PER_HAND;i++) {
			frequency[i] = 0;
		}
		for (int i=0;i<CARDS_PER_HAND;i++) {
			int valorActual = cartas[i].valor();
			for (int j=0;j<CARDS_PER_HAND;j++) {
				if (cartas[j].valor() == valorActual) {
					frequency[i]++;
				}
			}
		}
		for (int i = 0; i<CARDS_PER_HAND; i++) {
			if (frequency[i] == n)
				return cartas[i];
		}
		return null;
	}

}
