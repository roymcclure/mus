package roymcclure.juegos.mus.server.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import roymcclure.juegos.mus.common.network.ClientMessage;
import roymcclure.juegos.mus.server.logic.GameState;
import roymcclure.juegos.mus.server.logic.MessageValidation;
import roymcclure.juegos.mus.common.logic.Language;

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
	int id;
	private boolean connected = false;
	GameState gs;
	
	
	// data for sending / receiving
	private InputStream in;
	private ObjectInputStream objIn;
	
	private ServerMessage gdp;
	private ClientMessage cm;
	
	private OutputStream out2;
	private ObjectOutputStream objOut2;
	
	public AtenderCliente (Socket s, GameState gs, int id) {
		this.socket = s;
		this.gs = gs;
		connected = true;
		this.id = id;
	}
	
	public void run() {
		// el socket representa mi conexión con el cliente.
		// tengo que enviarle datos o tengo que recibir de él?
		// esto depende del estado de las cosas,
		// por lo que antes de leer o escribir tendré que examinar el estado de la partida
		// puede haber casos en los que el cliente se deba quedar esperando hasta que su hilo
		// le diga que ya puede hablar, por ejemplo cuando el cliente se ha sentado a la mesa
		// 
		while(connected) {
			try {
				// puede determinarse si el servidor debe escuchar al cliente o hablarle
				// en función del estado del mundo y el jugador?
				
				if (gs.getExpectedAction(id)==Language.ConnectionState.READ_FROM_CLIENT) {
					cm = this.receive();
					if (MessageValidation.isClientMessageConsistent(cm, gs, id))
				} else if (gs.getExpectedAction(id)==Language.ConnectionState.WRITE_TO_CLIENT){
					this.send(ServerMessage.forgeDataPacket(gs));
				} else if(gs.getExpectedAction(id) == Language.ConnectionState.WAIT_EXTERNAL) {
					wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				// thread was interrupted
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				// received object's class was not found
			} catch (SocketException se) {
				se.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				// error reading/writing
			} 
		}

	}
	
	public ClientMessage receive() throws IOException, ClassNotFoundException {
		in  = socket.getInputStream();
		objIn = new ObjectInputStream(in);
		ClientMessage cm = (ClientMessage) objIn.readObject();
		return cm;		
	}

	public void send(ServerMessage sm) throws IOException {
		out2    = socket.getOutputStream();
		objOut2 = new ObjectOutputStream (out2);
		objOut2.writeObject(sm);		
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
