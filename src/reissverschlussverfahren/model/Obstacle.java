package reissverschlussverfahren.model;

import reissverschlussverfahren.IMyAgent;

/*  This extension agent of IMyAgent agent is used to visualize an obstacle on the street.
 *  Also it adds functionality to set the x/y-Location of the obstacle in the continuous space. 
 *  It is written as a singleton, because there is only supposed to be one obstacle at a time.
 * 
 *  The visualization is done by integrating a style for this agent in XML. 
 *  Then formatting it to fit the size of our chosen visualization.
 *  The texture/icon is created in a simple drawing application.
 *  
 *  Style : styles\reissverschlussverfahren.model.Obstacle.style_0.xml
 *  Picture : icons\streetObstacle_icon.png
 */

public class Obstacle extends IMyAgent {

	private static Obstacle instance;

	private Obstacle() {
	}

	// Call-Method for obstacle in singleton pattern
	
	public static Obstacle getInstance() {
		if (Obstacle.instance == null) {
			Obstacle.instance = new Obstacle();
		}
		return Obstacle.instance;
	}

	// Default location of obstacle is x = 50, y = 4.5
	
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
