package roymcclure.juegos.mus.cliente.UI;

public class ClientPrincipal {

	public static void main(String[] args) {
		ClientWindow window = new ClientWindow("MUS -- cliente");
		javax.swing.SwingUtilities.invokeLater(new Runnable(){
			public void run() {
		          window.setVisible(true);
		    }
		});
	}

}
