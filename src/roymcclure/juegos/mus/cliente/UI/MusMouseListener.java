package roymcclure.juegos.mus.cliente.UI;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import roymcclure.juegos.mus.common.logic.jobs.ControllerJobsQueue;
import roymcclure.juegos.mus.cliente.logic.jobs.InputReceivedJob;
import static roymcclure.juegos.mus.common.logic.Language.MouseInputType.*;

public class MusMouseListener implements MouseListener {


		private ControllerJobsQueue jobs;
	
		public MusMouseListener(ControllerJobsQueue jobs) {
			this.jobs = jobs;
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			// we post a job and let the controller thread deal with it
			// of course the UI will have to be updated, but 
			// this way all game state modification is made outside
			// of the UI thread
			// System.out.println("Posting a click job in :" + e.getX() + "," + e.getY());
			jobs.postRequestJob(new InputReceivedJob(e.getX(), e.getY(),MOUSE_CLICK));	
			synchronized(jobs) {
				jobs.notify();
			}
		}
		
		@Override
		public void mouseExited(MouseEvent e) {
			
			
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
			//System.out.println("mouse in " + e.getX() + "," + e.getY());
			
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	

}
