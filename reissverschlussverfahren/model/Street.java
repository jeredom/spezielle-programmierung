package reissverschlussverfahren.model;

import reissverschlussverfahren.IMyAgent;

/*	This extension agent of IMyAgent agent is used to visualize a street plane.
 * 
 *  This is done by integrating a style for this agent in XML. 
 *  Then formatting it to fit the size of our chosen visualization.
 *  The texture/icon is created in a simple drawing application.
 *  
 *  Style : styles\reissverschlussverfahren.model.Street.style_0.xml
 *  Picture : icons\street_ic.png
 */

public class Street extends IMyAgent {

	private static Street instance;

	private Street() {
	}

	// Call-Method for street in singleton pattern
	
	public static Street getInstance() {
		if (Street.instance == null) {
			Street.instance = new Street();
		}
		return Street.instance;
	}

	// Default location of street is x = 50, y = 3.5
	
	final private double locX = 50d;
	final private double locY = 3.5d;

	public double getLocX() {
		return locX;
	}

	public double getLocY() {
		return locY;
	}
	
	// Default size of street is width = 100, height = 3.5
	
	final private double sizeX = 100d;
	final private double sizeY = 7d;

	public double getSizeX() {
		return sizeX;
	}

	public double getSizeY() {
		return sizeY;
	}
}
