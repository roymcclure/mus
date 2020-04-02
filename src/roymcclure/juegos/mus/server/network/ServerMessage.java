package roymcclure.juegos.mus.server.network;

import roymcclure.juegos.mus.server.logic.GameState;

public class ServerMessage {
	
	public final int MAX_TXT_SIZE = 80;
	private char[] text = new char[MAX_TXT_SIZE];
	private byte estadoPartida;
	
	public ServerMessage() {}
	
	public ServerMessage(String text) {
		for (int i = 0; i < MAX_TXT_SIZE; i++) { 
            this.text[i] = text.charAt(i); 
        }		
	}
	
	public static ServerMessage forgeDataPacket(GameState gs) {
		ServerMessage gdp = new ServerMessage();
		gdp.setEstadoPartida(gs.getGameState());
		return gdp;
	}

	private void setEstadoPartida(byte estadoPartida) {
		this.estadoPartida = estadoPartida;
	}
	
	
	
}
