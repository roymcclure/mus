package roymcclure.juegos.mus.server.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import roymcclure.juegos.mus.common.network.ClientMessage;
import roymcclure.juegos.mus.common.network.ServerMessage;
import roymcclure.juegos.mus.server.logic.ServerGameState;
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
	byte thread_id;
	private boolean connected = false;
	ServerGameState gs;
	SrvMus server;
	private String client_ID;
	
	// data for sending / receiving
	private InputStream in = null;
	private ObjectInputStream objIn = null;
	
	private ClientMessage cm;
	
	private OutputStream out;
	private ObjectOutputStream objOut;
	
	public AtenderCliente (Socket s, ServerGameState gs, byte thread_id, SrvMus server) {
		this.socket = s;
		this.gs = gs;
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
				System.out.print("AtenderCliente de thread "+thread_id+": recibiendo mensaje...");
				cm = this.receive();
				System.out.println("recibido.");
				// if the 
				System.out.print("ATenderCliente de thread "+ thread_id + ": Actualizando estado ...");
				gs.updateGameStateWith(cm, thread_id);
				System.out.println("Actualizado.");
				System.out.print("Creando una respuesta y enviándola....");
				send(ServerMessage.forgeDataPacket(gs, thread_id));
				System.out.println("Enviada");

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				// received object's class was not found
			} catch (SocketException se) {
				connected = false;
				System.out.println("Client disconnected.");
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
	
	public void disconnect() {
		try {
			socket.close();
			connected = false;			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}
