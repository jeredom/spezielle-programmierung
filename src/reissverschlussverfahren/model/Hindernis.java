package reissverschlussverfahren.model;

import reissverschlussverfahren.IMyAgent;

public class Hindernis extends IMyAgent {

	private static Hindernis instance;

	private Hindernis() {
	}

	public static Hindernis getInstance() {
		if (Hindernis.instance == null) {
			Hindernis.instance = new Hindernis();
		}
		return Hindernis.instance;
	}

	private double locX = 50d;
	private double locY = 4.5d;

	public double getLocX() {
		return locX;
	}

	public void setLocX(double locX) {
		this.locX = locX;
	}

	public double getLocY() {
		return locY;
	}

	public void setLocY(double locY) {
		this.locY = locY;
	}

}
