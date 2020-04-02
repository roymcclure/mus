package roymcclure.juegos.mus.cliente.network;

import roymcclure.juegos.mus.cliente.logic.GameState;

public class ServerDataPacket {
	
	public final int MAX_TXT_SIZE = 80;
	private char[] text = new char[MAX_TXT_SIZE];
	private byte estadoPartida;
	
	public ServerDataPacket() {}
	
	public ServerDataPacket(String text) {
		for (int i = 0; i < MAX_TXT_SIZE; i++) { 
            this.text[i] = text.charAt(i); 
        }		
	}
	
	public static ServerDataPacket forgeDataPacket(GameState gs) {
		ServerDataPacket gdp = new ServerDataPacket();
		gdp.setEstadoPartida(gs.getGameState());
		return gdp;
	}

	private void setEstadoPartida(byte estadoPartida) {
		this.estadoPartida = estadoPartida;
	}
	
	
	
}
