package roymcclure.juegos.mus.cliente.UI;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class ClientPrincipal {

	private static int MAX_ARGS = 4;
	
	public static void main(String[] args) {
		String pos = "0";
		if (args.length==MAX_ARGS) {
			pos = args[MAX_ARGS-1];
		}
		ClientWindow window = new ClientWindow("MUS -- cliente",pos);
		for (int i = 0; i< MAX_ARGS; i++) {
			try {
				window.updateWithArgs(i,args[i]);
			} catch(Exception ex) {
				
			}	
		}		
		javax.swing.SwingUtilities.invokeLater(new Runnable(){
			public void run() {
					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnsupportedLookAndFeelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		          window.setVisible(true);
		    }
		});
	}

}
