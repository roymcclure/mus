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
	
	public boolean isCerdo() {
    	// keep rest of dividing card_id by CARDS_PER_SUIT
    	// rest is either 2 or 11
    	byte card_id = (byte) (this.getId() % CARDS_PER_SUIT);
    	return card_id==2 || card_id == 11;
    }
	
	// so the tricky part here is:
	// a card id is card_value -1.
	// so we need to take that into account
	// 1 for {0,1}, 3 for {2,11}
	public byte valorPares() {
		byte num = (byte) (this.getId() % CARDS_PER_SUIT);
		if (num== 2 || num == 11)
			return 3;
		if (num == 1)
			return 1;
		return (byte) (num + 1);
	}

	// valor de una carta en el contexto de juego
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
		if (num == 1)
			return 1;
		if (num== 2)
			return 12;
		return (byte) (num + 1);
	}	

	public boolean isPito() {
    	byte card_id = (byte) (this.getId() % CARDS_PER_SUIT);
    	return card_id==0 || card_id == 1;
	}

	@Override
	public int compare(Carta carta1, Carta carta2) {
		return (new Integer(carta1.valor())).compareTo(new Integer(carta2.valor())); 
	}
	
	public boolean compareTo(Carta otraCarta) {
		if (otraCarta.valor() > this.valor())
			return false;
		else return true;
	}
	
}
