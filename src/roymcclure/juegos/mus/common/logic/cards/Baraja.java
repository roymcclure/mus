package roymcclure.juegos.mus.common.logic.cards;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;

public class Baraja implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8288123411520147398L;
	private List<Carta> baraja;
	
	public Baraja() {
		baraja = new ArrayList<Carta>();
	}
	
	public Carta sacarCarta(int index) throws Exception {
		if (baraja.size() == 0) {
			throw new Exception("Baraja vacía, no se puede sacar carta.");
		}
		Carta c = baraja.get(0);
		baraja.remove(0);
		return c;
	}
	
	public void barajar() {
		if (baraja.size()>1) {
			Random random = new Random();
			for (int i = 0; i< 100000; i++) {
				// elegir dos numeros aleatorios entre 0 y baraja.size() - 1
				int i1 = random.nextInt(baraja.size());
				int i2 = random.nextInt(baraja.size());				
				// intercambiar
				Carta aux = baraja.get(i1);
				baraja.set(i1, baraja.get(i2));
				baraja.set(i2, aux);
			}
		}
	}
	
	public void rellenar() {
		for (byte i = 0; i< TOTAL_CARDS;i++)
			if (!Carta.is89(i))
				baraja.add(new Carta(i));
	}
	
	public void conceal() {
		for(Carta c: baraja) {
			c.setId(ID_CARTA_DORSO);
		}
	}
	
	public void addCarta(Carta c) {
		baraja.add(c);
	}
	
	private static String numero(byte n) {
		n = (byte) (n % CARDS_PER_SUIT);
		switch(n) {
		case 0: return "AS";
		case 9: return "SOTA";
		case 10: return "CABALLO";
		case 11: return "REY";
		default: return Integer.toString(n+1);
		}
	}
	
	private static String palo(byte p) {
		switch(p/CARDS_PER_SUIT) {
		case 0: return "OROS";
		case 1: return "COPAS";
		case 2: return "ESPADAS";
		case 3: return "BASTOS";
		default: return "INDEFINIDO";
		}
	}
	
	public static void print(byte carta_id) {
		String numero = numero(carta_id); 
		String palo = palo(carta_id);
		String output=numero + " de " + palo;
		System.out.print(output);
	}
	
	public void print() {
		String output = "";
		for (Carta c: baraja) {
			String numero = numero(c.getId()); 
			String palo = palo(c.getId());
			output+=numero + " de " + palo + ",";
		}
		if (output.length()>1)
			output=output.substring(0, output.length()-2);
		System.out.println(output);
	}
	
	@Override
	public Baraja clone() {
		Baraja copy = new Baraja();
		for (Carta c: this.baraja) {
			copy.addCarta(new Carta(c.getId()));
		}
		return copy;
	}
	
	public int size() {
		return baraja.size();
	}
	
}
