package roymcclure.juegos.mus.cliente.UI;

import java.awt.Point;

import roymcclure.juegos.mus.common.logic.Language;

public class UIParameters {

	
	// give me the positioning of button, given the corresponding seat id
	public static Point seatButtonOrigin(byte seat_id) {
		int calculatedX=-1000, calculatedY=-1000;
		switch(seat_id) {
			case 0: // NORTH
				// centered, under the cards plus a 5% of window height
				// x = window.half_width - half_
				calculatedX = ClientWindow.WIDTH / 2 - (SeatButtonView.WIDTH / 2);
				calculatedY = ClientWindow.HEIGHT / 20;
				break;
			case 1: // EAST
				calculatedX = ClientWindow.WIDTH - (SeatButtonView.WIDTH + ClientWindow.WIDTH / 20);
				calculatedY = ClientWindow.HEIGHT / 2 - (SeatButtonView.HEIGHT / 2);				
				break;
			case 2: // SOUTH
				calculatedX = ClientWindow.WIDTH / 2 - (SeatButtonView.WIDTH / 2);
				calculatedY = ClientWindow.HEIGHT - (SeatButtonView.HEIGHT + ClientWindow.HEIGHT / 20);				
				break;
			case 3: // WEST
				calculatedX = ClientWindow.WIDTH / 20;
				calculatedY = ClientWindow.HEIGHT / 2 - (SeatButtonView.HEIGHT / 2);				
				break;
		}
		return new Point(calculatedX, calculatedY);
	}

	public static Point getPlayerNameOrigin(byte seat_id) {
		int calculatedX=-1000, calculatedY=-1000;
		switch(seat_id) {
		case 0: // NORTH
			// centered, under the cards plus a 5% of window height
			// x = window.half_width - half_
			calculatedX = ClientWindow.WIDTH / 2 - (SeatButtonView.WIDTH / 2);
			calculatedY = ClientWindow.HEIGHT / 18;
			break;
		case 1: // EAST
			calculatedX = ClientWindow.WIDTH - (SeatButtonView.WIDTH + ClientWindow.WIDTH / 18);
			calculatedY = ClientWindow.HEIGHT / 2 - (SeatButtonView.HEIGHT / 2);				
			break;
		case 2: // SOUTH
			calculatedX = ClientWindow.WIDTH / 2 - (SeatButtonView.WIDTH / 2);
			calculatedY = ClientWindow.HEIGHT - (SeatButtonView.HEIGHT + ClientWindow.HEIGHT / 18);				
			break;
		case 3: // WEST
			calculatedX = ClientWindow.WIDTH / 18;
			calculatedY = ClientWindow.HEIGHT / 2 - (SeatButtonView.HEIGHT / 2);				
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
	
}
