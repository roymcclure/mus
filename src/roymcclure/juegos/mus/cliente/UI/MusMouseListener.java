package roymcclure.juegos.mus.cliente.UI;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import roymcclure.juegos.mus.cliente.logic.Game;

public class MusMouseListener implements MouseListener {

		Game g;
	
		public MusMouseListener(Game g) {
			this.g = g;
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			System.out.println("detected mouse press in " + e.getX() + "," + e.getY());
			System.out.println("onScreen: detected mouse press in " + e.getXOnScreen() + "," + e.getYOnScreen());				
		}
		
		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	

}
