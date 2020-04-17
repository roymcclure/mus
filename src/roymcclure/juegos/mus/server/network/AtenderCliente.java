package roymcclure.juegos.mus.server.network;

import static roymcclure.juegos.mus.common.logic.Language.PlayerActions.*;
import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import roymcclure.juegos.mus.common.logic.GameState;
import roymcclure.juegos.mus.common.logic.TableState;
import roymcclure.juegos.mus.common.network.ClientMessage;
import roymcclure.juegos.mus.common.network.ServerMessage;
import roymcclure.juegos.mus.server.logic.SrvMus;

/**
 * 
 * @author Roy
 *
 *	Esta clase es un hilo que funciona como interlocutor
 *	entre el cliente y el servidor. Redirige los mensajes 
 *	del servidor a los clientes y viceversa. En teoría
 *	su estado está sincronizado con el del cliente, de tal
 *	modo que actúan como en un diálogo, donde la respuesta
 *  del cliente que altere el estado del juego es reenviada
 *  al servidor y este la comunica al resto de jugadores,
 *  y los cálculos del servidor que alteren el estado de juego
 *  son reenviados a todos los clientes.
 *  
 */
public class AtenderCliente extends Thread {
	
	private Socket socket;
	byte thread_id=-1;
	private boolean connected = false;
	GameState gs;
	TableState ts;
	SrvMus server;
	
	// data for sending / receiving
	private ObjectInputStream objIn = null;
	private ObjectOutputStream objOut =  null;	
	
	private ClientMessage cm;


	
	public AtenderCliente (Socket s, GameState gs, TableState ts, byte thread_id, SrvMus server) {
		this.socket = s;
		this.gs = gs;
		this.ts = ts;
		connected = true;
		this.thread_id = thread_id;
		this.server = server;
	}
	
	public void run() {
		// el socket representa mi conexión con el cliente.
		// tengo que enviarle datos o tengo que recibir de él?
		// esto depende del estado de la partida,
		// por lo que antes de leer o escribir tendré que examinar el estado de la partida
		// puede haber casos en los que el cliente se deba quedar esperando hasta que su hilo
		// le diga que ya puede hablar, por ejemplo cuando el cliente se ha sentado a la mesa
		// 
		while(connected) {
			try {

				cm = this.receive();
				System.out.print("thread_id ["+thread_id+"] sent a message.\n");
				System.out.println(cm);
				updateGameStateWith(cm, thread_id);
				send(ServerMessage.forgeDataPacket(gs, ts, thread_id));
				System.out.println("Enviada");

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				// received object's class was not found
			} catch (SocketException se) {
				connected = false;
				System.out.println("Client disconnected.Calling releaseThread("+thread_id+")");
				server.releaseThread(thread_id);
				synchronized(gs) {
					ts.clearSeat(ts.getSeatOf(gs.getPlayerID(thread_id)));
					gs.setPlayerID(NO_PLAYER, thread_id);
					
				}
				// se.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				// error reading/writing
			} 
		}
		// tell server that user disconnected
		// and to release seat_id
		server.releaseThread(thread_id);
		

	}
	
	public ClientMessage receive() throws IOException, ClassNotFoundException {
		if (objIn==null)
			objIn = new ObjectInputStream(socket.getInputStream());
		ClientMessage cm = (ClientMessage) objIn.readObject();
		return cm;		
	}

	public void send(ServerMessage sm) throws IOException {
		if (objOut==null)
			objOut = new ObjectOutputStream (socket.getOutputStream());
		objOut.writeObject(sm);		
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public void disconnect() {
		try {
			socket.close();
			connected = false;			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public synchronized void updateGameStateWith(ClientMessage cm, byte thread_id) {
		// si cliente solicita información del mundo, realmente no hacemos gran cosa.
		if (cm.getAction() == REQUEST_GAME_STATE) {
			// player can pass their name here
			String playerID = cm.getInfo();
			synchronized(gs) {
				gs.setPlayerID(playerID, thread_id);
			}
			System.out.println("SERVER: player " + playerID.toString() + " connected.");			
		}
		
		if (cm.getAction() == REQUEST_SEAT) {
			// we try to seat the player in the requested seat
			// we assume the player knows the game state so it doesnt really need
			// a refresh at this point
			System.out.println("PLAYER " + gs.getPlayerID(thread_id) + " requested the SEAT " + cm.getQuantity());
			byte requested_seat = cm.getQuantity();
			ts.takeAseat(requested_seat, gs.getPlayerID(thread_id));
		}

	}	


}
