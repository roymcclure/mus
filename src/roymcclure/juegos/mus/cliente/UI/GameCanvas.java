package roymcclure.juegos.mus.cliente.UI;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;

import roymcclure.juegos.mus.cliente.logic.Game;
import roymcclure.juegos.mus.cliente.logic.Handler;
import roymcclure.juegos.mus.common.logic.jobs.ControllerJobsQueue;

public class GameCanvas extends Canvas {

	
	private static final long serialVersionUID = -3672618905133807570L;
	
	public GameCanvas(ControllerJobsQueue jobs) {
		this.addMouseListener(new MusMouseListener(jobs));
		this.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				System.out.println(e.getKeyCode());
				if (e.getKeyCode() == 32) {
					System.out.println("MARICOOON!");
				}
				
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
