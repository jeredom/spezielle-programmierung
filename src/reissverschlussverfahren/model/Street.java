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
 *  
 *  Attributes :
 *  	Constants :
 * 			- xStreet			x-Location of street agent
 *  		- yStreet			y-Location of street agent 
 */

public class Street extends IMyAgent {

	private final double xStreet = 50.0d;
	private final double yStreet = 3.5d;
	
	public double getxStreet() {
		return xStreet;
	}
	public double getyStreet() {
		return yStreet;
	}
	
	
	
}
