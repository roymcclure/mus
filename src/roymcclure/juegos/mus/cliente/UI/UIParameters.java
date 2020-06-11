package roymcclure.juegos.mus.cliente.UI;

import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.CARDS_PER_HAND;
import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.MAX_CLIENTS;

import java.awt.Point;

import roymcclure.juegos.mus.common.logic.Language;

public class UIParameters {

	// measures of the window
	// TODO: never get this resolution, always a bit less.
	public static final int CANVAS_WIDTH = 800;
	public static final int CANVAS_HEIGHT = 600;	
	
	// measures of cards in the game space
	public static final int ANCHO_CARTA_RENDER = (CANVAS_WIDTH * 47) / (4 * 100);
	public static final int ALTO_CARTA_RENDER = CANVAS_HEIGHT * 25 / 100;
	public static final int X_INICIAL_NORTE_SUR = (int)(CANVAS_WIDTH * 0.2425f);
	public static final int Y_INICIAL_OESTE_ESTE = (int)(CANVAS_HEIGHT * 0.15f);
	
	// measures of mano
	public static final int ANCHO_MANO_RENDER = CANVAS_WIDTH / 20; 
	public static final int ALTO_MANO_RENDER = CANVAS_WIDTH / 20;
	public static final int ANCHO_MANO_FICHERO = 512; 
	public static final int ALTO_MANO_FICHERO = 512;	
	
	// measures of cards on the origin file
	public static final int ANCHO_TOTAL_FICHERO = 2496; 
	public static final int ALTO_TOTAL_FICHERO = 1595;	
	public static final int ANCHO_CARTA_FICHERO = ANCHO_TOTAL_FICHERO / 12; 
	public static final int ALTO_CARTA_FICHERO = ALTO_TOTAL_FICHERO / 5;		
	
	// measures of amarrakos
	public static final int ANCHO_AMARRAKO_RENDER = CANVAS_WIDTH / 20; 
	public static final int ALTO_AMARRAKO_RENDER = CANVAS_HEIGHT / 15;
	public static final int ANCHO_AMARRAKO_FICHERO = 64; 
	public static final int ALTO_AMARRAKO_FICHERO = 64;	

	
	// measures of bocadillo
	public static final int BOCADILLO_ALTO = CANVAS_HEIGHT / 2;
	public static final int BOCADILLO_ANCHO = 2 * CANVAS_WIDTH / 5;

	
	public static boolean mouseIsOverCard = false;
	
	// give me the positioning of button, given the corresponding seat id
	public static Point seatButtonOrigin(byte seat_id) {
		int calculatedX=-1000, calculatedY=-1000;
		switch(seat_id) {
			case 0: // NORTH
				// centered, under the cards plus a 5% of window height
				// x = window.half_width - half_
				calculatedX = UIParameters.CANVAS_WIDTH / 2 - (SeatButtonView.WIDTH / 2);
				calculatedY = UIParameters.CANVAS_HEIGHT / 20;
				break;
			case 1: // EAST
				calculatedX = UIParameters.CANVAS_WIDTH - (SeatButtonView.WIDTH + UIParameters.CANVAS_WIDTH / 20);
				calculatedY = UIParameters.CANVAS_HEIGHT / 2 - (SeatButtonView.HEIGHT / 2);				
				break;
			case 2: // SOUTH
				calculatedX = UIParameters.CANVAS_WIDTH / 2 - (SeatButtonView.WIDTH / 2);
				calculatedY = UIParameters.CANVAS_HEIGHT - (SeatButtonView.HEIGHT + UIParameters.CANVAS_HEIGHT / 20);				
				break;
			case 3: // WEST
				calculatedX = UIParameters.CANVAS_WIDTH / 20;
				calculatedY = UIParameters.CANVAS_HEIGHT / 2 - (SeatButtonView.HEIGHT / 2);				
				break;
		}
		return new Point(calculatedX, calculatedY);
	}

	// for a given seat, gives me the point where i should start writing player name
	public static Point getPlayerNameOrigin(byte seat_id) {
		int calculatedX=-1000, calculatedY=-1000;
		switch(seat_id) {
		case 0: // NORTH
			// centered, under the cards plus a 5% of window height
			// x = window.half_width - half_
			calculatedX = UIParameters.CANVAS_WIDTH / 2 - (SeatButtonView.WIDTH / 2);
			calculatedY = ALTO_CARTA_RENDER + ALTO_AMARRAKO_RENDER + 10;
			break;
		case 1: // EAST
			calculatedX = UIParameters.CANVAS_WIDTH - (ANCHO_CARTA_RENDER + ANCHO_AMARRAKO_RENDER + 100);
			calculatedY = UIParameters.CANVAS_HEIGHT / 2 - (SeatButtonView.HEIGHT / 2);				
			break;
		case 2: // SOUTH
			calculatedX = UIParameters.CANVAS_WIDTH / 2 - (SeatButtonView.WIDTH / 2);
			calculatedY = UIParameters.CANVAS_HEIGHT - (ALTO_CARTA_RENDER + ALTO_AMARRAKO_RENDER + 10);				
			break;
		case 3: // WEST
			calculatedX = ANCHO_CARTA_RENDER + ANCHO_AMARRAKO_RENDER;
			calculatedY = UIParameters.CANVAS_HEIGHT / 2 - (SeatButtonView.HEIGHT / 2);				
			break;
		}
		return new Point(calculatedX, calculatedY);		
	}
	

	public static byte seatRequestWasClicked(int x, int y) {
		for(byte i=0; i<Language.GameDefinitions.MAX_CLIENTS; i++) {
			Point p = seatButtonOrigin(i);
			if (x>=p.x && x<=(p.x + SeatButtonView.WIDTH) && y>=p.y && (y<=p.y + SeatButtonView.HEIGHT)) {
				return i;
			}
		}
		return -1;
	}
	
	// es responsabilidad de cada vista definir su posicion?
	// no, es responsabilidad de los parámetros de la interfaz
	// quién define los parámetros de la interfaz?
	public static Point getCardRenderPosition(int seat_id, int pos_in_mano) {
		Point p = new Point();
		if (seat_id %2 == 0) {
			p.x = X_INICIAL_NORTE_SUR + (pos_in_mano * (ANCHO_CARTA_RENDER + (CANVAS_WIDTH / 100)));
			p.y = seat_id == 0 ? 0 : (int)(CANVAS_HEIGHT * 0.75f);
		} else {
			p.x = seat_id == 3? 0 : CANVAS_WIDTH - ANCHO_CARTA_RENDER; 
//					(- ANCHO_CARTA / 2) + (seat_id==1 ? WIDTH : 0);
			p.y = Y_INICIAL_OESTE_ESTE + (pos_in_mano * (ANCHO_CARTA_RENDER + (CANVAS_WIDTH / 100)));
		}
		return p;
	}

	public static Point getHandPosition(byte pos) {
		Point p;
		switch(pos) {
		case 0:
			p = UIParameters.getCardRenderPosition(pos, CARDS_PER_HAND-1);			
			return new Point(p.x+ANCHO_CARTA_RENDER , (ALTO_CARTA_RENDER /2)  - (ALTO_MANO_RENDER / 2));
		case 2:
			p = UIParameters.getCardRenderPosition(pos, 0);			
			return new Point(p.x - ANCHO_MANO_RENDER, p.y + (ALTO_CARTA_RENDER / 2)-(ALTO_MANO_RENDER / 2));
		case 1:
			p = UIParameters.getCardRenderPosition(pos, CARDS_PER_HAND-1);
			return new Point(p.x + (ANCHO_CARTA_RENDER / 2) - (ANCHO_MANO_RENDER/2), p.y+ALTO_CARTA_RENDER);
		case 3:
			p = UIParameters.getCardRenderPosition(pos, 0);			
			return new Point(p.x + (ANCHO_CARTA_RENDER / 2) - (ANCHO_MANO_RENDER/2) , p.y-ALTO_MANO_RENDER);
			
		}
		// la mano se muestra desde el punto de vista del jugador
		return new Point(-1000,-1000);
	}	
	
	// position ID from the player's perspective
	public static byte relativePosition(byte my_seat_id, byte position) {
		byte nrotations = (byte) (2 - my_seat_id);
		byte calculatedPos = (byte) (position + nrotations);
		if (calculatedPos < 0)
			calculatedPos = (byte) (MAX_CLIENTS + calculatedPos);
		else if (calculatedPos >= MAX_CLIENTS)
			calculatedPos = (byte) (calculatedPos % MAX_CLIENTS);
		return calculatedPos;
	}

	
	/*
	 * 
	 * BUTTONS
	 *
	 *
	 */
	
	
	// used to display several buttons
	public static Point getButtonPosition(int buttonIndex, int totalButtons) {
		int alturaTotal = totalButtons * GenericButtonView.HEIGHT + ((totalButtons - 1)* GenericButtonView.VGAP); 
		int startY = (UIParameters.CANVAS_HEIGHT / 2) - (alturaTotal / 2); 
		int startX = (UIParameters.CANVAS_WIDTH / 2) - (GenericButtonView.WIDTH / 2);
		startY+=buttonIndex * (GenericButtonView.HEIGHT + GenericButtonView.VGAP);
		return new Point(startX, startY);
	}

	public static byte getMenuClickedButton(int nbuttons, int x, int y) {
		// i need to return the index of the clicked button
		for(byte i=0; i < nbuttons; i++) {
			Point p = getButtonPosition(i, nbuttons);
			if (x>=p.x && x<=(p.x + GenericButtonView.WIDTH) && y>=p.y && (y<=p.y + GenericButtonView.HEIGHT)) {
				return i;
			}
		}
		return -1;
	}

	public static int getCardPosUnderMouse(int x, int y) {
		for (int i = 0; i< MAX_CLIENTS;i++) {
			Point pos = getCardRenderPosition(2, i);
			if (y >= pos.y && x>=pos.x && (x<=pos.x + ANCHO_CARTA_RENDER) && (y <= pos.y + ALTO_CARTA_RENDER))
				return i;			
		}
		return -1;
	}

	public static Point getAmarrakoDrawPosition(byte position, byte howMany) {
		int x_mitadPantalla = UIParameters.CANVAS_WIDTH/2;
		int y_mitadPantalla = UIParameters.CANVAS_HEIGHT/2;
		switch(position) {
		case 0: return new Point(x_mitadPantalla-(howMany*ANCHO_AMARRAKO_RENDER/2), ALTO_CARTA_RENDER );
		case 1: return new Point(UIParameters.CANVAS_WIDTH - ANCHO_CARTA_RENDER - ANCHO_AMARRAKO_RENDER, y_mitadPantalla - (howMany*ALTO_AMARRAKO_RENDER/2));
		case 2: return new Point(x_mitadPantalla-(howMany*ANCHO_AMARRAKO_RENDER/2), UIParameters.CANVAS_HEIGHT - (ALTO_CARTA_RENDER + UIParameters.ALTO_AMARRAKO_RENDER));
		case 3: return new Point(ANCHO_CARTA_RENDER, y_mitadPantalla - (howMany*ALTO_AMARRAKO_RENDER/2));
		}
		return null;
	}
	
}
