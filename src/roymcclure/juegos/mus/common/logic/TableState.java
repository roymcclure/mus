package roymcclure.juegos.mus.common.logic;

import java.io.Serializable;

/*
 * 
 * State of game data not related to connection.
 * 
 */

public class TableState implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private byte 
	estado_partida,
	id_ronda, tipo_ronda,
	piedras_norte, piedras_este, piedras_sur, piedras_oeste,
	juegos_nortesur, juegos_esteoeste, vacas_nortesur, vacas_esteoeste, 
	id_jugador_que_le_toca_hablar, piedras_envidadas_ronda_actual;



	private byte[][] cartas;

	// this ids correspond to the seats.
	//private String[] player_ids;
	public String[] player_ids;	
	
	public TableState() {
		
		cartas = new byte[Language.GameDefinitions.MAX_CLIENTS][Language.GameDefinitions.CARDS_PER_HAND];
		
	}
	
	public String getPlayerId_By_Seat(byte seat_id) {
		return player_ids[seat_id];
	}
	
	public void setPlayerId_By_Seat(byte seat_id, String id) {
		player_ids[seat_id] = id;
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


	public byte getId_jugador_que_le_toca_hablar() {
		return id_jugador_que_le_toca_hablar;
	}


	public void setId_jugador_que_le_toca_hablar(byte id_jugador_que_le_toca_hablar) {
		this.id_jugador_que_le_toca_hablar = id_jugador_que_le_toca_hablar;
	}


	public byte getPiedras_envidadas_ronda_actual() {
		return piedras_envidadas_ronda_actual;
	}


	public void setPiedras_envidadas_ronda_actual(byte piedras_envidadas_ronda_actual) {
		this.piedras_envidadas_ronda_actual = piedras_envidadas_ronda_actual;
	}	
	
	
}
