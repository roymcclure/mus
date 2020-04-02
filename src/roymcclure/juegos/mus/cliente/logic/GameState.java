package roymcclure.juegos.mus.cliente.logic;

// esta clase refleja el estado de la partida
// baraja, cartas en cada mano, piedras
// quien es mano (postre se determina de manera implícita)
// 

public class GameState {
	
	public static final int WAITING_ALL_PLAYERS_TO_SEAT = 0,
		PLAYING = 1,
		END_OF_ROUND = 2;	
	
	public static final int READ_FROM_CLIENT = 0,
						WRITE_TO_CLIENT = 1,
						MUST_WAIT = 2;
	
	private byte estado_partida = 0; // 0 esperando que se llenen los sitios, 
	
	public byte getGameState() {
		return estado_partida;
	}
	
	public void setGameState(byte s) {
		this.estado_partida = s;
	}
	
	public byte getExpectedAction() {
		if(estado_partida == WAITING_ALL_PLAYERS_TO_SEAT) {
			
		}
		return 0;
	}
	
	
	
}
