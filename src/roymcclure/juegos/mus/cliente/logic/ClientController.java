package roymcclure.juegos.mus.cliente.logic;

import roymcclure.juegos.mus.cliente.UI.UIParameters;
import roymcclure.juegos.mus.cliente.logic.jobs.*;

import static roymcclure.juegos.mus.common.logic.Language.PlayerActions.*;
import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;
import static roymcclure.juegos.mus.common.logic.Language.ButtonIndices.*;
import static roymcclure.juegos.mus.common.logic.Language.MouseInputType.*;
import static roymcclure.juegos.mus.common.logic.Language.GamePhase.*;

import roymcclure.juegos.mus.common.logic.Language;
import roymcclure.juegos.mus.common.logic.Language.GamePhase;
import roymcclure.juegos.mus.common.logic.Language.PlayerActions;
import roymcclure.juegos.mus.common.logic.TableState;
import roymcclure.juegos.mus.common.logic.cards.Jugadas;
import roymcclure.juegos.mus.common.logic.jobs.*;
import roymcclure.juegos.mus.common.network.*;

import static roymcclure.juegos.mus.cliente.logic.ClientGameState.*;

/*
 * La responsabilidad de esta clase es gestionar los
 * cambios en el modelo. Toda solicitud de cambio en el modelo 
 * pasa por esta clase.
 * El input desde el listener y la información recibida desde ControllerJobsQueue
 * son enviados a colas de trabajo, que son leídas por el controller
 * que a continuación modificará posiblemente el modelo.
 * A su vez el controlador genera las solicitudes al servidor.
 * Los cambios en el modelo modifican la vista.
 * El modelo es básicamente clientGameState.
 * 
 */

public class ClientController extends Thread {

	private static Handler _handler;
	private static ControllerJobsQueue _controllerJobs;
	private static ConnectionJobsQueue _connectionJobs;
	private static Game _game;

	public ClientController(Handler handler, ControllerJobsQueue contrJobs, ConnectionJobsQueue connJobs, Game game) {
		_handler = handler;
		_controllerJobs = contrJobs;
		_connectionJobs = connJobs;
		_game = game;
	}

	@Override
	public void run() {
		System.out.println("CALLED start() in ClientController");
		Job job;
		System.out.println("CLIENT CONTROLLER RUNNING:");
		while(Game.running) {
			//System.out.print("[Controller] Awaiting for a job to process....\n");
			synchronized(_controllerJobs) {
				job = getJob();
				//System.out.println("Got a job.");
				processJob(job);					
			}					
		}		
	}	

	private void processJob(Job job) {
		// game state is modified by clickReceived and message received
		if (job instanceof MessageJob) {
			// System.out.println("Controller: processing ServerMessageJob");
			messageReceived(((MessageJob) job).getServerMessage());
		}
		else if (job instanceof InputReceivedJob) {
			InputReceivedJob j = (InputReceivedJob) job;
			switch(j.getType()) {
			case MOUSE_CLICK:
				clickReceived(j.getX(), j.getY());				
				break;
			case MOUSE_MOVE:
				mouseMoved(j.getX(), j.getY());
				break;
			case MOUSE_ENTERED_CARD:
				// this is a bit dirty.. i receive the card index from j.getX()
				ClientGameState.setMouseOverCard(j.getX());
				break;
			case MOUSE_EXITED_CARD:
				// just tell the view to not render the "mouse over card" frame
				ClientGameState.setMouseOverCard(-1);
				break;
			}

		} 
		//System.out.println("Updating View...");
		_handler.updateView();
	}

	private void mouseMoved(int x, int y) {
		// we know for a fact that mouse entered or exited a card, so lets update state
	}	

	private void messageReceived(ServerMessage sm) {
		//System.out.println("[Controller] calling messageReceived.");
		// si es un mensaje de broadcast
		if (sm.isBroadCastMsg()) {
			//System.out.println("Received broadcast message!!!");
			_handler.broadcastMsgToView(sm.getBroadCastMessage());
		} else {
			// update client game state
			ClientGameState.updateWith(sm);
			if (sm.getTableState().getGamePhase()==PARES && ClientGameState.getPares_hablados()==0) {
				showSpeechBubblesForSkippableRounds(HABLO_PARES);
				if (!table().seJueganPares())
					postConnectionJob(NO_SE_JUEGA_RONDA, (byte)0, "");				
			}
			else if (sm.getTableState().getGamePhase()==JUEGO && ClientGameState.getJuego_hablados()==0) {
				showSpeechBubblesForSkippableRounds(HABLO_JUEGO);
				if (!table().seJuegaJuego())
					postConnectionJob(NO_SE_JUEGA_RONDA, (byte)0, "");
			}
			else if(sm.getTableState().getGamePhase()==FIN_RONDA) {
				showSpeechBubblesForEndOfRound(ClientGameState.table().isOrdago_lanzado());
			}

		}


	}

	// WIP: si unos tienen pares (o juego) y los otros no
	private void showSpeechBubblesForEndOfRound(boolean ordago) {
		// si habia un ordago, simplemente se dice quien lo ganó
		if (ordago) {
			byte absolute_seat_id = table().getGanador(table().getPreviousLance());
			_handler.addSpeechBubble(absolute_seat_id, "GANO EL ORDAGO A " + Language.StringLiterals.LANCES[table().getPreviousLance()], 3000);
		} else {
			for (byte i = GRANDE; i <= JUEGO; i++) {
				// para cada ronda en orden
				// si la ronda se podía jugar
				if (table().rondaWasPlayable(i)) {
					System.out.println("Lance " + Language.StringLiterals.LANCES[i] + " was playable!");
					// si quedó en paso, se le da al ganador
					byte winner_seat_id = table().getGanador(i);
					System.out.println("Ganador de lance " + Language.StringLiterals.LANCES[i] + " es " + table().getClient(winner_seat_id).getName());					
					if (table().lanceQuedoEnPaso(i)) {
						System.out.println("lance " +Language.StringLiterals.LANCES[i] + " quedo en paso");
						_handler.addSpeechBubble(winner_seat_id, "GANO " + Language.StringLiterals.LANCES[i] + " EN PASO",  2000);					
					} else {
						System.out.println("lance " + Language.StringLiterals.LANCES[i] + " no quedo en paso");						
						_handler.addSpeechBubble(winner_seat_id, "GANO " + table().piedrasApostadasEnRonda(i) + " a " + Language.StringLiterals.LANCES[i],  2000);					
					}
					// si estamos a pares o juego, el compañero del ganador también se apunta sus pares y/o juego en caso de que tenga
					if (i==PARES) {
						byte pareja_winner = table().opuesto(winner_seat_id);
						if (Jugadas.valorEnPiedrasMano(PARES, table().getClient(pareja_winner)) >0) {
							_handler.addSpeechBubble(winner_seat_id, "GANO " + Jugadas.valorMano(PARES, table().getClient(pareja_winner)) + " DE PARES",  2000);							
						}
					}
					else if (i== JUEGO) {
						byte pareja_winner = table().opuesto(winner_seat_id);
						if (Jugadas.valorEnPiedrasMano(JUEGO, table().getClient(pareja_winner)) >0) {
							_handler.addSpeechBubble(winner_seat_id, "GANO " + Jugadas.valorMano(JUEGO, table().getClient(pareja_winner)) + " DE JUEGO",  2000);							
						}						
					}
				} else {
					System.out.println("lance " + Language.StringLiterals.LANCES[i] + " was not playable :(");
					switch(i) {
					case PARES:
						for (byte j = 0; j < MAX_CLIENTS; j++) {
							if (table().tienePares(j))
								_handler.addSpeechBubble(j, "ME LLEVO " + Jugadas.valorMano(PARES,table().getClient(j)) + " de " + Language.StringLiterals.LANCES[i],  2000);
						}
						break;
					case JUEGO:
						for (byte j = 0; j < MAX_CLIENTS; j++) {
							if (table().tieneJuego(j))
								_handler.addSpeechBubble(j, "ME LLEVO " + Jugadas.valorMano(JUEGO, table().getClient(j)) + " de " + Language.StringLiterals.LANCES[i],  2000);
						}
						break;
					}
				}
			}
		}
	}

	private void showSpeechBubblesForSkippableRounds(byte que_hablo) {
		// first time we enter into PARES, we each have to talk. first to talk is relativePosition(mano)
		byte next_seat_id = UIParameters.relativePosition(my_seat_id(), table().getMano_seat_id());
		for (int i = 0; i < MAX_CLIENTS; i++) {
			ClientMessage broadCastMessage = new ClientMessage(que_hablo,next_seat_id,table().getClient(next_seat_id).getID());
			_handler.broadcastMsgToView(broadCastMessage);
			if (que_hablo==HABLO_PARES) {
				ClientGameState.setPares_hablados((byte) (ClientGameState.getPares_hablados() + 1));				
			} else if (que_hablo == HABLO_JUEGO) {
				ClientGameState.setJuego_hablados((byte) (ClientGameState.getJuego_hablados() + 1));
			}
			next_seat_id = TableState.nextTableSeatId(next_seat_id);					
		}
	}

	private void clickReceived(int x, int y) {
		// clicks are to be addressed when:
		// 1. clicking on the "have a seat" button
		// 2. clicking on the "stand up" button
		// 3. click on a button from the menu
		// 4. select/deselect a card
		if (ClientGameState.table() != null && ClientGameState.isClickEnabled()) {
			// if not seated, accept seat requests
			if (ClientGameState.table().getSeatOf(ClientGameState.getPlayerID())==-1) {
				handleSeatRequest(x,y);
			} else { 			// i am seated
				handlePlayerAction(x, y);		
			}			
		}


	}

	private void handlePlayerAction(int x, int y) {

		byte my_seat_id =ClientGameState.table().getSeatOf(ClientGameState.getPlayerID());

		switch(ClientGameState.table().getGamePhase()) {


		case GamePhase.DESCARTE:
			int clickedCard = UIParameters.getCardPosUnderMouse(x, y);
			if (clickedCard!=-1 && !me().isCommitedToDiscard()) {
				boolean currentState = ClientGameState.getSelectedCard(clickedCard);
				ClientGameState.setSelectedCard(clickedCard, !currentState);
			} else if (UIParameters.getMenuClickedButton(1,x,y)==0) {
				// clicked on "finished discard" button
				//System.out.println("cards selected as byte:"+ClientGameState.getSelectedCardsAsByte());
				if (ClientGameState.getSelectedCardsAsByte()!=0) {
					postConnectionJob(DESCARTAR,ClientGameState.getSelectedCardsAsByte(),"");
				} else {
					ClientMessage cm = new ClientMessage(ERROR_MUS,(byte)0,"");
					_handler.broadcastMsgToView(cm);
				}
			}
			break;


		case GamePhase.MUS:
			if (my_seat_id== ClientGameState.table().getJugador_debe_hablar()) {
				byte clicked_button = UIParameters.getMenuClickedButton(2,x,y);
				switch(clicked_button) {
				case BUTTON_MUS:
					postConnectionJob(PlayerActions.MUS, (byte) 0,"");
					break;
				case BUTTON_CORTO_MUS:
					postConnectionJob(CORTO_MUS, (byte) 0,"");						
					break;
				}	
			}
			break;

		case GamePhase.GRANDE:
		case GamePhase.CHICA:
		case GamePhase.PARES:
		case GamePhase.JUEGO:
			if (my_seat_id== ClientGameState.table().getJugador_debe_hablar()) {

				byte clicked_button;
				if (table().isOrdago_lanzado()) {
					clicked_button = UIParameters.getMenuClickedButton(2,x,y);
					switch(clicked_button) {
					case BUTTON_ACEPTO_ORDAGO:
						postConnectionJob(ACCEPT, (byte) 0,"");
						break;
					case BUTTON_ME_CAGO:
						postConnectionJob(PASS, (byte) 0,"");						
						break;
					}						
				} else if (table().getPiedras_envidadas_ronda_actual()==0){
					clicked_button = UIParameters.getMenuClickedButton(4,x,y);
					switch(clicked_button) {
					case BUTTON_ENVIDAR_2:
						postConnectionJob(ENVITE, (byte) 2,"");
						break;
					case BUTTON_ENVIDAR_5:
						postConnectionJob(ENVITE, (byte) 5,"");							
						break;
					case BUTTON_ORDAGO:
						postConnectionJob(ORDAGO, (byte) 0,"");
						break;
					case BUTTON_PASO:
						postConnectionJob(PASS, (byte) 0,"");						
						break;
					}						
				} else {
					clicked_button = UIParameters.getMenuClickedButton(5,x,y);
					//System.out.println("Clicked button is:" + clicked_button);
					switch(clicked_button) {
					case BUTTON_ACEPTAR:
						postConnectionJob(ACCEPT, (byte) 0,"");							
						break;
					case BUTTON_ENVIDAR_2:
						postConnectionJob(ENVITE, (byte) 2,"");
						break;
					case BUTTON_ENVIDAR_5:
						postConnectionJob(ENVITE, (byte) 5,"");							
						break;
					case BUTTON_ORDAGO:
						postConnectionJob(ORDAGO, (byte) 0,"");
						break;
					case BUTTON_PASO:
						postConnectionJob(PASS, (byte) 0,"");						
						break;
					}											
				}
			}
			break;

		}	

	}

	private void handleSeatRequest(int x, int y) {
		byte seat = UIParameters.seatRequestWasClicked(x,y);
		if (seat != -1) {
			//System.out.println("Client wishes to seat in " + seat);
			// we request that seat from the server
			postConnectionJob(REQUEST_SEAT, seat, "");		}

	}

	private static Job getJob() {
		Job job;
		synchronized(_controllerJobs) {
			if (_controllerJobs.isEmpty()) {
				try {
					_controllerJobs.wait();
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}
			job = _controllerJobs.getControllerJob();
			//System.out.println("Got a Controller job.");
		}			

		return job;
	}

	public static void postInitialRequest() {
		postConnectionJob(REQUEST_GAME_STATE, (byte)0,  ClientGameState.getPlayerID());
	}

	private static void postConnectionJob(byte playerAction, byte playerQty, String info) {
		ClientMessage cm = null;
		cm = new ClientMessage(playerAction, playerQty, info);
		ConnectionJob job;
		job = new ConnectionJob(cm);
		synchronized(_connectionJobs) {
			//System.out.println("ClientController posted a ConnectionJob to connection jobs queue");
			_connectionJobs.postConnectionJob(job);
			_connectionJobs.notify();
		}
	}

}
