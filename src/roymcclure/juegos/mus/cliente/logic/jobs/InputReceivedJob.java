package roymcclure.juegos.mus.cliente.logic.jobs;

import roymcclure.juegos.mus.common.logic.jobs.Job;

public class InputReceivedJob extends Job {

	private int x, y;
	
	public InputReceivedJob(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	
}
