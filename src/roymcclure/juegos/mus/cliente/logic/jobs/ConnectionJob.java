package roymcclure.juegos.mus.cliente.logic.jobs;

import roymcclure.juegos.mus.common.network.ClientMessage;

public class ConnectionJob extends Job {

	private boolean reply_needed = false;
	
	ClientMessage cm;
	
	public ConnectionJob(ClientMessage cm) {
		this.cm = cm;
	}
	
	public boolean isReplyNeeded() {
		return reply_needed;
	}
	
	public ClientMessage getClientMessage() {
		return cm;
	}
	
	public void setReplyNeeded(boolean s) {
		reply_needed = s;
	}
	
	
}
