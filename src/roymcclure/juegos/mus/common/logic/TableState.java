package roymcclure.juegos.mus.common.logic;

import java.io.Serializable;

import roymcclure.juegos.mus.common.logic.Language.GamePhase;
import roymcclure.juegos.mus.common.logic.Language.TipoPares;
import roymcclure.juegos.mus.common.logic.cards.Baraja;
import roymcclure.juegos.mus.common.logic.cards.Carta;
import roymcclure.juegos.mus.common.logic.cards.Jugadas;

import static roymcclure.juegos.mus.cliente.logic.ClientGameState.table;
import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;
import static roymcclure.juegos.mus.common.logic.Language.GamePhase.*;
/*
 * 
 * State of game data unrelated to connection.
 * 
 */

public class TableState implements Serializable  {

	/**
	 * THIS CLASS CONTAINS DATA THAT CAN CHANGE ALONG THE GAME
	 */
	private static final long serialVersionUID = -1307767440829066871L;

	private PlayerState[] clients;
	private Baraja baraja;
	private Baraja barajaDescartes;
	private boolean ordago_lanzado = false;
	private byte mano_id = -1;
	private boolean[] pares; // tells the client what can be done in the round
	private boolean[] juego;
	private boolean[] enPaso; // for each of the lances, tells us 


	// domain specific data
	private byte 	piedras_norte_sur = 0, piedras_oeste_este = 0, 
			juegos_norte_sur = 0, juegos_oeste_este = 0,
			vacas_norte_sur = 0, vacas_oeste_este = 0,
			jugador_debe_hablar = -1, // index of the SEAT_ID not THREAD_ID
			piedras_envidadas_ronda_actual = 0,
			piedras_envidadas_a_grande = 0, // these will store a value != 0 ONLY when a bet was accepted
			piedras_envidadas_a_chica = 0,
			piedras_envidadas_a_pares = 0,
			piedras_envidadas_a_juego = 0,
			piedras_en_ultimo_envite = 0,
			piedras_acumuladas_en_apuesta = 0, // this is what the "pot" contains
			ultimo_envidador = -1, // used to know who to assign the pot to in case of fold
			jugadores_hablado_en_turno_actual = 0, // used to know when all players have talked, 
			//  also knowing if the person sending the request is the first bidder
			id_ronda = 0, //number that increases with each new round (a set of the four different lances)
			tipo_lance = 0, //identifier of the lance type
			previous_lance; // used to know where do we come from. useful for ordagos e.g.



	///// CONSTRUCTORS

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
		this.pares = new boolean[MAX_CLIENTS];
		this.juego = new boolean[MAX_CLIENTS];
		this.enPaso = new boolean[JUEGO+1];
		for (int i = 0; i < MAX_CLIENTS; i++) {
			this.pares[i] = false;
			this.juego[i] = false;
		}
		
		for (int i = 0; i <= JUEGO; i++) {
			this.enPaso[i] = true;
		}
		
	}

	public TableState(PlayerState[] clients, byte piedras_norte_sur, byte piedras_oeste_este, byte juegos_norte_sur,
			byte juegos_oeste_este, byte vacas_norte_sur, byte vacas_oeste_este, byte jugador_debe_hablar,
			byte piedras_envidadas_ronda_actual, byte id_ronda, byte tipo_lance, Baraja baraja,
			byte piedras_envidadas_a_grande, byte piedras_envidadas_a_chica, byte piedras_envidadas_a_pares, byte piedras_envidadas_a_juego,
			byte jugadores_hablado_en_turno_actual, byte ultimo_envidador, byte piedras_acumuladas_en_apuesta, boolean ordago_lanzado,
			byte mano_id, byte piedras_en_ultimo_envite, boolean[] pares, boolean[] juego, boolean[] enPaso, byte previous_lance) {

		super();
		this.clients = new PlayerState[MAX_CLIENTS];
		this.pares = new boolean[MAX_CLIENTS];
		this.juego = new boolean[MAX_CLIENTS];
		this.enPaso = new boolean[JUEGO+1];
		for (int i = 0; i < MAX_CLIENTS; i++) {
			this.clients[i] = new PlayerState(clients[i]);
			this.pares[i] = pares[i];
			this.juego[i] = juego[i];
		}
		for (int i = 0; i <= JUEGO; i++) {
			this.enPaso[i] = enPaso[i];
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
		this.ultimo_envidador = ultimo_envidador;
		this.piedras_acumuladas_en_apuesta = piedras_acumuladas_en_apuesta;
		this.ordago_lanzado = ordago_lanzado;
		this.mano_id = mano_id;
		this.piedras_en_ultimo_envite = piedras_en_ultimo_envite;
		this.baraja = baraja.clone();
		// i purposefully do not include discarded deck here. clients shouldnt know whats in it.
		this.previous_lance = previous_lance;
	}


	// clone the TableState for a particular player
	public TableState clone(String playerID) {
		TableState tableCopy =new TableState(clients, piedras_norte_sur, piedras_oeste_este, 
				juegos_norte_sur, juegos_oeste_este,
				vacas_norte_sur, vacas_oeste_este,jugador_debe_hablar, piedras_envidadas_ronda_actual,
				id_ronda,tipo_lance, baraja, piedras_envidadas_a_grande, piedras_envidadas_a_chica, 
				piedras_envidadas_a_pares, piedras_envidadas_a_juego, jugadores_hablado_en_turno_actual, ultimo_envidador,
				piedras_acumuladas_en_apuesta, ordago_lanzado, mano_id, piedras_en_ultimo_envite, pares, 
				juego, enPaso, previous_lance);
		// remove all data not pertinent to playerID
		// conceal what baraja contains
		tableCopy.baraja.conceal();
		// conceal other player's cards - only when gamephase is not END_OF_ROUND
		if (getGamePhase()!=FIN_RONDA) {
			for (int i = 0; i < MAX_CLIENTS; i++) {
				if (clients[i] != null && !clients[i].getID().equals(playerID)) {
					Carta[] cartas = new Carta[CARDS_PER_HAND];
					for (int j = 0; j< CARDS_PER_HAND;j++) {
						cartas[j] = new Carta(ID_CARTA_DORSO);
					}
					tableCopy.getClient(i).setCartas(cartas);
				}			
			}
		}

		// we shouldnt need to conceal pares & juego; their values will be set only when the time comes

		return  tableCopy;
	}	

	///// GETTERS & SETTERS	

	public byte getMano_seat_id() {
		return mano_id;
	}


	public void setMano_seat_id(byte mano_id) {
		this.mano_id = mano_id;
	}


	public boolean isOrdago_lanzado() {
		return ordago_lanzado;
	}


	public void setOrdago_lanzado(boolean ordago_lanzado) {
		this.ordago_lanzado = ordago_lanzado;
	}

	public byte getPiedras_en_ultimo_envite() {
		return piedras_en_ultimo_envite;
	}


	public void setPiedras_en_ultimo_envite(byte piedras_en_ultimo_envite) {
		this.piedras_en_ultimo_envite = piedras_en_ultimo_envite;
	}


	public byte getPiedras_acumuladas_en_apuesta() {
		return piedras_acumuladas_en_apuesta;
	}


	public void setPiedras_acumuladas_en_apuesta(byte piedras_acumuladas_en_apuesta) {
		this.piedras_acumuladas_en_apuesta = piedras_acumuladas_en_apuesta;
	}


	public byte getUltimo_envidador() {
		return ultimo_envidador;
	}


	public void setUltimo_envidador(byte ultimo_envidador) {
		this.ultimo_envidador = ultimo_envidador;
	}




	// allows me to know if a bid is a new bid, or bidding more
	public byte getJugadores_hablado_en_turno_actual() {
		return jugadores_hablado_en_turno_actual;
	}


	public void setJugadores_hablado_en_turno_actual(byte jugadores_hablado_en_turno_actual) {
		this.jugadores_hablado_en_turno_actual = jugadores_hablado_en_turno_actual;
	}


	public void setPiedras_envidadas(byte lance, byte piedras) {
		switch(lance) {
		case GRANDE:
			this.piedras_envidadas_a_grande = piedras;
			break;
		case CHICA:
			this.piedras_envidadas_a_chica = piedras;			
			break;
		case PARES:
			this.piedras_envidadas_a_pares = piedras;			
			break;
		case JUEGO:
			this.piedras_envidadas_a_juego = piedras;			
			break;
		}		

	}

	public byte getPiedras_envidadas_a_grande() {
		return piedras_envidadas_a_grande;
	}	

	public byte getPiedras_envidadas_a_chica() {
		return piedras_envidadas_a_chica;
	}

	public byte getPiedras_envidadas_a_pares() {
		return piedras_envidadas_a_pares;
	}

	public byte getPiedras_envidadas_a_juego() {
		return piedras_envidadas_a_juego;
	}


	public void setPiedras_envidadas_a_juego(byte piedras_envidadas_a_juego) {
		this.piedras_envidadas_a_juego = piedras_envidadas_a_juego;
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

	public void setJugador_debe_hablar(byte jugador_debe_hablar) {
		this.jugador_debe_hablar = jugador_debe_hablar;
	}

	public byte getPiedras_envidadas_ronda_actual() {
		return piedras_envidadas_ronda_actual;
	}

	public void setPiedras_envidadas_ronda_actual(byte piedras_envidadas_ronda_actual) {
		this.piedras_envidadas_ronda_actual = piedras_envidadas_ronda_actual;
	}

	public byte getId_ronda() {
		return id_ronda;
	}

	public byte getGamePhase() {
		return tipo_lance;
	}

	public boolean isSeatOccupied(int i) {
		return !clients[i].getID().equals("");

	}	

	public boolean isSeatEmpty(byte seat_id) {
		return clients[seat_id].getID().equals("");
	}

	public boolean tienePares(int i) {
		return pares[i];
	}

	public boolean tieneJuego(int i) {
		return juego[i];
	}


	////////////// ADDITIONAL BEHAVIOUR
	/// TODO: this should be in the controller???
	/// after all, he is responsible of state modification


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

		for (int i = 0; i < MAX_CLIENTS; i++) {
			System.out.println(clients[i].getID() + (pares[i]?"":" no ") + " tiene pares");
		}

	}

	public void repartir() {
		try {
			for (int i = 0; i<MAX_CLIENTS;i++) {
				Carta[] cartas = new Carta[CARDS_PER_HAND];
				for (int j = 0; j<CARDS_PER_HAND;j++) {
					if (clients[i].isMarkedForReplacement(j))
						if (this.baraja.size()>0) {
							cartas[j] = this.baraja.sacarCarta(0);							
						}else {
							cartas[j] = this.barajaDescartes.sacarCarta(0);
						}

					else cartas[j] = clients[i].getCarta(j);
				}
				clients[i].setCartas(cartas);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public boolean allSeated() {
		for (byte i = 0; i < MAX_CLIENTS; i++) {
			if (isSeatEmpty(i))
				return false;
		}
		return true;
	}

	public void advanceTalkingPlayer() {
		this.jugador_debe_hablar -= 1;
		if (this.jugador_debe_hablar==-1)
			this.jugador_debe_hablar=MAX_CLIENTS - 1;

	}


	public void setGamePhase(byte lance) {
		this.previous_lance = this.tipo_lance;
		this.tipo_lance = lance;
		this.onGamePhaseChange();
	}

	private void onGamePhaseChange() {
		if (tipo_lance == FIN_RONDA) {
			if (isOrdago_lanzado()) {
				byte winner=getGanador(previous_lance);
				System.out.println("ganador de ese ordago es " + winner);
				addJuegoToTeamOf(winner);				
			}	else {
				// hacer los cambios de estado que se visualizan en showSpeechBubblesForEndOfRound
				for (byte i = GRANDE; i <= JUEGO; i++) {
					// para cada ronda en orden
					// si la ronda era jugable
					if (rondaWasPlayable(i)) {
						
					}
					byte winner_seat_id = table().getGanador(i);
					// 	si quedó en paso, se le da al ganador					
					if (table().lanceQuedoEnPaso(i)) { 
						addPiedrasToTeamOf(winner_seat_id, (byte) 1);
					} else {
						addPiedrasToTeamOf(winner_seat_id, piedrasApostadasEnRonda(i));						
					}
				}				
				
			}
		}		
	}	

	

	public boolean rondaWasPlayable(byte tipo_lance) {
		switch(tipo_lance) {
		case GRANDE:
		case CHICA:
			return true;
		case PARES:
			return seJueganPares();
		case JUEGO:
			return seJuegaJuego();
		}
		return false;

	}
	
	public byte piedrasApostadasEnRonda(byte i) {
		switch(i) {
		case GRANDE:
			return piedras_envidadas_a_grande; 
		case CHICA:
			return piedras_envidadas_a_chica;
		case PARES:
			return piedras_envidadas_a_pares;
		case JUEGO:
			return piedras_envidadas_a_juego;
		}
		return 0;
	}

	public boolean allPlayersTalked() {
		return getJugadores_hablado_en_turno_actual()==MAX_CLIENTS;
	}

	public void increaseTalkedPlayers() {
		jugadores_hablado_en_turno_actual++;		
	}

	public void resetTalked() {
		jugadores_hablado_en_turno_actual = 0;
	}


	public boolean allPlayersCommitedToDiscard() {
		for (int i = 0; i < MAX_CLIENTS; i++) {
			if (!clients[i].isCommitedToDiscard())
				return false;
		}
		return true;
	}


	public void moveDiscarded() {
		// moves discarded cards to the discarded deck
		for (int i = 0; i < MAX_CLIENTS; i++) {
			for (int j=0;j<CARDS_PER_HAND;j++) {
				if (clients[i].isMarkedForReplacement(j))
					this.barajaDescartes.addCarta(clients[i].getCarta(j));
			}
		}

	}


	public void uncommitToDiscard() {
		for (int i = 0; i < MAX_CLIENTS; i++) {
			clients[i].setCommitedToDiscard(false);
		}

	}

	// nos devuelve el seat_id de la mano del equipo donde no está seat_id
	// TODO: no me termina de encantar. set for refactor.
	public byte getManoOtroEquipo(byte player_seat_id) {
		int ret;
		byte mano = getMano_seat_id();
		// get id of current mano
		if (mano % 2 == player_seat_id % 2) {
			ret = (getMano_seat_id()-1);
		} else ret = getMano_seat_id();
		if (ret==-1)
			ret = 3;
		return (byte)ret;
	}


	public boolean isPostreEnSuEquipo(byte player_seat_id) {
		if (getMano_seat_id()== player_seat_id)
			return false;
		else if (getMano_seat_id() == (player_seat_id + 1) % MAX_CLIENTS)
			return false;
		return true;
	}

	public boolean isPostre(byte player_seat_id) {
		return player_seat_id == TableState.previousTableSeatId(getMano_seat_id());
	}

	// increases the game phase
	// sets pares and juego 
	public void advanceLance() {
		tipo_lance++;
		if (tipo_lance==GamePhase.PARES ) {
			// fill the pares boolean array
			for (int i = 0; i < MAX_CLIENTS; i++) {
				if(clients[i].valorPares()!=TipoPares.NO_PAR) {
					pares[i] = true;
				}
				else pares[i] = false;
			}
		} 
		else if (tipo_lance==GamePhase.JUEGO) {
			// fill the juego boolean array
			for (int i = 0; i < MAX_CLIENTS; i++) {
				if(clients[i].valorJuego()>30) {
					juego[i] = true;
				}
				else juego[i] = false;
			}			
		}		
	}

	// receives the seat_id of one of the members of the team receiving the pot
	// and assigns pot depending on its parity 
	// actually it can receive a non existing id, but that's fine for this game
	// since there's only two teams
	public void givePotTo(int i) {
		// so, even is team north_south
		// and odd is team west_east
		if (i%2==0) {
			if (piedras_acumuladas_en_apuesta == 0) {
				if (this.getGamePhase()==JUEGO) {
					piedras_norte_sur+=2;					
				} else {
					piedras_norte_sur+=1;
				}
			} else {
				piedras_norte_sur+= piedras_acumuladas_en_apuesta;				
			}
		}	
		else {
			if (piedras_acumuladas_en_apuesta == 0) {
				if (this.getGamePhase()==JUEGO) {
					piedras_oeste_este+=2;					
				} else {
					piedras_oeste_este+=1;
				}
			} else {
				piedras_oeste_este+= piedras_acumuladas_en_apuesta;				
			}
		}
		piedras_acumuladas_en_apuesta = 0;
	}

	// TODO: adjust functions. this depends if players talk clockwise or ccw
	public static byte previousTableSeatId(byte seat_id) {
		return (byte) ((seat_id + 1 == MAX_CLIENTS) ? 0 : (seat_id + 1 ));
	}

	public static byte nextTableSeatId(byte seat_id) {
		return (byte) ((seat_id - 1 == -1) ? MAX_CLIENTS - 1 : (seat_id - 1 ));
	}

	// pasa al siguiente lance y determina quién debe hablar
	public void pasarASiguienteLance() {
		resetRoundScore();
		this.advanceLance();
		setJugadores_hablado_en_turno_actual((byte) 0);
		if (tipo_lance == PARES && seJueganPares()) {
			setJugador_debe_hablar(primeroConPares());
		} else if (tipo_lance == JUEGO && seJuegaJuego()) {
			if (seJuegaAlPunto()) {
				setJugador_debe_hablar(mano_id);
			} else {
				setJugador_debe_hablar(primeroConJuego());
			}
		} else if (tipo_lance == FIN_RONDA) {

		} else {
			setJugador_debe_hablar(getMano_seat_id());	
		}
	}

	private void resetRoundScore() {
		byte n = 0;
		setPiedras_acumuladas_en_apuesta(n);
		setPiedras_envidadas_ronda_actual(n);
		setOrdago_lanzado(false);
	}

	private byte primeroConPares() {
		byte i = getMano_seat_id();
		do {
			if (pares[i])
				return i;
			i = nextTableSeatId(i);
		} while(i!=getMano_seat_id());
		return -1;
	}

	private byte primeroConJuego() {
		byte i = getMano_seat_id();
		do {
			if (juego[i])
				return i;
			i = nextTableSeatId(i);
		} while(i!=getMano_seat_id());
		return -1;
	}	

	public boolean seJueganPares() {
		byte sig = nextTableSeatId(mano_id);
		return (tienePares(mano_id) || tienePares(opuesto(mano_id))) && (tienePares(sig) || tienePares(opuesto(sig)));  
		
	}

	public boolean seJuegaAlPunto() {
		byte sig = nextTableSeatId(mano_id);
		return !tieneJuego(mano_id) && !tieneJuego(opuesto(mano_id)) && !tieneJuego(sig) && !tieneJuego(opuesto(sig));
	}
	
	public boolean seJuegaJuego() {
		byte sig = nextTableSeatId(mano_id);
		boolean envite = (tieneJuego(mano_id) || tieneJuego(opuesto(mano_id))) && (tieneJuego(sig) || tieneJuego(opuesto(sig)));
		return envite || seJuegaAlPunto() ;
	}

	// based on the table state, sets who the next person to talk should be
	public void assignNextTalker() {
		byte player_seat_id = getJugador_debe_hablar();
		// next person that should talk is the mano of the other team
		// for grande and chica, and the first player that can talk in pares and juego
		if (getGamePhase()==GRANDE || getGamePhase() == CHICA) {
			byte mano_otro_equipo = getManoOtroEquipo(player_seat_id);
			setJugador_debe_hablar(mano_otro_equipo);
		} else if (getGamePhase() == PARES){
			byte mano_otro_equipo = getManoOtroEquipo(player_seat_id);					
			if (pares[mano_otro_equipo]) {
				setJugador_debe_hablar(mano_otro_equipo);
			} else {						
				setJugador_debe_hablar(opuesto(mano_otro_equipo));						
			}
		} else if (getGamePhase() == JUEGO){
			byte mano_otro_equipo = getManoOtroEquipo(player_seat_id);					
			if (juego[mano_otro_equipo]) {
				setJugador_debe_hablar(mano_otro_equipo);
			} else {						
				setJugador_debe_hablar(opuesto(mano_otro_equipo));						
			}
		}

	}

	public byte opuesto(byte seat_id) {
		return (byte) ((seat_id + 2) % MAX_CLIENTS);
	}

	public boolean isUltimoConPares(byte player_seat_id) {
		int counter = 0;
		byte ultimoConPares = -1; 		
		for (byte i = getMano_seat_id(); counter < MAX_CLIENTS; i--) {
			if (i == -1)
				i = MAX_CLIENTS - 1;
			if (pares[i])
				ultimoConPares = i;

			counter++;
		}		
		return ultimoConPares==player_seat_id;
	}

	public byte postre() {
		return previousTableSeatId(mano_id);
	}

	public byte siguienteConPares(byte player_seat_id) {
		for (int i = 0; i < MAX_CLIENTS; i++) {
			player_seat_id = nextTableSeatId(player_seat_id);
			if (pares[player_seat_id])
				return player_seat_id;			
		}
		return -1;
	}

	public boolean isUltimoConJuego(byte player_seat_id) {
		int counter = 0;
		byte ultimoConJuego = -1; 		
		for (byte i = getMano_seat_id(); counter < MAX_CLIENTS; i--) {
			if (i == -1)
				i = MAX_CLIENTS - 1;
			if (juego[i])
				ultimoConJuego = i;

			counter++;
		}		
		return ultimoConJuego==player_seat_id;
	}

	public byte siguienteConJuego(byte player_seat_id) {
		while ((player_seat_id = nextTableSeatId(player_seat_id))!=mano_id) {
			if (juego[player_seat_id])
				return player_seat_id;
		}
		return -1;
	}

	public void addJuegoToTeamOf(byte winner) {

		if (winner % 2 == 0) {
			juegos_norte_sur++;
		} else {
			juegos_oeste_este++;
		}
	}
	
	public void addPiedrasToTeamOf(byte winner, byte piedras) {
		if (winner % 2 == 0) {
			piedras_norte_sur+=piedras;
		} else {
			piedras_oeste_este+=piedras;
		}		
	}

	public boolean vacaAlcanzada(byte juegos_vaca) {
		return (juegos_norte_sur == juegos_vaca) || (juegos_oeste_este == juegos_vaca);
	}

	// if some team has reached juegos_vaca
	public void asignarVaca(byte juegos_vaca) {
		if (juegos_norte_sur == juegos_vaca) {
			vacas_norte_sur++;
		}else if(juegos_oeste_este == juegos_vaca) {
			vacas_oeste_este++;
		}
		juegos_norte_sur = 0;
		juegos_oeste_este = 0;

	}

	public boolean partidaAlcanzada(byte vacas_por_partida) {
		return (vacas_norte_sur == vacas_por_partida) || (vacas_oeste_este == vacas_por_partida);		
	}

	public void asignarPartida(byte vacas_por_partida) {
		if (vacas_norte_sur == vacas_por_partida) {
			System.out.println("LA PARTIDA ES DE EL EQUIPO NORTE/SUR!!");
		} else {
			System.out.println("LA PARTIDA ES DE EL EQUIPO OESTE_ESTE!!");			
		}


	}

	public int piedrasEnJugador(int i) {
		switch (i) {
		case 0:
			return piedras_norte_sur / AMARRAKO_GORDO;
		case 1:
			return piedras_oeste_este % AMARRAKO_GORDO;
		case 2:
			return piedras_norte_sur % AMARRAKO_GORDO;
		case 3:
			return piedras_oeste_este / AMARRAKO_GORDO;
		}
		return 0;
	}

	public byte getPreviousLance() {
		return previous_lance;
	}

	// returns the absolute seat id of ganador ordago
	// TODO: these cases are very similar, refactor into method
	public byte getGanador(byte tipo_lance) {
		// chica y grande:partiendo de la mano, buscamos la jugada más grande
		byte winner = -1, sig, inicial;
		switch(tipo_lance) {
		case GRANDE:
			winner = getMano_seat_id();
			System.out.println("WINNER es " + winner);

			sig = nextTableSeatId(winner);
			System.out.println("SIG es " + sig);
			for (byte i = 1; i < MAX_CLIENTS; i++) {
				if (!Jugadas.ganaAGrande(this.getClient(winner), this.getClient(sig))) {
					winner = sig;
				}
				sig = nextTableSeatId(sig);
			}
			break;
		case CHICA:
			winner = getMano_seat_id();
			sig = nextTableSeatId(winner);
			for (byte i = 1; i < MAX_CLIENTS; i++) {
				if (!Jugadas.ganaAChica(this.getClient(winner), this.getClient(sig))) {
					winner = sig;
				}
				sig = nextTableSeatId(sig);
			}
			break;		
		case PARES:
			winner = primeroConPares();
			inicial = winner;			
			sig = siguienteConPares(winner);
			while(sig!=inicial) {
				if (!Jugadas.ganaAPares(this.getClient(winner), this.getClient(sig))) {
					winner = sig;
				}
				sig = siguienteConPares(sig);
			}
			break;
		case JUEGO:
			if (seJuegaAlPunto()) {
				winner = mano_id;
				
			} else {
				winner = primeroConJuego();
				inicial = winner;			
				sig = siguienteConJuego(winner);
				while(sig!=inicial) {
					if (!Jugadas.ganaJuego(this.getClient(winner), this.getClient(sig))) {
						winner = sig;
					}
					sig = siguienteConPares(sig);
				}	
			}
						
			break;
		}
		return winner;		
	}

	public void setEnPaso(byte game_phase, boolean value) {
		enPaso[game_phase] = value;
	}
	
	public boolean lanceQuedoEnPaso(byte game_phase) {
		return enPaso[game_phase];
	}


}
