package roymcclure.juegos.mus.cliente.UI;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;

import roymcclure.juegos.mus.cliente.logic.Handler;
import roymcclure.juegos.mus.cliente.logic.jobs.InputReceivedJob;
import roymcclure.juegos.mus.common.logic.jobs.ControllerJobsQueue;

import static roymcclure.juegos.mus.common.logic.Language.MouseInputType.*;

public class GameCanvas extends Canvas {

	
	private static final long serialVersionUID = -3672618905133807570L;
	
	public GameCanvas(ControllerJobsQueue jobs) {
		this.addMouseListener(new MusMouseListener(jobs));
		this.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				
				// dont want to overwhelm the job queue with redundant info
				// so i make the preselection here
				int i = UIParameters.getCardPosUnderMouse(e.getX(), e.getY());
				if (i!=-1) {
					if (!UIParameters.mouseIsOverCard) {
						UIParameters.mouseIsOverCard = true;
						jobs.postRequestJob(new InputReceivedJob(i, e.getY(),MOUSE_ENTERED_CARD));
						synchronized(jobs) {
							jobs.notify();
						}
					}	
				} else {
					if (UIParameters.mouseIsOverCard) {
						UIParameters.mouseIsOverCard = false;
						jobs.postRequestJob(new InputReceivedJob(e.getX(), e.getY(),MOUSE_EXITED_CARD));
						synchronized(jobs) {
							jobs.notify();
						}						
					}
				}								
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		this.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		this.setSize(UIParameters.WIDTH, UIParameters.HEIGHT);
	}

	public void render(Handler handler) {
		BufferStrategy bs = this.getBufferStrategy();
		if (bs == null) {
			this.createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		
		g.setColor(Color.decode("#1E7E1E"));
		
		g.fillRect(0, 0, UIParameters.WIDTH, UIParameters.HEIGHT);
		handler.render(g);
		g.dispose();
		bs.show();		
	}
	
	@Override
    public Dimension getPreferredSize() {
        return new Dimension(1024, 768);
    }


	
	
}
