package roymcclure.juegos.mus.cliente.logic.jobs;

import roymcclure.juegos.mus.common.network.ServerMessage;

public class ServerMessageJob extends Job {

	private ServerMessage sm;
	
	public ServerMessageJob(ServerMessage sm) {
		this.sm = sm;
	}
	
	public ServerMessage getServerMessage() {
		return this.sm;
	}
	
}
