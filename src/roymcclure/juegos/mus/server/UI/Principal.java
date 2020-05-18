package roymcclure.juegos.mus.server.UI;

import javax.swing.SwingUtilities;

import roymcclure.juegos.mus.server.logic.SrvMus;

public class Principal {

	public static void main(String[] args) {
		

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				ServerWindow serverWindow = new ServerWindow();
				SrvMus server = new SrvMus(serverWindow);
				serverWindow.setServer(server);
				serverWindow.setVisible(true);
				if(args.length >= 1) {
					if(args[0].equals("--runOnStart")) {
						serverWindow.runOnStart();
					}
				}
			}			
		});

	}

}
