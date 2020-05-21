package roymcclure.juegos.mus.cliente.logic;

import roymcclure.juegos.mus.cliente.UI.UIParameters;
import roymcclure.juegos.mus.cliente.logic.jobs.*;

import static roymcclure.juegos.mus.common.logic.Language.PlayerActions.*;
import static roymcclure.juegos.mus.common.logic.Language.ButtonIndices.*;
import static roymcclure.juegos.mus.common.logic.Language.MouseInputType.*;

import roymcclure.juegos.mus.common.logic.Language.GamePhase;
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

	public ClientController(Handler handler, ControllerJobsQueue contrJobs, ConnectionJobsQueue connJobs) {
		_handler = handler;
		_controllerJobs = contrJobs;
		_connectionJobs = connJobs;
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

	private void clickReceived(int x, int y) {
		// clicks are to be addressed when:
		// 1. clicking on the "have a seat" button
		// 2. clicking on the "stand up" button
		// 3. click on a button from the menu
		// 4. select/deselect a card


		// if not seated, accept seat requests
		if (ClientGameState.table().getSeatOf(ClientGameState.getPlayerID())==-1) {
			byte seat = UIParameters.seatRequestWasClicked(x,y);
			if (seat != -1) {
				System.out.println("Client wishes to seat in " + seat);
				// we request that seat from the server
				postConnectionJob(REQUEST_SEAT, seat, ""); 
			}
		} else { 			// i am seated
			byte my_seat_id =ClientGameState.table().getSeatOf(ClientGameState.getPlayerID());
			
			switch(ClientGameState.table().getTipo_Lance()) {
			
			
			case GamePhase.DESCARTE:
				int clickedCard = UIParameters.getCardPosUnderMouse(x, y);
				if (clickedCard!=-1 && !me().isCommitedToDiscard()) {
					boolean currentState = ClientGameState.getSelectedCard(clickedCard);
					ClientGameState.setSelectedCard(clickedCard, !currentState);
				} else if (UIParameters.getMenuClickedButton(1,x,y)==0) {
					// clicked on "finished discard" button
					System.out.println("cards selected as byte:"+ClientGameState.getSelectedCardsAsByte());
					postConnectionJob(DESCARTAR,ClientGameState.getSelectedCardsAsByte(),"");
				}
				break;
				
				
			case GamePhase.MUS:
				if (my_seat_id== ClientGameState.table().getJugador_debe_hablar()) {
					byte clicked_button = UIParameters.getMenuClickedButton(2,x,y);
					switch(clicked_button) {
					case BUTTON_MUS:
						postConnectionJob(MUS, (byte) 0,"");
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
						System.out.println("Clicked button is:" + clicked_button);
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
	}

	// we assume the client state has not changed between sending and receiving the message
	// it shouldnt since state is changed ONLY after message is received.
	private void messageReceived(ServerMessage sm) {
		//System.out.println("[Controller] calling messageReceived.");
		// si es un mensaje de broadcast
		if (sm.isBroadCastMsg()) {
			System.out.println("Received broadcast message!!!");
			_handler.broadcastMsgToView(sm.getBroadCastMessage());
		} else {
			// update client game state
			ClientGameState.updateWith(sm);			
		}
			

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

}
