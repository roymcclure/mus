package roymcclure.juegos.mus.cliente.logic;

import roymcclure.juegos.mus.cliente.UI.ClientWindow;
import roymcclure.juegos.mus.cliente.UI.UIParameters;
import roymcclure.juegos.mus.cliente.logic.jobs.*;
import roymcclure.juegos.mus.common.logic.Language;
import roymcclure.juegos.mus.common.network.ClientMessage;
import roymcclure.juegos.mus.common.network.ServerMessage;
import static roymcclure.juegos.mus.common.logic.Language.ClientGameState.*;

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

	private static Handler _handler;
	private static boolean _running = false;
	private static ControllerJobsQueue _controllerJobs;
	private static ConnectionJobsQueue _connectionJobs;
	
	public Controller(Handler handler, ControllerJobsQueue contrJobs, ConnectionJobsQueue connJobs) {

		_handler = handler;
		_running = true;
		_controllerJobs = contrJobs;
		_connectionJobs = connJobs;
	}
	
	private static void postConnectionJob(byte playerAction, byte playerQty, String info) {
		// i post a connection job requesting initial state of table
		ClientMessage cm = new ClientMessage();
		cm.setAction(playerAction);
		cm.setQuantity(playerQty);
		cm.setInfo(info);
		ConnectionJob job = new ConnectionJob(cm);
		job.setReplyNeeded(true);
		synchronized(_connectionJobs) {
			_connectionJobs.postConnectionJob(job);
			_connectionJobs.notify();
		}
	}
	
	// MODEL is updated  
	// 
	
	private void clickReceived(int x, int y) {
		

		// if not seated, accept seat requests
		if (ClientGameState.table().getSeatOf(ClientGameState.getPlayerID())==-1) {
			byte seat = UIParameters.seatRequestWasClicked(x,y);
			if (seat != -1) {
				System.out.println("Client wishes to seat in " + seat);
				// we request that seat from the server
				postConnectionJob(Language.PlayerActions.REQUEST_SEAT, seat, ""); 
			}
		}
	}
	
	// we assume the client state has not changed between sending and receiving the message
	// it shouldnt since state is changed ONLY after message is received.
	private void messageReceived(ServerMessage sm) {
		System.out.println("[Controller] calling messageReceived.");
		// update client game state
		ClientGameState.updateWith(sm);		
	}
	

	@Override
	public void run() {
		Job job;
		while(_running) {
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
		if (job instanceof ServerMessageJob) {
			System.out.println("Controller: processing ServerMessageJob");
			messageReceived(((ServerMessageJob) job).getServerMessage());
		}
		else if (job instanceof InputReceivedJob) {
			clickReceived(((InputReceivedJob) job).getX(), ((InputReceivedJob) job).getY());
		}
		System.out.println("Updating View...");
		_handler.updateView(ClientWindow.clientGameState);
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
		postConnectionJob(Language.PlayerActions.REQUEST_GAME_STATE, (byte)0,  ClientGameState.getPlayerID());
	}
	
}
