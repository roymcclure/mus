package roymcclure.juegos.mus.cliente.logic;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.util.Random;

import roymcclure.juegos.mus.cliente.UI.MusMouseListener;

/*
 * mostly from https://www.youtube.com/watch?v=1gir2R7G9ws
 */

public class Game extends Canvas implements Runnable {

	
	public static final int WIDTH = 1024, HEIGHT = 1024;
	private Thread thread;
	private boolean running = false;
	
	private Handler handler;
	

	
	public Game() {		
		handler = new Handler();
		this.addMouseListener(new MusMouseListener(this));
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
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		long timer = System.currentTimeMillis();
		int frames = 0;
		
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while (delta >=1) {
				update();
				delta--;
			}
			if(running)
				render();
			frames++;
			
			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				// System.out.println("FPS:" + frames);
				frames = 0;
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

	
}
