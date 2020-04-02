package roymcclure.juegos.mus.cliente.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.SwingUtilities;

import roymcclure.juegos.mus.cliente.UI.ClientWindow;
import roymcclure.juegos.mus.common.network.*;
import roymcclure.juegos.mus.common.logic.*;


public class ClientConnection extends Thread {
	
	private java.net.Socket socket;
	private InputStream in;
	private ObjectInputStream objIn;
	private ServerDataPacket gdp;
	private OutputStream out;
	private ObjectOutputStream objOut;
	private int connState;
	ClientWindow clientWindow;
	
	private final int WAITING = 0;
	private final int SENDING = 1;
	private final int CLOSED = 2;
	
	public ClientConnection(ClientWindow clientWindow) {
		this.clientWindow = clientWindow;
	}
	
	public int connect(String url, int port) {
		int error = 0; // OK
		try {
			socket = new Socket(url, port);
		} catch (UnknownHostException e) {
			// server doesnt respond
			error = 1;
//			e.printStackTrace();
		} catch (ConnectException ce) {
			// ce.printStackTrace();
			error = 2;
		} catch (IOException e) {
			// error in data transference
			error = 3;
			// e.printStackTrace();
		}
		return error;
	}
	
	

	@Override
	public void run() {
		while(connState != CLOSED) {
			System.out.println("running client connection thread...");

			try {
				Thread.sleep(1000);				
				if (connState == WAITING) {
					gdp = receive();
					// updateUI(gdp);				
				} else if (connState == SENDING){
					ClientMessage cm = null;//getDataPacket();
					send(cm);
					connState = WAITING;
				}
			} catch(ClassCastException e) {
				System.out.println("Se recibió un paquete con objeto inesperado!");						
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BrokenConnectionException e) {
				System.out.println("SE CORTO LA CONEXION!!");
				showConnectionDialog();
			}
		
		}
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("WTF HERE2?");			
		}
	}
		
	
	private void showConnectionDialog() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				clientWindow.showConnectionDialog();

			}

		});
	}
	
	public void send(ClientMessage cm) {
		try {
			out    = socket.getOutputStream();
			objOut = new ObjectOutputStream (out);
			objOut.writeObject(cm);
			
		}
		catch (IOException e) {
			
		}
	}
	
	public ServerDataPacket receive() throws BrokenConnectionException {
		try {
			in  = socket.getInputStream();
			if (in.read()==-1) 
				throw new BrokenConnectionException();
			objIn = new ObjectInputStream(in);
			gdp = (ServerDataPacket) objIn.readObject();
			return gdp;
		}
		catch (IOException e) {
			gdp = new ServerDataPacket();
			return gdp;
		}
		catch (ClassNotFoundException e) {
			gdp = new ServerDataPacket();
			return gdp;
		}
		catch (ClassCastException e) {
			e.printStackTrace();
			gdp = new ServerDataPacket();
			return gdp;
		}
	}
	
}
