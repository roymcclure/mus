package roymcclure.juegos.mus.common.network;

import java.io.Serializable;

import roymcclure.juegos.mus.common.logic.Language;
import roymcclure.juegos.mus.common.logic.TableState;
import roymcclure.juegos.mus.server.logic.ServerGameState;

/*
 * Containts connection-related data, and possibly
 * game state data.
 *  
 */

public class ServerMessage implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4450746104884476694L;
	public final int MAX_TXT_SIZE = 80;
	//private char[] nombre = new char[MAX_TXT_SIZE];
	private TableState tableState;
	private byte reply;



	public ServerMessage() {}

	public TableState getTableState() {
		return tableState;
	}


	public void setTableState(TableState tableState) {
		this.tableState = tableState;
	}	
	

/*
	public char[] getNombre() {
		return nombre;
	}


	public void setText(char[] nombre) {
		this.nombre = nombre;
	}
*/



	public int getMAX_TXT_SIZE() {
		return MAX_TXT_SIZE;
	}




	
	// [estado_partida, ronda_id, tipo_ronda_id, piedras_jugador_norte, piedras_jugador_este, piedras_jugador_sur, piedras_jugador_oeste, juegos_pareja_norte_sur, juegos_pareja_este_oeste, vacas_pareja_norte_sur, vacas_pareja_este_oeste,
	// carta_0_jugador_0, carta_1_jugador_0, carta_2_jugador_0, carta_3_jugador_0, carta_0_jugador_1, carta_1_jugador_1, carta_2_jugador_1, carta_3_jugador_1, carta_0_jugador_2, carta_1_jugador_2, carta_2_jugador_2, carta_3_jugador_2, 
	// carta_0_jugador_3, carta_1_jugador_3, carta_2_jugador_3, carta_3_jugador_3, id_jugador_que_le_toca_hablar, piedras_envidadas_en_ronda_actual]
	
	public static ServerMessage forgeDataPacket(ServerGameState gs, byte player_id, boolean includeGameState) {
		
		
		ServerMessage sm = new ServerMessage();
		
		if ( includeGameState ) {
			TableState ts = new TableState();
			// i believe this does not bring much to the table
			// from all the other state it should be implicit
			ts.setEstado_partida(gs.getGameState());

			// numero que identifica a la ronda
			ts.setId_ronda(gs.getId_ronda());
			// numero que identifica el tipo de ronda
			ts.setTipo_ronda(gs.getTipo_ronda());
			// piedras de cada jugador
			ts.setPiedras_norte(gs.getPiedras_norte());
			ts.setPiedras_sur(gs.getPiedras_sur());
			ts.setPiedras_este(gs.getPiedras_este());
			ts.setPiedras_oeste(gs.getPiedras_oeste());
			// 
			ts.setJuegos_esteoeste(gs.getJuegos_esteoeste());
			ts.setJuegos_nortesur(gs.getJuegos_nortesur());
			
			ts.setVacas_esteoeste(gs.getVacas_esteoeste());
			ts.setVacas_nortesur(gs.getVacas_nortesur());
			
			for (byte id_cliente=0; id_cliente < Language.GameDefinitions.MAX_CLIENTS; id_cliente++) {
				for (byte pos_carta = 0; pos_carta < Language.GameDefinitions.CARDS_PER_HAND; pos_carta++) {
					if (id_cliente == player_id || gs.getTipo_ronda() == Language.ServerGameState.END_OF_ROUND) {
						ts.setCarta(id_cliente,	pos_carta, gs.getCarta(id_cliente, pos_carta));
					}
					else {
						ts.setCarta(id_cliente,	pos_carta, Language.GameDefinitions.ID_CARTA_DORSO);
					}
					
				}
			}
			
			// player ids by seat
			
			for (byte i = 0; i< Language.GameDefinitions.MAX_CLIENTS; i++) {
				System.out.println("forging server message, seat:"+i+" is occupied by player:"+gs.getPlayerID_by_Seat(i));
				ts.setPlayerId_By_Seat(i, gs.getPlayerID_by_Seat(i));
			}
							
			ts.setId_jugador_que_le_toca_hablar(gs.getPlayer_in_turn());
			ts.setPiedras_envidadas_ronda_actual(gs.getPiedras_envidadas_ronda_actual());
			sm.setTableState(ts);
		}
		
		
		return sm;
	}
	
	public void printContent() {
		System.out.print("Estado de juego:");
		System.out.println("----------------");
		if (tableState != null) { 
			switch(tableState.getEstado_partida()) {
			case Language.ServerGameState.WAITING_ALL_PLAYERS_TO_SEAT:
				System.out.println("ESPERANDO A QUE TODOS SE SIENTEN");
				break;
			case Language.ServerGameState.PLAYING:
				System.out.println("JUGANDO");
				break;
			case Language.ServerGameState.GAME_FINISHED:
				System.out.println("JUEGo TERMINADO");
				break;
			case Language.ServerGameState.END_OF_ROUND:
				System.out.println("FIN DE LA RONDA");
				break;
			}
			System.out.println("Sentados:");
			//for (byte i=0; i<Language.GameDefinitions.MAX_CLIENTS;i++)	{
				System.out.println("Sitio " + 0 + ": " + tableState.getPlayerId_By_Seat((byte) 0));
				System.out.println("Sitio " + 1 + ": " + tableState.getPlayerId_By_Seat((byte) 1));
				System.out.println("Sitio " + 2 + ": " + tableState.getPlayerId_By_Seat((byte) 2));
				System.out.println("Sitio " + 3 + ": " + tableState.getPlayerId_By_Seat((byte) 3));			
		} else {
			System.out.println("El mensaje no contiene info del estado de la partida.");
		}
			
		//}
		
	}
	
	
}
