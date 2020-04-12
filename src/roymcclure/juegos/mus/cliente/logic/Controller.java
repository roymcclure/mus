package roymcclure.juegos.mus.cliente.logic;

import roymcclure.juegos.mus.cliente.UI.ClientWindow;
import roymcclure.juegos.mus.cliente.UI.UIParameters;
import roymcclure.juegos.mus.cliente.logic.jobs.*;
import roymcclure.juegos.mus.common.logic.Language;
import roymcclure.juegos.mus.common.network.ClientMessage;
import roymcclure.juegos.mus.common.network.ServerMessage;

/*
 * La responsabilidad de esta clase es gestionar los
 * cambios en el modelo.
 * Todos los cambios en el modelo pasan de alguna manera por esta clase.
 * El input desde el listener y la información recibida desde ClientConnection
 * son enviados a colas de trabajo, que son leídas modifican el modelo.
 * A su vez el controlador genera las solicitudes al servidor.
 * Los cambios en el modelo modifican la vista.
 * El modelo es básicamente clientGameState. 
 * 
 */

public class Controller implements Runnable {

	private Handler handler;
	private boolean running = false;
	private ControllerJobsQueue controllerJobs;
	private ConnectionJobsQueue connectionJobs;
	
	public Controller(Handler handler, ControllerJobsQueue contrJobs, ConnectionJobsQueue connJobs) {

		this.handler = handler;
		running = true;
		this.controllerJobs = contrJobs;
		this.connectionJobs = connJobs;
	}
	
	private void postConnectionJob(byte playerAction, byte playerQty, String info) {
		// i post a connection job requesting initial state of table
		ClientMessage cm = new ClientMessage();
		cm.setAction(playerAction);
		cm.setQuantity(playerQty);
		cm.setInfo(info);
		ConnectionJob job = new ConnectionJob(cm);
		job.setReplyNeeded(true);
		synchronized(connectionJobs) {
			connectionJobs.postConnectionJob(job);
			connectionJobs.notify();
		}
	}
	
	private void clickReceived(int x, int y) {
		
		switch(ClientWindow.clientGameState.getGameState()) {
			
			case Language.ClientGameState.AWAITING_GAME_STATE:
				// not much to do while awaiting initial state
				break;

			case Language.ClientGameState.UNSEATED:
				// on this state, the client can request to have a seat
				// the server will reply with its own state, including
				// the client being seated if request was succesful
				// then client will change its state to SEATED
				System.out.println("received a click in "+x+","+y);
				byte seat = UIParameters.seatRequestWasClicked(x,y);
				if (seat != -1) {
					System.out.println("Client clicked to seat in " + seat);
					// we request that seat from the server
					postConnectionJob(Language.PlayerActions.REQUEST_SEAT, seat, "");
				}
				break;
		}
	}
	
	private void messageReceived(ServerMessage sm) {
		// what we do with received data depends on the client game state
		// so next client game state is a function of current state and server game state
		// we modify game state accordingly
		switch(ClientWindow.clientGameState.getGameState()) {
			
			case Language.ClientGameState.AWAITING_GAME_STATE:
				// if my id is seated, it means that i have reconnected to a game
				// before the server kicked me out me from my seat
				// if i am not seated, i might be getting into a new game
				// OR a started one where player was expelled
				// ClientWindow.clientGameState.updateWith(sm)
				ClientWindow.clientGameState.setGameState(Language.ClientGameState.UNSEATED);
				break;
		
			case Language.ClientGameState.UNSEATED:
				// the answer we get is in response to a state request
				// so we udpate our client game state with:
				// what seats are taken, and by whom
				System.out.println("Received message, client state: UNSEATED");
				for (byte i=0; i < Language.GameDefinitions.MAX_CLIENTS; i++) {
					System.out.println("setting client game state, player id:" + sm.getTableState().getPlayerId_By_Seat(i) + " in seat " + i);
					ClientWindow.clientGameState.setPlayerID_by_Seat(i, sm.getTableState().getPlayerId_By_Seat(i));
					String myName = ClientWindow.clientGameState.getPlayerName();
					if (sm.getTableState().getPlayerId_By_Seat(i).equals(myName)) {
						System.out.println("EL SERVIDOR ME CONFIRMA QUE ME HE SENTADO");
					}
				}
				
				
				// how many stones, games, and cows does everyone have
				// how many
				break;
			case Language.ClientGameState.AWAITING_ALL_SEATED:
				// i am seated, the rest are not
				break;
		
		}
	}
	

	@Override
	public void run() {
		Job job;
		while(running) {
				System.out.print("Controller awaiting for a job to process....");
				synchronized(controllerJobs) {
					job = getJob();
					System.out.println("Got a job!");
					System.out.print("PRocessing it...");
					processJob(job);
					System.out.println("Processed!");
				}		
			
		}
		
	}
	
	private void processJob(Job job) {
		// game state is modified by clickReceived and message received
		if (job instanceof ServerMessageJob) {
			System.out.println("Controller: processing ServerMessageJob");
			messageReceived(((ServerMessageJob) job).getServerMessage());
		}
		else if (job instanceof InputReceivedJob) {
			clickReceived(((InputReceivedJob) job).getX(), ((InputReceivedJob) job).getY());
		}
		handler.updateView(ClientWindow.clientGameState);
	}
	
	private Job getJob() {
		Job job;
		synchronized(controllerJobs) {
			if (controllerJobs.isEmpty()) {
				try {
					controllerJobs.wait();
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}
			job = controllerJobs.getControllerJob();
			System.out.println("Got a Controller job.");
		}			
				
		return job;
	}
	
	public void postInitial() {
		postConnectionJob(Language.PlayerActions.REQUEST_GAME_STATE, (byte)0,  ClientWindow.clientGameState.getPlayerName());
	}
	
}
