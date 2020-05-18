package roymcclure.juegos.mus.common.logic.cards;

import java.io.Serializable;

public class Carta implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1005985398336067321L;

	private byte id;
	
	public Carta(byte id) {
		this.id = id;
	}

	public byte getId() {
		return id;
	}

	public void setId(byte id) {
		this.id = id;
	}
	
	public static boolean is89(byte val) {
		val = (byte) (val % 12);
		if (val == 0)
			return false;
		if (val %  7 == 0) {
			return true;
		}
		if (val %  8 == 0) {
			return true;
		}		
		return false;
	}
	
}
