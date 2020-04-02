package roymcclure.juegos.mus.common.network;

public class ClientMessage {

	private byte action;
	private byte quantity;
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
