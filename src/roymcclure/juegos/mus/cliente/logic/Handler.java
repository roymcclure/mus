package roymcclure.juegos.mus.cliente.logic;


import java.awt.Graphics;
import java.awt.Point;
import java.util.LinkedList;

import roymcclure.juegos.mus.cliente.UI.*;
import roymcclure.juegos.mus.common.logic.cards.Carta;
import roymcclure.juegos.mus.common.network.ClientMessage;

import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;
import static roymcclure.juegos.mus.common.logic.Language.ServerGameState.*;
import static roymcclure.juegos.mus.common.logic.Language.GamePhase.*;

import static roymcclure.juegos.mus.cliente.logic.ClientGameState.*;

public class Handler {

	private static LinkedList<GameObject> objects = new LinkedList<GameObject>();
	private static LinkedList<GameObject> temporaryObjects = new LinkedList<GameObject>();	
	
	private static Graphics _graphics;
	
	public Handler() {}
	
	public void update() {
		synchronized(objects) {
			for (int i=0; i<objects.size(); i++) {
				GameObject tempObject = objects.get(i);
				tempObject.tick();
			}
		}
		synchronized(temporaryObjects) {
			for (int i=0; i<temporaryObjects.size(); i++) {
				GameObject tempObject = temporaryObjects.get(i);
				tempObject.tick();
			}
		}		
	}
	
	public void render(Graphics g) {
		_graphics = g;		
		synchronized(objects) {
			for (int i=0; i<objects.size(); i++) {
				GameObject tempObject = objects.get(i);
				tempObject.render(_graphics);
			}
		}
		synchronized(temporaryObjects) {
			for (int i=0; i<temporaryObjects.size(); i++) {
				GameObject tempObject = temporaryObjects.get(i);
				tempObject.render(_graphics);
				if (tempObject.isMarkedForRemoval()) {
					temporaryObjects.remove(tempObject);
				}
			}

		}

	}

	public static void addObject(GameObject go) {
		synchronized(objects) {
			objects.add(go);
		}
	}
	
	public void removeObject(GameObject go) {
		synchronized(objects) {
			objects.remove(go);
		}
	}
	
	public static void addTemporaryObject(GameObject go) {
		synchronized(objects) {
			temporaryObjects.add(go);
		}
	}
	
	public void removeTemporaryObject(GameObject go) {
		synchronized(objects) {
			temporaryObjects.remove(go);
		}
	}	
	
	// TODO: there is no need to re-create all objects that havent changed	
	public void updateView() {
		//System.out.println("called updateView in handler");
		synchronized(objects) {
			objects.clear();
		}
		// player names
		updateNamesView();
		// cards
		updateCards();
		// mouse over card
		updateMouseOverCard();
		// selected cards
		updateSelectedCards();
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


	private void updateSelectedCards() {
		switch(ClientGameState.table().getTipo_Lance()) {
		case MUS:
			for (int i=0; i<CARDS_PER_HAND;i++)
				ClientGameState.setSelectedCard(i, false);
		case DESCARTE:
			for (int i = 0; i < MAX_CLIENTS; i++) {
				if (ClientGameState.getSelectedCard(i)) {
					Point p = UIParameters.getCardRenderPosition(2, i);
					addObject(new CardFrameView(p.x, p.y, ID.CartaFrameSelected));
				}
			}
			break;
		}		
	}

	private void updateMouseOverCard() {
		switch(ClientGameState.table().getTipo_Lance()) {
		case DESCARTE:
			int pos_in_hand =ClientGameState.getMouseOverCard(); 
			if (pos_in_hand!=-1 && !me().isCommitedToDiscard()) {
				Point p = UIParameters.getCardRenderPosition(2, pos_in_hand);
				addObject(new CardFrameView(p.x, p.y, ID.CartaFrame));
			}		
			break;
		}

	}

	private void updateHandPosition() {
		// get position of who's mano
		if (ClientGameState.getGameState().getServerGameState() != WAITING_ALL_PLAYERS_TO_CONNECT && ClientGameState.getGameState().getServerGameState() != WAITING_ALL_PLAYERS_TO_CONNECT) { 
			byte mano = ClientGameState.table().getMano_seat_id();
			byte finalPosition = UIParameters.positionFromPlayerPerspective(my_seat_id(), mano);
			Point p = UIParameters.getHandPosition(finalPosition);
			HandView hand = new HandView(p.x, p.y, ID.Mano);
			addObject(hand);		
		}
	}

	private void updateCards() {
		switch(ClientGameState.getGameState().getServerGameState()) {
		case PLAYING:
		case DEALING:
		case END_OF_ROUND:
		{
			for (byte i = 0; i<MAX_CLIENTS;i++) {
				Carta[] cartas = ClientGameState.table().getClient(i).getCartas(); 
				for (byte j = 0; j<CARDS_PER_HAND;j++) {
					Point p = UIParameters.getCardRenderPosition(UIParameters.positionFromPlayerPerspective(my_seat_id(), i), j);
					// System.out.println("Para cliente " + i + " añado carta con id " + cartas[j].getId() + " en posicion " + p.x + "," + p.y);
					addCartaView(p, cartas[j].getId());
				}
			}
		}
		break;
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
		//System.out.println("en updateButtonsView");
		switch(ClientGameState.getGameState().getServerGameState()) {
		case WAITING_ALL_PLAYERS_TO_CONNECT:
		case WAITING_ALL_PLAYERS_TO_SEAT:
			updateButtonsViewAwaitingAllSeated();
			break;
		case PLAYING:
			updateButtonsViewPlaying();
			break;
		case DEALING:
			updateButtonsViewDealing();
			break;
		
		}
		
		
	}
	
	private void updateButtonsViewDealing() {

		switch(ClientGameState.table().getTipo_Lance()) {
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
			if (!me().isCommitedToDiscard()) {
				addTextGameObject(new Point(UIParameters.WIDTH/2,  (UIParameters.HEIGHT/2)-100),"Elige qué cartas quieres descartar.");
				String[] labels_descarte = {"ESTOY SERVIDO"};
				addButtons(labels_descarte);	
			} else {
				addTextGameObject(new Point(UIParameters.WIDTH/2,  UIParameters.HEIGHT/2),"Esperando a que el resto se descarte...");
			}
						
			break;
			
		}
		
		
	}

	private void updateButtonsViewPlaying() {

		// when playing, i only show buttons to the player in turn
		updateButtonsViewForPhase(ClientGameState.table().getTipo_Lance());
	}

	// this are the possible actions for the player in turn
	private void updateButtonsViewForPhase(byte tipo_ronda) {

		switch(tipo_ronda) {
		case CHICA:
		case PARES:
		case JUEGO:
		case GRANDE:
			if (ClientGameState.table().getJugador_debe_hablar()==my_seat_id()) {
				if (table().getPiedras_envidadas_ronda_actual()==0) {
					String[] labels_grande = {"ENVIDAR 2", "ENVIDAR 5", "ORDAGO", "PASO"};
					addButtons(labels_grande);	
				}
				else if (table().isOrdago_lanzado()){
					// hay un ordago
					String[] labels_grande = {"ACEPTAR ORDAGO!", "SOY UN CAGAO"};
					addButtons(labels_grande);					
				} else {
					// hay un envite normal
					String[] labels_grande = {"ENVIDAR 2 MAS", "ENVIDAR 5 MAS", "ORDAGO", "PASO", "ACEPTAR"};
					addButtons(labels_grande);						
				}
			} else {
				addTextGameObject(new Point(UIParameters.WIDTH/2,  UIParameters.HEIGHT/2),"Esperando a que hablen los demás...");
			}
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
			for (byte i = 0; i < MAX_CLIENTS; i++) {
				byte pos = UIParameters.positionFromPlayerPerspective(my_seat_id(), i);
				Point p = UIParameters.getPlayerNameOrigin(pos);
				addTextGameObject(p,ClientGameState.table().getClient(i).getName());
			}
		}
	}

	public void broadcastMsgToView(ClientMessage broadCastMessage) {
		int x = UIParameters.WIDTH / 2 - UIParameters.BOCADILLO_ANCHO / 2;
		int y = UIParameters.HEIGHT / 2 - UIParameters.BOCADILLO_ALTO / 2;
		byte absolute_seat_id = ClientGameState.table().getSeatOf(broadCastMessage.getInfo());
		byte relative_seat_id = UIParameters.positionFromPlayerPerspective(ClientGameState.my_seat_id(), absolute_seat_id);
		BocadilloView bv = new BocadilloView(x,y,ID.Bocadillo, 2000,relative_seat_id);
		addTemporaryObject(bv);
		System.out.println("ADDED BOCADILLO VIEW IN " + x+ ","+y);
		
	}

	
}
