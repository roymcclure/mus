package roymcclure.juegos.mus.common.logic.jobs;

import roymcclure.juegos.mus.common.network.ClientMessage;
import roymcclure.juegos.mus.common.network.ServerMessage;

public class ConnectionJob extends Job {

	private byte operation;
	
	ClientMessage cm;
	ServerMessage sm;
	
	public ConnectionJob(ClientMessage cm) {
		this.cm = cm;
	}
	
	public ConnectionJob(ServerMessage sm) {
		this.sm = sm;
	}
	
	public ClientMessage getClientMessage() {
		return cm;
	}
	
	public ServerMessage getServerMessage() {
		return sm;
	}
	
	public byte getOperation() {
		return operation;
	}

	public void setOperation(byte operation) {
		this.operation = operation;
	}
	
}
