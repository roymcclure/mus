package roymcclure.juegos.mus.cliente.logic;


import java.awt.Graphics;

import java.awt.Point;
import java.util.LinkedList;

import roymcclure.juegos.mus.cliente.UI.*;
import roymcclure.juegos.mus.common.logic.Language.GamePhase;
import roymcclure.juegos.mus.common.logic.Language.PlayerActions;
import roymcclure.juegos.mus.common.logic.ByteMessage;
import roymcclure.juegos.mus.common.logic.cards.Carta;
import roymcclure.juegos.mus.common.network.ClientMessage;


import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;
import static roymcclure.juegos.mus.common.logic.Language.PlayerActions.*;
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
			//for (int i=0; i<temporaryObjects.size(); i++) {
			if (temporaryObjects.size() > 0) {
				GameObject tempObject = temporaryObjects.get(0);
				tempObject.tick();
			}
			//}
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
			//for (int i=0; i<temporaryObjects.size(); i++) {
			if (temporaryObjects.size()>0) {
				GameObject tempObject = temporaryObjects.get(0);
				tempObject.render(_graphics);
				if (tempObject.isMarkedForRemoval()) {
					temporaryObjects.remove(tempObject);
				}

			}
			//}

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


	// UI update upon Server Message reception.
	// TODO: there is no need to re-create all objects that havent changed	
	public void updateView() {
		System.out.println(System.nanoTime() + " calling UpdateView");
		if (ClientGameState.table()!=null && ClientGameState.getGameState() != null) {
			//System.out.println("called updateView in handler");
			synchronized(objects) {
				objects.clear();
			}
			//System.out.println("UPDATE view called: state is " + ClientGameState.getGameState().getServerGameState());
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
			updateStones();
			// games
			// cows
			// buttons
			updateButtonsView();
		}
	}


	// el tanteo de 5 lo llevan norte y oeste
	// amarrakosTotales / 5
	// el tanteo de 1 lo llevan sur y este. por pobres!
	// amarrakosTotales % 5
	private void updateStones() {
		// draw the bet

		// draw how the ones players have
		for (byte i = 0; i < MAX_CLIENTS; i++) {
			byte relative_position = UIParameters.relativePosition(my_seat_id(), i);
			int howMany = table().piedrasEnJugador(i);			
			Point p = UIParameters.getAmarrakoDrawPosition(relative_position, (byte) howMany);
			ID id;
			int x, y;
			if (i == 0 || i == 3)
				id = ID.Amarrako5;
			else
				id = ID.Amarrako;				
			if (relative_position%2==0) {
				for (int j = 0; j<howMany;j++) {
					// dibujar en horizontal para norte y sur
					x = p.x + (j * UIParameters.ANCHO_AMARRAKO_RENDER);
					y = p.y;
					addObject(new AmarrakoView(x, y, id));					
				}			

			} else {
				for (int j = 0; j<howMany;j++) {
					// dibujar en vertical para oeste y este
					x = p.x;
					y = p.y + (j * UIParameters.ALTO_AMARRAKO_RENDER);				
					addObject(new AmarrakoView(x, y, id));
				}
			}
		}		
	}

	private void updateSelectedCards() {
		switch(ClientGameState.table().getGamePhase()) {
		case GamePhase.MUS:
			for (int i=0; i<CARDS_PER_HAND;i++)
				ClientGameState.setSelectedCard(i, false);
		case DESCARTE:
			for (int i = 0; i < MAX_CLIENTS; i++) {
				if (ClientGameState.getSelectedCard(i)) {
					Point p = UIParameters.getCardRenderPosition(2, i);
					addObject(new CardFrameView(p.x, p.y, ID.CartaFrameSelected));				}
			}
			break;
		}		
	}

	private void updateMouseOverCard() {
		switch(ClientGameState.table().getGamePhase()) {
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
		if (ClientGameState.getGameState().getServerGameState() != WAITING_ALL_PLAYERS_TO_CONNECT && ClientGameState.getGameState().getServerGameState() != WAITING_ALL_PLAYERS_TO_SEAT) { 
			byte mano = ClientGameState.table().getMano_seat_id();
			byte finalPosition = UIParameters.relativePosition(my_seat_id(), mano);
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
					Point p = UIParameters.getCardRenderPosition(UIParameters.relativePosition(my_seat_id(), i), j);
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
		case END_OF_ROUND:
			updateButtonsViewEndOfRound();
			break;

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

	private void updateButtonsViewPlaying() {

		// when playing, i only show buttons to the player in turn
		updateButtonsViewForPhase(ClientGameState.table().getGamePhase());
	}

	private void updateButtonsViewDealing() {

		switch(ClientGameState.table().getGamePhase()) {
		case GamePhase.MUS:// la gente se está dando mus
			if (ClientGameState.table().getJugador_debe_hablar()==my_seat_id()) {
				// its either mus, or cut.
				String[] labels_mus = {"MUS","CORTO MUS"};
				addButtons(labels_mus);				
			}	else {
				// show "waiting for [player in turn] to play"...
				addTextGameObject(new Point(UIParameters.CANVAS_WIDTH/2,  UIParameters.CANVAS_HEIGHT/2),"Esperando que hable otro jugador...");
			}	
			break;
		case GamePhase.DESCARTE:
			if (!me().isCommitedToDiscard()) {
				addTextGameObject(new Point(UIParameters.CANVAS_WIDTH/2,  (UIParameters.CANVAS_HEIGHT/2)-100),"Elige qué cartas quieres descartar.");
				String[] labels_descarte = {"DESCARTAR"};
				addButtons(labels_descarte);	
			} else {
				addTextGameObject(new Point(UIParameters.CANVAS_WIDTH/2,  UIParameters.CANVAS_HEIGHT/2),"Esperando a que el resto se descarte...");
			}

			break;

		}


	}


	private void updateButtonsViewEndOfRound() {

		// if partida terminada, show button JUGAR OTRA PARTIDA.
		
		if (getGameState().isReadyForNextRound(my_seat_id())) {
			String[] labels_ronda = {"REPETIR CONTEO"};
			addTextGameObject(new Point(UIParameters.CANVAS_WIDTH/2,  UIParameters.CANVAS_HEIGHT/2), "Esperando al resto de jugadores...");
			addButtons(labels_ronda);
		} else {
			String[] labels_ronda = {"SIGUIENTE RONDA","REPETIR CONTEO"};
			addButtons(labels_ronda);			
		}
	}	



	// this are the possible actions for the player in turn
	private void updateButtonsViewForPhase(byte tipo_ronda) {
		boolean show = true;
		switch(tipo_ronda) {
		case GRANDE:
		case CHICA:
		case PARES:
		case JUEGO:			
			//System.out.println("updating buttons. tipo ronda:" + Language.StringLiterals.LANCES[tipo_ronda]);
			if (tipo_ronda == PARES) {
				if (!table().tienePares(my_seat_id())) {
					System.out.println("poniendo show a false en pares");
					show = false;
				}
			}		
			else if (tipo_ronda == JUEGO) {				
				if (table().seJuegaJuego()) {
					if (!table().seJuegaAlPunto() && !table().tieneJuego(my_seat_id())) {
						System.out.println("poniendo show a false en juego");
						show = false;
					}
				} 
			}		
			if (ClientGameState.table().getJugador_debe_hablar()==my_seat_id() && show) {
				if (table().isOrdago_lanzado()){
					// hay un ordago
					String[] labels_grande = {"ACEPTAR ORDAGO!", "SOY UN CAGAO"};
					addButtons(labels_grande);					
				} else if (table().getPiedras_envidadas_ronda_actual()==0) {
					String[] labels_grande = {"ENVIDAR 2", "ENVIDAR 5", "ORDAGO", "PASO"};
					addButtons(labels_grande);	
				} else {
					// hay un envite normal
					String[] labels_grande = {"ENVIDAR 2 MAS", "ENVIDAR 5 MAS", "ORDAGO", "PASO", "ACEPTAR"};
					addButtons(labels_grande);						
				}
			} else {
				byte cid = ClientGameState.table().getJugador_debe_hablar();
				String text = "Esperando a que hable " + ClientGameState.table().getClient(cid).getName();
				int text_width = _graphics.getFontMetrics().stringWidth(text);				
				addTextGameObject(new Point(UIParameters.CANVAS_WIDTH/2 - (text_width / 2),  UIParameters.CANVAS_HEIGHT/2),text);
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
				byte pos = UIParameters.relativePosition(my_seat_id(), i);
				Point p = UIParameters.getPlayerNameOrigin(pos);
				addTextGameObject(p,ClientGameState.table().getClient(i).getName());
			}
		}
	}

	// en qty viene el sitio
	public void broadcastMsgToView(ClientMessage broadCastMessage) {
		// i guess it's not great to use the info field for this. i guess it's not terrible either.
		String sender_playerID = broadCastMessage.getInfo();
		byte sender_absolute_seat_id = ClientGameState.table().getSeatOf(sender_playerID);
		byte sender_relative_seat_id = UIParameters.relativePosition(my_seat_id(), sender_absolute_seat_id);
		String msg = "";
		String[] playerActionsText = {"PASO","ENVIDO","ACEPTO","ORDAGO","ME DOY MUS","UNA MIERDA MUS!",	"ME TIRO DE ",
				"", "", "", "", "", "", "TENGO PARES","TENGO JUEGO","","","","","","CIERRO CONEXION","DESCARTA AL MENOS UNA"
		};
		msg = playerActionsText[broadCastMessage.getAction()];
		if (broadCastMessage.getAction()==PlayerActions.HABLO_PARES ) {
			if (!ClientGameState.table().tienePares(sender_absolute_seat_id)) {
				msg="NO " + msg;			
			}
		}
		if (broadCastMessage.getAction()==PlayerActions.HABLO_JUEGO ) {
			if (!ClientGameState.table().tieneJuego(sender_absolute_seat_id)) {
				msg="NO " + msg;
			}
		}			
		// TODO: en descarte, lo que llega es qué cartas en forma de máscara de bits, no la cantidad
		// eso habrá que deducirlo con una operación sobre bits (bitSet)
		if (broadCastMessage.getAction()==ENVITE)
			msg+=broadCastMessage.getQuantity();
		else if (broadCastMessage.getAction() == DESCARTAR) {
			byte descartes = 0;
			for (byte i = 0; i < CARDS_PER_HAND; i++) {
				if (ByteMessage.isBitSet(i,broadCastMessage.getQuantity())) {					
					descartes++;
				}				
			}
			msg+=descartes;
		} else 	if (broadCastMessage.getAction()==ENVITE && ClientGameState.table().getPiedras_acumuladas_en_apuesta() > 0)
			msg+=" MAS!";

		addSpeechBubble(sender_relative_seat_id, msg, DEFAULT_SPEECHBUBBLE_LIFETIME);

	}
	
	public void addSpeechBubble(byte seat_position, String msg, int duracion) {
		int x = UIParameters.CANVAS_WIDTH / 2 - UIParameters.BOCADILLO_ANCHO / 2;
		int y = UIParameters.CANVAS_HEIGHT / 2 - UIParameters.BOCADILLO_ALTO / 2;
		BocadilloView bv = new BocadilloView(x,y,ID.Bocadillo, 500,seat_position,msg);
		addTemporaryObject(bv);
	}


}
