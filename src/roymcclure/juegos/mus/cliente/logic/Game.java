package roymcclure.juegos.mus.cliente.logic;

import roymcclure.juegos.mus.cliente.UI.ClientWindow;
import roymcclure.juegos.mus.cliente.UI.GameCanvas;


/*
 * mostly from https://www.youtube.com/watch?v=1gir2R7G9ws
 * - removed canvas to UI.
 * 
 */

public class Game implements Runnable {

	private Thread thread;
	public static boolean running = false;

	private Handler handler;
	private GameCanvas gameCanvas;


	public Game(ClientGameState clientGameState, GameCanvas gameCanvas) {		
		handler = new Handler();
		this.gameCanvas = gameCanvas;

	}

	public synchronized void start() {
		System.out.println("Game: thread starting...");
		thread = new Thread(this);
		running = true;
		// so controller can start
		synchronized(ClientWindow.semaphore) {
			ClientWindow.semaphore.notify();
		}
		thread.start();


	}

	public synchronized void stop() {
		try {
			System.out.println("[GAME] stop() called");
			running = false;
			System.out.println("[GAME] joining thread...");
			thread.join();
			System.out.println("joined.");
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
		int frames = 0;

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
			frames++;

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

			// prints FPS every second
			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				//System.out.println("FPS:" + frames);
				frames = 0;
			}			
		}

	}

	private void update() {
		synchronized(handler) {
			handler.update();
		}
	}

	private void render() {
		synchronized(handler) {
			gameCanvas.render(handler);			
		}
	}

	public Handler getHandler() {

		return this.handler;
	}


}
