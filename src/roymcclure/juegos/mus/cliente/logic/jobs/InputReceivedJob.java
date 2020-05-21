package roymcclure.juegos.mus.cliente.logic.jobs;

import roymcclure.juegos.mus.common.logic.jobs.Job;

public class InputReceivedJob extends Job {

	private int x, y;
	private byte type;
	
	public InputReceivedJob(int x, int y, byte type) {
		this.x = x;		
		this.y = y;
		this.type = type;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
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
