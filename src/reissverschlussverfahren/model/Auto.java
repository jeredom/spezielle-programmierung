package reissverschlussverfahren.model;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;

public class Auto {
	
	public final Double höchstgeschwindigkeit;
	public Double aktuelleGeschwindigkeit = 0d;
	public final Double maxPositiveBeschleunigung;
	public final Double maxNegativeBeschleunigung;
	private ContinuousSpace<Object> continuousSpace;
	
	public Auto(ContinuousSpace<Object> continuousSpace, Double höchstgeschwindigkeit, Double maxPositiveBeschleunigung, Double maxNegativeBeschleunigung) {
		this.continuousSpace = continuousSpace;
		this.höchstgeschwindigkeit = höchstgeschwindigkeit;
		this.maxPositiveBeschleunigung = maxPositiveBeschleunigung;
		this.maxNegativeBeschleunigung = maxNegativeBeschleunigung;
		
		ScheduleParameters sp = ScheduleParameters.createRepeating(1, 1);
		RunEnvironment.getInstance().getCurrentSchedule().schedule(sp, this, "step");
	}
	
	public void step() {
		accelerate();
	}
	
	public NdPoint accelerate() {
			Double neuXAchsenLocation = continuousSpace.getLocation(this).getX() + aktuelleGeschwindigkeit + maxPositiveBeschleunigung;
			NdPoint newLocation = new NdPoint(neuXAchsenLocation, 2d);
			aktuelleGeschwindigkeit = aktuelleGeschwindigkeit + maxPositiveBeschleunigung;
			return continuousSpace.moveByDisplacement(this, newLocation.getX());	
	}

	public void abbremsen() {
		Double neuXAchsenLocation = continuousSpace.getLocation(this).getX() + aktuelleGeschwindigkeit + maxNegativeBeschleunigung;
		NdPoint newLocation = new NdPoint(neuXAchsenLocation, 2d);
		aktuelleGeschwindigkeit = aktuelleGeschwindigkeit + maxNegativeBeschleunigung;
		continuousSpace.moveByDisplacement(this, newLocation.getX());	
		
	}

}
