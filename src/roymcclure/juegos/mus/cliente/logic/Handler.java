package roymcclure.juegos.mus.cliente.logic;


import java.awt.Graphics;
import java.awt.Point;
import java.util.LinkedList;

import roymcclure.juegos.mus.cliente.UI.SeatButtonView;
import roymcclure.juegos.mus.cliente.UI.UIParameters;
import roymcclure.juegos.mus.common.logic.PlayerState;

import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;

public class Handler {

	private static LinkedList<GameObject> objects = new LinkedList<GameObject>();
	
	private static Graphics _graphics;
	
	public Handler() {

	}
	
	public void update() {
		for (int i=0; i<objects.size(); i++) {
			GameObject tempObject = objects.get(i);
			tempObject.tick();
		}
	}
	
	public void render(Graphics g) {
		_graphics = g;		
		for (int i=0; i<objects.size(); i++) {
			GameObject tempObject = objects.get(i);
			tempObject.render(_graphics);
		}
		
	}
	
	public static void addObject(GameObject go) {
		objects.add(go);
	}
	
	public void removeObject(GameObject go) {
		objects.remove(go);
	}
	
	public void updateView(ClientGameState gs) {
		objects.clear();
		// player names
		updateNamesView(gs);
		// stones
		// games
		// cows
		// buttons
		updateButtonsView();
	}

	private void updateButtonsView() {
		// state-dependent and this client-only
		// if i am not seated, display SEAT button where available
		if (ClientGameState.table().getSeatOf(ClientGameState.getPlayerID()) <0) {
			// draw "Seat" buttons
			for (byte i = 0; i< MAX_CLIENTS; i++) {
				if (ClientGameState.table().isSeatEmpty(i)) {
					addObject(new SeatButtonView(UIParameters.seatButtonOrigin(i).x,UIParameters.seatButtonOrigin(i).y,ID.SeatButton));
				}
			}
		} else {
			
		}

		
	}
	
	private static void updateNamesView(ClientGameState gs) {
		// if i am not seated, draw each name on its place
		if (ClientGameState.table().getSeatOf(ClientGameState.getPlayerID()) <0) {
			for (byte i = 0; i < MAX_CLIENTS; i++) {
				Point p = UIParameters.getPlayerNameOrigin(i);
				TextGameObject to =new TextGameObject(p.x, p.y, ID.Text); 
				to.setText(ClientGameState.table().getClient(i).getName());
				addObject(to);
			}
		} else {
			// calculate the distance from my seat to seat 2 (south)
			int seat_id = ClientGameState.table().getSeatOf(ClientGameState.getPlayerID());
			int distance = Math.abs(seat_id - 2) % MAX_CLIENTS;			
			// else draw me in the south and apply the same displacement to everyone else
			for (byte i = 0; i < MAX_CLIENTS; i++) {
				Point p = UIParameters.getPlayerNameOrigin((byte)((i + distance) % MAX_CLIENTS));
				TextGameObject to =new TextGameObject(p.x, p.y, ID.Text); 
				to.setText(ClientGameState.table().getClient(i).getName());
				addObject(to);
			}
		}
	}

	
}
