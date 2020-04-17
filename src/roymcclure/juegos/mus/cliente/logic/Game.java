package roymcclure.juegos.mus.cliente.logic;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;


import roymcclure.juegos.mus.cliente.UI.MusMouseListener;
import roymcclure.juegos.mus.cliente.logic.jobs.ControllerJobsQueue;

/*
 * mostly from https://www.youtube.com/watch?v=1gir2R7G9ws
 */

public class Game extends Canvas implements Runnable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4686288067523908021L;
	
	public static final int WIDTH = 1024, HEIGHT = 1024;
	private Thread thread;
	private boolean running = false;
	
	private Handler handler;
	

	
	public Game(ClientGameState clientGameState, ControllerJobsQueue jobs) {		
		handler = new Handler();
		this.addMouseListener(new MusMouseListener(jobs));
		this.setSize(WIDTH, HEIGHT);
	}
	
	public synchronized void start() {
		thread = new Thread(this);
		thread.start();
		running = true;
	}

	public synchronized void stop() {
		try {
			thread.join();
			running = false;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}	
	
	@Override
	public void run() {
		long lastTime = System.nanoTime();
		double amountOfTicks = 60.0f;
		double nsPerTick = 1000000000 / amountOfTicks;
		double delta = 0;
		long timer = System.currentTimeMillis();
		//int frames = 0;
		
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / nsPerTick;
			lastTime = now;
			while (delta >=1) {
				update();
				delta--;
			}
			if(running)
				render();
			//frames++;
			
			// System.out.println("frames:" + frames);
			long then = System.nanoTime();
			try {
				//System.out.println("ns ellapsed between then and now:" + (then - now));
				long diff= (then - now) / 1000000; // ns to ms
				long sleepTime = (long) ((1000 / amountOfTicks) - diff); 
				Thread.sleep( sleepTime > 0 ? sleepTime : 0 );
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				//System.out.println("FPS:" + frames);
				//frames = 0;
			}
			
			
		}
		stop();

	}
	
	private void update() {
		handler.update();
	}
	
	private void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if (bs == null) {
			this.createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		
		g.setColor(Color.decode("#1E7E1E"));
		
		g.fillRect(0, 0, WIDTH, HEIGHT);
		handler.render(g);
		g.dispose();
		bs.show();
	}

	public Handler getHandler() {

		return this.handler;
	}

	
}
