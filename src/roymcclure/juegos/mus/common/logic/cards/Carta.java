package roymcclure.juegos.mus.common.logic.cards;

import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.CARDS_PER_SUIT;

import java.io.Serializable;
import java.util.Comparator;

public class Carta implements Serializable, Comparator<Carta> {

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
	
	public static boolean isCerdo(byte card_id) {
    	// keep rest of dividing card_id by CARDS_PER_SUIT
    	// rest is either 2 or 11
    	card_id = (byte) (card_id % CARDS_PER_SUIT);
    	return card_id==2 || card_id == 11;
    }
	
	// so the tricky part here is:
	// a card id is card_value -1.
	// so we need to take that into account
	public byte valorPares() {
		byte num = (byte) (this.getId() % CARDS_PER_SUIT);
		if (num== 2 || num == 11)
			return 10;
		if (num == 1)
			return 1;
		return (byte) (num + 1);
	}

	public byte valorJuego() {
		byte num = (byte) (this.getId() % CARDS_PER_SUIT);
		if (num== 2 || num== 9 || num == 10 || num == 11)
			return 10;
		if (num == 1)
			return 1;
		return (byte) (num+1);
	}

	public byte valor() {
		byte num = (byte) (this.getId() % CARDS_PER_SUIT);
		if (num== 2 || num== 9 || num == 10 || num == 11)
			return 10;
		if (num == 1)
			return 1;
		return (byte) (num + 1);
	}	

	public static boolean isPito(byte card_id) {
		card_id = (byte) (card_id % CARDS_PER_SUIT);
    	return card_id==0 || card_id == 1;
	}

	@Override
	public int compare(Carta arg0, Carta arg1) {
		return ((Integer)((int)arg0.valor())).compareTo((Integer)((int)arg1.valor())); 
	}
	
	public boolean compareTo(Carta otraCarta) {
		if (otraCarta.valor() > this.valor())
			return false;
		else return true;
	}
	
}
