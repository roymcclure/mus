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
	private String info;
	
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
		default:
			content += "ACTION: UNKNOWN TYPE!!!\n";
			break;
		}
		content+="QUANTITY: " + getQuantity() + "\n";
		content+="INFO: " + getInfo() + "\n";

		return content;
	}

}
