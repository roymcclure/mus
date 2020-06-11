package roymcclure.juegos.mus.common.logic.cards;

import java.util.ArrayList;
import java.util.Collections;

import roymcclure.juegos.mus.common.logic.PlayerState;

import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;
import static roymcclure.juegos.mus.common.logic.Language.GamePhase.*;
import static roymcclure.juegos.mus.common.logic.Language.TipoPares;

public class Jugadas {
	
	// IMPORTANTE: jugador1 SIEMPRE es mano sobre jugador2 es postre, esto hay que tenerlo
	// en cuenta a la hora de hacer las llamadas
	public static boolean ganaAGrande(PlayerState mano, PlayerState postre) {
		if (mano.numeroCerdos() > postre.numeroCerdos()) {
			return true;
		} else if (mano.numeroCerdos() < postre.numeroCerdos()) {
			return false;
		} else {
			// empatados a cerdos. carta más alta.
			// creamos copia de ambas manos
			ArrayList<Carta> copiaManoJugador1 = mano.getMano();
			ArrayList<Carta> copiaManoJugador2 = postre.getMano();
			/* superfluo
			// eliminamos referencias a cerdos de ambas manos
			eliminarCerdos(copiaManoJugador1);
			eliminarCerdos(copiaManoJugador2);*/			
			// con lo que resta, vamos comparando la carta mas alta

			
			while (!copiaManoJugador1.isEmpty() && !copiaManoJugador2.isEmpty() && 
				(Jugadas.cartaMasAlta(copiaManoJugador1).valor() == Jugadas.cartaMasAlta(copiaManoJugador2).valor())) {
				copiaManoJugador1.remove(Jugadas.cartaMasAlta(copiaManoJugador1));
				copiaManoJugador2.remove(Jugadas.cartaMasAlta(copiaManoJugador2));				
			}
			
			// ambas manos vacias == tenian misma jugada, por tanto gana la mano
			if ((copiaManoJugador1.size() == 0) && (copiaManoJugador2.size()==0)) {
				return true;
			} else if (cartaMasAlta(copiaManoJugador1).valor() < cartaMasAlta(copiaManoJugador2).valor()) {
				return false;
			} else {
				return true;
			}	
		}
	}
	
	// IMPORTANTE: jugador1 SIEMPRE es mano sobre jugador2 es postre, esto hay que tenerlo
	// en cuenta a la hora de hacer las llamadas
	public static boolean ganaAChica(PlayerState mano, PlayerState postre)  {
		System.out.println("Gana a chica: comparando estas manos.");
		mano.printCartas();
		System.out.println("\ncon esta:");
		postre.printCartas();
		if (mano.numeroPitos() > postre.numeroPitos()) {
			System.out.println("mano tiene más pitos que postre");
			return true;
		} else if (mano.numeroPitos() < postre.numeroPitos()) {
			System.out.println("mano tiene menos pitos que postre");			
			return false;
		} else {
			// empatados a pitos. carta más baja.
			// creamos copia de ambas manos
			ArrayList<Carta> copiaManoJugador1 = mano.getMano();
			System.out.println("En mano hay:");
			System.out.println(copiaManoJugador1);
			ArrayList<Carta> copiaManoJugador2 = postre.getMano();
			/* superfluo
			// eliminamos referencias a pitos de ambas manos
			eliminarPitos(copiaManoJugador1);
			eliminarPitos(copiaManoJugador2);
			*/
			
			// con lo que resta, vamos comparando la carta mas alta

			while (!copiaManoJugador1.isEmpty() && !copiaManoJugador2.isEmpty() 
					&& Jugadas.cartaMasBaja(copiaManoJugador1).valor() == Jugadas.cartaMasBaja(copiaManoJugador2).valor()) {
				copiaManoJugador1.remove(Jugadas.cartaMasBaja(copiaManoJugador1));
				copiaManoJugador2.remove(Jugadas.cartaMasBaja(copiaManoJugador2));				
			}

			// ambas manos vacias == tenian misma jugada, por tanto gana la mano
			// TODO: revisar, esto no bien wei
			if (copiaManoJugador1.size()>0) {
				System.out.println("Valor de la carta más baja de mano es " + cartaMasBaja(copiaManoJugador1).valor());
			}
			if (copiaManoJugador2.size()>0) {
				System.out.println("Valor de la carta más baja de postre es " + cartaMasBaja(copiaManoJugador2).valor());
			}			
			if ((copiaManoJugador1.size() == 0) && (copiaManoJugador2.size()==0)) {
				System.out.println("ambas manos vacias");
				return true;
			} else if (cartaMasBaja(copiaManoJugador1).valor() > cartaMasBaja(copiaManoJugador2).valor()){
				System.out.println("carta más baja de jugador 1 tiene mayor valor que carta mas baja de jugador 2");
				return false;
			} else {
				System.out.println("carta más baja de jugador 2 tiene mayor valor que carta mas baja de jugador 1");				
				return true;
			}	
		}
	}	

	public static boolean ganaAPares(PlayerState mano, PlayerState postre) {
		boolean ret = false;
		// valor duples=4 medias=3 pares=2
		if (mano.valorPares() == TipoPares.DUPLES) {
			if (postre.valorPares() == TipoPares.MEDIAS || postre.valorPares() == TipoPares.PAR) {
				ret= true;
			} else {
				// ambos tienen duples y hay que compararlos
				// si ordenamos las manos de mayor a menor y vamos comparando el primer elemento
				// el primero que sea mayor que el otro deberia perder
				return Jugadas.ordenaYcompara(mano, postre);
			}
		}
		else
			if (mano.valorPares() == TipoPares.MEDIAS) {
				if (postre.valorPares() == TipoPares.PAR) {
					ret= true;
				} else if (postre.valorPares() == TipoPares.DUPLES){
					ret= false;// 
				} else {
					// ambos tienen medias y hay que comparar
					// obtenemos la carta cuyo count es 3 en la mano
					// y las comparamos
					return mano.getCartaByCount(3).compareTo(postre.getCartaByCount(3));

				}
			}
			else
				if (mano.valorPares() == TipoPares.PAR) {
					if (postre.valorPares() == TipoPares.MEDIAS || postre.valorPares() == TipoPares.DUPLES) {
						ret= false;
					} else {
						// ambos tienen pares y hay que compararlos
						// obtenemos la carta cuyo count es 2 en ambas manos
						// y las comparamos
						// nunca puede devolver
						return mano.getCartaByCount(2).compareTo(postre.getCartaByCount(2));
					}
				}    		
		return ret;
	}

	public static boolean ganaJuego(PlayerState mano, PlayerState postre) {
		// TODO: 31 real?
		if (mano.valorJuego() == 31) {
			return true;
		} else if (postre.valorJuego() == 31){
			return false;
		} else if (mano.valorJuego() == 32) {
			return true;    		
		} else if (postre.valorJuego() == 32) {
			return false;
		} else return mano.valorJuego() >= postre.valorJuego();
	}

	/*
	// elimina los cerdos de una mano para hacer cálculos
	private static void eliminarCerdos(ArrayList<Carta> mano) {

		Iterator<Carta> iter = mano.iterator();

		while (iter.hasNext()) {
			Carta c = iter.next();
			byte n = (byte) (c.getId() % CARDS_PER_SUIT);
			if (n==2 || n == 11)
				iter.remove();
		}
	}

	// elimina los pitos de una mano para hacer cálculos
	private static void eliminarPitos(ArrayList<Carta> mano) {

		Iterator<Carta> iter = mano.iterator();

		while (iter.hasNext()) {
			Carta c = iter.next();
			byte n = (byte) (c.getId() % CARDS_PER_SUIT);
			if (n==0 || n == 1)
				iter.remove();
		}
	}
	*/	

	public static Carta	cartaMasAlta(ArrayList<Carta> mano) {
		byte valor_mas_alta = Byte.MIN_VALUE;
		Carta masAlta = null;
		for (Carta c : mano) {
			byte valor = 0;
			if (c.isCerdo()) {
				valor = 11;
			} else if (c.isPito()) {
				valor = 1;
			} else {
				valor = (byte) (c.getId() % CARDS_PER_SUIT);
			}
			if (valor > valor_mas_alta) {
				valor_mas_alta = valor;
				masAlta = c;
			}
		}
		return masAlta;
	}
	
	public static Carta	cartaMasBaja(ArrayList<Carta> mano) {
		byte valor_mas_baja = (byte) Byte.MAX_VALUE;
		Carta masBaja = null;
		for (Carta c : mano) {
			byte valor = 0;
			if (c.isCerdo()) {
				valor = 12;
			} else if (c.isPito()) {
				valor = 1;
			} else {
				valor = (byte) (c.getId() % CARDS_PER_SUIT);
			}
			if (valor < valor_mas_baja) {
				valor_mas_baja = valor;
				masBaja = c;
			}
		}
		return masBaja;
	}	

 

	/* ordena una copia de ambas manos ascendente
     * y devuelve true en el momento en el que la mano1 tenga
     * una carta con un valor más alto en la misma posición que mano2
     * o ambas manos son idénticas. 
     */
    public static boolean ordenaYcompara(PlayerState mano1, PlayerState mano2) {

    	ArrayList<Carta> primero = mano1.getMano();
		ArrayList<Carta> segundo = mano2.getMano();
		System.out.println("antes de ordenar..." + primero.toString());
		
		// realmente pasar aquí la carta la única finalidad es para poder pasar el comparador que implementa
		Collections.sort(primero, mano1.getCarta(0));
		Collections.sort(segundo, mano1.getCarta(0));
		
		// hemos ordenado la mano de menor a mayor y no me quiero complicar
		// implementando un comparador que ordene de mayor a menor
		// por eso comparo desde la última carta
		for (int i= CARDS_PER_HAND-1; i>=0;i--) {
			if (primero.get(i).valor() > segundo.get(i).valor()) {
				return true;
			} else if (primero.get(i).valor() < segundo.get(i).valor()) {
				return false;
			}
			// si ninguna condicion se cumple es que el valor era igual
		}
		return true;

    }

	public static byte valorEnPiedrasMano(byte lance, PlayerState cliente) {
		switch(lance) {
		case PARES:
			return Jugadas.valorEnPiedrasPares(cliente);
		case JUEGO:
			return Jugadas.valorEnPiedrasJuego(cliente);			
		}
		return 0;
	}

	// solo si hay juego
	private static byte valorEnPiedrasJuego(PlayerState cliente) {
		if (cliente.valorJuego()==31)
			return 3;
		else if (cliente.valorJuego()>31) {
			return 2;
		}
		return 0;
	}

	private static byte valorEnPiedrasPares(PlayerState cliente) {
		switch(cliente.valorPares()) {
		case DUPLES: return 3;
		case MEDIAS: return 2;
		case PAR: return 1;
		case NO_PAR: return 0;
		}
		return 0;		
	}
	

	public static boolean ganaAlPunto(PlayerState mano, PlayerState postre) {
		return mano.valorJuego() > postre.valorJuego();
	}

    
}
