package roymcclure.juegos.mus.common.network;

import java.io.Serializable;

import roymcclure.juegos.mus.common.logic.Language;

public class ClientMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 458220917665843387L;
	private byte action;
	private byte quantity;
	// used to communicate a player ID, ours when requesting connection, msg originator's when broadcast	
	private String info; 
	
	public ClientMessage(ClientMessage cm) {
		this.action = cm.getAction();
		this.quantity = cm.getQuantity();
		this.info = cm.getInfo();
	}
	
	public ClientMessage(byte playerAction, byte playerQty, String info2) {
		this.action = playerAction;
		this.quantity = playerQty;
		this.info = info2;		
	}
	
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
			this.info = info;			
	}
	
	public byte getAction() {
		return action;
	}
	public void setAction(byte action) {
		this.action = action;

	}
	public byte getQuantity() {
		return quantity;
	}
	public void setQuantity(byte quantity) {
		this.quantity = quantity;
	}
	
	@Override
	public String toString() {
		String content = "==CLIENT MESSAGE==\n";

		switch(action) {
		case Language.PlayerActions.REQUEST_SEAT:
			content += "ACTION: SEAT REQUEST\n";
			break;
		case Language.PlayerActions.ACCEPT:
			content += "ACTION: ACCEPT\n";
			break;
		case Language.PlayerActions.ENVITE:
			content += "ACTION: ENVITE\n";
			break;
		case Language.PlayerActions.HANDSHAKE:
			content += "ACTION: HANDSHAKE\n";
			break;
		case Language.PlayerActions.ORDAGO:
			content += "ACTION: ORDAGO\n";
			break;
		case Language.PlayerActions.PASS:
			content += "ACTION: PASS\n";
			break;
		case Language.PlayerActions.REQUEST_GAME_STATE:
			content += "ACTION: REQUEST GAME STATE\n";
			break;
		case Language.PlayerActions.MUS:
			content += "ACTION: DARSE MUS\n";
			break;			
		case Language.PlayerActions.CORTO_MUS:
			content += "ACTION: CORTAR MUS\n";
			break;
		case Language.PlayerActions.DESCARTAR:
			content += "ACTION: DESCARTAR\n";
			break;			
		default:
			content += "ACTION: UNKNOWN TYPE!!!\n";
			break;
		}
		content+="QUANTITY: " + getQuantity() + "\n";
		content+="INFO: " + getInfo() + "\n";

		return content;
	}

}
