package roymcclure.juegos.mus.cliente.logic;

import java.awt.Graphics;
import java.awt.Point;
import java.util.LinkedList;

import roymcclure.juegos.mus.cliente.UI.SeatButtonView;
import roymcclure.juegos.mus.cliente.UI.UIParameters;
import roymcclure.juegos.mus.common.logic.Language;

public class Handler {

	LinkedList<GameObject> objects = new LinkedList<GameObject>();
	
	private Graphics g;
	
	public Handler() {

	}
	
	public void update() {
		for (int i=0; i<objects.size(); i++) {
			GameObject tempObject = objects.get(i);
			tempObject.tick();
		}
	}
	
	public void render(Graphics g) {
		this.g = g;
		for (int i=0; i<objects.size(); i++) {
			GameObject tempObject = objects.get(i);
			tempObject.render(g);
		}	

	}
	
	public void addObject(GameObject go) {
		this.objects.add(go);
	}
	
	public void removeObject(GameObject go) {
		this.objects.remove(go);
	}
	
	public void updateView(ClientGameState gs) {
		this.objects.clear();
		// seats
		updateSeatsView(gs);
		// cards		
		
		// stones
		
		
		// on
		
		// player names
		/*for (byte i = 0; i < Language.GameDefinitions.MAX_CLIENTS; i++) {
			g.drawString(gs.getPlayerIDbySeatID(i), UIParameters.getPlayerNameX(i), UIParameters.getPlayerNameY(i));
		}*/
	}

	private void updateSeatsView(ClientGameState gs) {
		
		for (byte i = 0; i < Language.GameDefinitions.MAX_CLIENTS; i++) { 
			System.out.println("");
			if (gs.getPlayerIDbySeatID(i).equals("empty")) {
				// draw seat button. this presumes that the client can seat.
				Point p = UIParameters.seatButtonOrigin(i);
				this.addObject(new SeatButtonView(p.x, p.y, ID.SeatButton));				
			} 
		}
		
	}

	
}
