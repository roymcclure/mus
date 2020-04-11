package roymcclure.juegos.mus.common.network;

import java.io.Serializable;

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
	
}
