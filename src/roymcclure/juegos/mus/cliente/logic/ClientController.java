package roymcclure.juegos.mus.cliente.logic;

import roymcclure.juegos.mus.cliente.UI.UIParameters;
import roymcclure.juegos.mus.cliente.logic.jobs.*;
import static roymcclure.juegos.mus.common.logic.Language.PlayerActions.*;

import roymcclure.juegos.mus.common.logic.jobs.*;
import roymcclure.juegos.mus.common.network.*;

/*
 * La responsabilidad de esta clase es gestionar los
 * cambios en el modelo. Toda solicitud de cambio en el modelo 
 * pasa por esta clase.
 * El input desde el listener y la información recibida desde ControllerJobsQueue
 * son enviados a colas de trabajo, que son leídas modifican el modelo.
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
	
	private static void postConnectionJob(byte playerAction, byte playerQty, String info) {
		ClientMessage cm = null;
		cm = new ClientMessage();
		cm.setAction(playerAction);
		cm.setInfo(info);
		cm.setQuantity(playerQty);
		ConnectionJob job;
		job = new ConnectionJob(cm);
		synchronized(_connectionJobs) {
			System.out.println("ClientController posted a ConnectionJob to connection jobs queue");
			_connectionJobs.postConnectionJob(job);
			_connectionJobs.notify();
		}
	}
	
	
	private void clickReceived(int x, int y) {
		// if not seated, accept seat requests
		if (ClientGameState.table().getSeatOf(ClientGameState.getPlayerID())==-1) {
			byte seat = UIParameters.seatRequestWasClicked(x,y);
			if (seat != -1) {
				System.out.println("Client wishes to seat in " + seat);
				// we request that seat from the server
				postConnectionJob(REQUEST_SEAT, seat, ""); 
			}
		}
	}
	
	// we assume the client state has not changed between sending and receiving the message
	// it shouldnt since state is changed ONLY after message is received.
	private void messageReceived(ServerMessage sm) {
		System.out.println("[Controller] calling messageReceived.");
		// update client game state
		ClientGameState.updateWith(sm);
		// update connection state
		updateConnection();
	}
	

	private void updateConnection() {
		// analyzes the game state, and decides wether it should read or write from the server
		// communicates with the connection thread through postConnectionJob()		
		// while choosing a seat, seated or playing i will listen from the server 
	}

	@Override
	public void run() {
		System.out.println("CALLED start() in ClientController");
		Job job;
		System.out.println("CLIENT CONTROLLER RUNNING:");
		while(Game.running) {
				System.out.print("[Controller] Awaiting for a job to process....\n");
				synchronized(_controllerJobs) {
					job = getJob();
					System.out.println("Got a job.");
					processJob(job);
					
				}		
			
		}
		
	}
	
	private void processJob(Job job) {
		// game state is modified by clickReceived and message received
		if (job instanceof MessageJob) {
			System.out.println("Controller: processing ServerMessageJob");
			messageReceived(((MessageJob) job).getServerMessage());
		}
		else if (job instanceof InputReceivedJob) {
			clickReceived(((InputReceivedJob) job).getX(), ((InputReceivedJob) job).getY());
		}
		System.out.println("Updating View...");
		_handler.updateView();
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
			System.out.println("Got a Controller job.");
		}			
				
		return job;
	}
	
	public static void postInitialRequest() {
		postConnectionJob(REQUEST_GAME_STATE, (byte)0,  ClientGameState.getPlayerID());
	}
	
}
