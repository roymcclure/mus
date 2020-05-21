package roymcclure.juegos.mus.common.logic.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import roymcclure.juegos.mus.common.logic.PlayerState;

import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;
import static roymcclure.juegos.mus.common.logic.Language.TipoPares;

public class Jugadas {
	
	// IMPORTANTE: jugador1 SIEMPRE es mano sobre jugador2 es postre, esto hay que tenerlo
	// en cuenta a la hora de hacer las llamadas
	public boolean ganaAGrande(PlayerState mano, PlayerState postre) throws Exception {
		if (mano.numeroCerdos() > postre.numeroCerdos()) {
			return true;
		} else if (mano.numeroCerdos() < postre.numeroCerdos()) {
			return false;
		} else {
			// empatados a cerdos. carta más alta.
			// creamos copia de ambas manos
			ArrayList<Carta> tempManoJugador1 = mano.getMano();
			ArrayList<Carta> tempManoJugador2 = postre.getMano();
			// eliminamos referencias a cerdos de ambas manos
			this.eliminarCerdos(tempManoJugador1);
			this.eliminarCerdos(tempManoJugador2);			
			// con lo que resta, vamos comparando la carta mas alta

			
			while (!tempManoJugador1.isEmpty() && !tempManoJugador2.isEmpty() && 
				(Jugadas.cartaMasAlta(tempManoJugador1).valor() == 
						Jugadas.cartaMasAlta(tempManoJugador2).valor())) {
				tempManoJugador1.remove(Jugadas.cartaMasAlta(tempManoJugador1));
				tempManoJugador2.remove(Jugadas.cartaMasAlta(tempManoJugador2));				
			}
			
			// ambas manos vacias == tenian misma jugada, por tanto gana la mano
			if ((tempManoJugador1.size() == 0) && (tempManoJugador2.size()==0)) {
				return true;
			} else if (cartaMasAlta(tempManoJugador1).valor() < cartaMasAlta(tempManoJugador2).valor()) {
				return false;
			} else {
				return true;
			}	
		}
	}
	
	// IMPORTANTE: jugador1 SIEMPRE es mano sobre jugador2 es postre, esto hay que tenerlo
	// en cuenta a la hora de hacer las llamadas
	public boolean ganaAChica(PlayerState mano, PlayerState postre) throws Exception {
		if (mano.numeroPitos() > postre.numeroPitos()) {
			return true;
		} else if (mano.numeroPitos() < postre.numeroPitos()) {
			return false;
		} else {
			// empatados a pitos. carta más baja.
			// creamos copia de ambas manos
			ArrayList<Carta> tempManoJugador1 = mano.getMano();
			ArrayList<Carta> tempManoJugador2 = postre.getMano();
			// eliminamos referencias a pitos de ambas manos
			this.eliminarPitos(tempManoJugador1);
			this.eliminarPitos(tempManoJugador2);			
			// con lo que resta, vamos comparando la carta mas alta

			while (!tempManoJugador1.isEmpty() && !tempManoJugador2.isEmpty() && 
				Jugadas.cartaMasBaja(tempManoJugador1).valor() == 
						Jugadas.cartaMasBaja(tempManoJugador2).valor()) {
				tempManoJugador1.remove(Jugadas.cartaMasBaja(tempManoJugador1));
				tempManoJugador2.remove(Jugadas.cartaMasBaja(tempManoJugador2));				
			}

			// ambas manos vacias == tenian misma jugada, por tanto gana la mano
			if ((tempManoJugador1.size() == 0) && (tempManoJugador2.size()==0)) {
				System.out.println("ambas manos vacias");
				return true;
			} else if (cartaMasBaja(tempManoJugador1).valor() > cartaMasBaja(tempManoJugador2).valor()){
				System.out.println("carta más baja de jugador 1 tiene mayor valor que carta mas baja de jugador 2");
				return false;
			} else {
				System.out.println("carta más baja de jugador 2 tiene mayor valor que carta mas baja de jugador 1");				
				return true;
			}	
		}
	}	

	public boolean ganaAPares(PlayerState mano, PlayerState postre) throws Exception {
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

	public boolean ganaJuego(PlayerState mano, PlayerState postre) throws Exception {
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

	// elimina los cerdos de una mano para hacer cálculos
	private void eliminarCerdos(ArrayList<Carta> mano) {

		Iterator<Carta> iter = mano.iterator();

		while (iter.hasNext()) {
			Carta c = iter.next();
			byte n = (byte) (c.getId() % CARDS_PER_SUIT);
			if (n==2 || n == 3)
				iter.remove();
		}
	}

	// elimina los pitos de una mano para hacer cálculos
	private void eliminarPitos(ArrayList<Carta> mano) {

		Iterator<Carta> iter = mano.iterator();

		while (iter.hasNext()) {
			Carta c = iter.next();
			byte n = (byte) (c.getId() % CARDS_PER_SUIT);
			if (n==0 || n == 1)
				iter.remove();
		}
	}	


	public static Carta	cartaMasAlta(ArrayList<Carta> mano) {
		int valor_mas_alto = -1;
		Carta masAlta = null;
		for (Carta c : mano) {
			byte valor = (byte) (c.getId() % CARDS_PER_SUIT);
			if (valor > valor_mas_alto) {
				masAlta = c;
				valor_mas_alto = valor;
			}
		}
		return masAlta;
	}

	public static Carta	cartaMasBaja(ArrayList<Carta> mano) {
		int valor_mas_bajo = CARDS_PER_SUIT;
		Carta masBaja = null;
		for (Carta c : mano) {
			byte valor = (byte) (c.getId() % CARDS_PER_SUIT);
			if (valor < valor_mas_bajo) {
				masBaja = c;
				valor_mas_bajo = valor;
			}
		}
		return masBaja;
	}    

	/* ordena una copia de ambas manos ascendente
     * y devuelve true en el momento en el que la mano1 tenga
     * una carta con un valor más alto en la misma posición que mano2
     * o ambas manos son idénticas. 
     */
    public static boolean ordenaYcompara(PlayerState mano1, PlayerState mano2) throws Exception {

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

    
}
