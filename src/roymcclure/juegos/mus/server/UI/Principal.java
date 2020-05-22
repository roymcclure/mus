package roymcclure.juegos.mus.server.UI;

import javax.swing.SwingUtilities;

import roymcclure.juegos.mus.common.logic.PlayerState;
import roymcclure.juegos.mus.common.logic.cards.Carta;
import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;

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
				/*
				PlayerState p = new PlayerState();
				Carta[] cartas = new Carta[CARDS_PER_HAND];
				for (int i = 0; i < MAX_CLIENTS; i++) {
					cartas[i] = new Carta((byte) i);
				}
				p.setCartas(cartas);
				System.out.println("valor de pares " + p.valorPares());
				*/
			}			
		});

	}

}
