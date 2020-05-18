package roymcclure.juegos.mus.cliente.logic;


import java.awt.Graphics;
import java.awt.Point;
import java.util.LinkedList;

import roymcclure.juegos.mus.cliente.UI.CartaView;
import roymcclure.juegos.mus.cliente.UI.GenericButtonView;
import roymcclure.juegos.mus.cliente.UI.SeatButtonView;
import roymcclure.juegos.mus.cliente.UI.UIParameters;
import roymcclure.juegos.mus.common.logic.cards.Carta;

import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;
import static roymcclure.juegos.mus.common.logic.Language.ServerGameState.*;
import static roymcclure.juegos.mus.common.logic.Language.GamePhase.*;

public class Handler {

	private static LinkedList<GameObject> objects = new LinkedList<GameObject>();
	
	private static Graphics _graphics;
	
	public Handler() {}
	
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
	
	public void updateView() {
		objects.clear();
		// player names
		updateNamesView();
		// cards
		// todo: there's a "gameState" mess in the namespace. needs refactor.
		updateCards();
		// deck of discarded cards
		// deck of remaining cards
		// show who's hand
		updateHandPosition();
		// stones
		// games
		// cows
		// buttons
		updateButtonsView();
	}

	private void updateHandPosition() {
		// get position of who's mano
		byte habla = ClientGameState.table().getJugador_debe_hablar();
		byte my_seat_id = ClientGameState.table().getSeatOf(ClientGameState.getPlayerID());		
		byte finalPosition = UIParameters.positionFromPlayerPerspective(my_seat_id, habla);
		Point p = UIParameters.getHandPosition(finalPosition);
		HandView hand = new HandView(p.x, p.y, ID.Mano);
		addObject(hand);		
	}

	private static void updateCards() {
		if (ClientGameState.getGameState().getGameState()==PLAYING || ClientGameState.getGameState().getGameState()==DEALING || ClientGameState.getGameState().getGameState()==END_OF_ROUND) {
			byte my_seat_id = ClientGameState.table().getSeatOf(ClientGameState.getPlayerID());
			// read all cards in state
			for (byte i = 0; i<MAX_CLIENTS;i++) {
				Carta[] cartas = ClientGameState.table().getClient(i).getCartas(); 
				for (byte j = 0; j<CARDS_PER_HAND;j++) {
					Point p = UIParameters.getCardRenderPosition(UIParameters.positionFromPlayerPerspective(my_seat_id, i), j);
					// System.out.println("Para cliente " + i + " añado carta con id " + cartas[j].getId() + " en posicion " + p.x + "," + p.y);
					addCartaView(p, cartas[j].getId());
				}
			}
		}	
	}
	
	private static void addCartaView(Point p, byte carta_id) {
		CartaView cv =new CartaView(p.x, p.y, ID.Carta);
		cv.setCarta_id(carta_id);
		addObject(cv);		
	}

	private void updateButtonsView() {
		// state-dependent and this client-only
		// if i am not seated, display SEAT button where available
		switch(ClientGameState.getGameState().getGameState()) {
		case WAITING_ALL_PLAYERS_TO_CONNECT:
		case WAITING_ALL_PLAYERS_TO_SEAT:
			updateButtonsViewAwaitingAllSeated();
			break;
		case PLAYING:
			updateButtonsViewPlaying();
			break;
		case DEALING:
			updateButtonsViewDealing(ClientGameState.table().getTipo_Lance());
			break;
		
		}
		
		
	}
	
	private byte my_seat_id() {
		return ClientGameState.table().getSeatOf(ClientGameState.getPlayerID());
	}
	
	private void updateButtonsViewDealing(byte tipo_lance) {
	
		switch(tipo_lance) {
		case MUS:// la gente se está dando mus
			if (ClientGameState.table().getJugador_debe_hablar()==my_seat_id()) {
				// its either mus, or cut.
				String[] labels_mus = {"MUS","CORTO MUS"};
				addButtons(labels_mus);				
			}	else {
				// show "waiting for [player in turn] to play"...
				addTextGameObject(new Point(UIParameters.WIDTH/2,  UIParameters.HEIGHT/2),"Waiting for another player to talk...");
			}	
			break;
		case DESCARTE:
			
			addTextGameObject(new Point(UIParameters.WIDTH/2,  UIParameters.HEIGHT/2),"Choose the cards you want to discard.");
			String[] labels_descarte = {"DESCARTAR"};
			addButtons(labels_descarte);			
			break;
			
		}
		
		
	}

	private void updateButtonsViewPlaying() {
		// when playing, i only show buttons to the player in turn
		if (ClientGameState.table().getJugador_debe_hablar()==my_seat_id()) {
			updateButtonsViewForPhase(ClientGameState.table().getId_ronda());
		} 
		
	}

	// this are the possible actions for the player in turn
	private void updateButtonsViewForPhase(byte tipo_ronda) {
		switch(tipo_ronda) {
		case GRANDE:
			break;
		case CHICA:
			break;
		case PARES:
			break;
		case JUEGO:
			break;
		}
		
	}

	private void addButtons(String[] labels) {
		for (int i = 0; i<labels.length;i++) {
			Point p = UIParameters.getButtonPosition(i, labels.length);
			addObject(new GenericButtonView(p.x, p.y,labels[i],ID.Button));			
		}
	}
	
	private void updateButtonsViewAwaitingAllSeated() {
		if (my_seat_id() <0) {
			// draw "Seat" buttons
			for (byte i = 0; i< MAX_CLIENTS; i++) {
				if (ClientGameState.table().isSeatEmpty(i)) {
					addObject(new SeatButtonView(UIParameters.seatButtonOrigin(i).x,UIParameters.seatButtonOrigin(i).y,ID.Button));
				}
			}
		}
	}
	
	private static void addTextGameObject(Point p, String text) {
		TextGameObject to =new TextGameObject(p.x, p.y, ID.Text); 
		to.setText(text);
		addObject(to);
	}
	

	
	// names are displayed on the general perspective if player is not seated
	// if player is seated, he is always place on the south seat
	private void updateNamesView() {
		// if i am not seated, draw names of whoever are seated in their actual position
		if (my_seat_id() <0) {
			for (byte i = 0; i < MAX_CLIENTS; i++) {
				Point p = UIParameters.getPlayerNameOrigin(i);
				addTextGameObject(p, ClientGameState.table().getClient(i).getName());
			}
		} else {
			byte my_seat_id = ClientGameState.table().getSeatOf(ClientGameState.getPlayerID());
			for (byte i = 0; i < MAX_CLIENTS; i++) {
				byte pos = UIParameters.positionFromPlayerPerspective(my_seat_id, i);
				Point p = UIParameters.getPlayerNameOrigin(pos);
				addTextGameObject(p,ClientGameState.table().getClient(i).getName());
			}
		}
	}

	
}
