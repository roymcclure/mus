package roymcclure.juegos.mus.common.logic.jobs;

import roymcclure.juegos.mus.common.network.*;

/*
 * Wraps a Message (either client or server) to be processed by the controller.
 */


public class MessageJob extends Job {

	private ServerMessage sm;
	private ClientMessage cm;
	
	public MessageJob(ServerMessage sm) {
		this.sm = sm;
	}

	public MessageJob(ClientMessage cm) {
		this.cm = cm;
	}	
	
	public ServerMessage getServerMessage() {
		return this.sm;
	}
	
	public ClientMessage getClientMessage() {
		return this.cm;
	}
	
}
