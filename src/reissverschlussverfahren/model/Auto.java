package reissverschlussverfahren.model;

import reissverschlussverfahren.IMyAgent;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;

public class Auto implements IMyAgent {
	
	public final Double hoechstgeschwindigkeit;
	public Double aktuelleGeschwindigkeit = 0d;
	public final Double maxPositiveBeschleunigung;
	public final Double maxNegativeBeschleunigung;
	private ContinuousSpace<Object> continuousSpace;
	
	public Auto(ContinuousSpace<Object> continuousSpace, Double höchstgeschwindigkeit, Double maxPositiveBeschleunigung, Double maxNegativeBeschleunigung) {
		this.continuousSpace = continuousSpace;
		this.hoechstgeschwindigkeit = höchstgeschwindigkeit;
		this.maxPositiveBeschleunigung = maxPositiveBeschleunigung;
		this.maxNegativeBeschleunigung = maxNegativeBeschleunigung;
		
		ScheduleParameters sp = ScheduleParameters.createRepeating(1, 1);
		RunEnvironment.getInstance().getCurrentSchedule().schedule(sp, this, "step");
	}
	
	public void step() {
		if(aktuelleGeschwindigkeit < hoechstgeschwindigkeit) {
			beschleunigen();			
		}if(aktuelleGeschwindigkeit.equals(hoechstgeschwindigkeit)) {
			fahren();
		}
	}
	
	public void beschleunigen() {
		Double differenz = hoechstgeschwindigkeit - aktuelleGeschwindigkeit;
		Double neuXAchsenLocation;
		
		if(differenz > maxPositiveBeschleunigung) {
			neuXAchsenLocation = continuousSpace.getLocation(this).getX() + aktuelleGeschwindigkeit + maxPositiveBeschleunigung;
			aktuelleGeschwindigkeit = aktuelleGeschwindigkeit + maxPositiveBeschleunigung;
		}else {
			neuXAchsenLocation = continuousSpace.getLocation(this).getX() + aktuelleGeschwindigkeit + differenz;
			aktuelleGeschwindigkeit = hoechstgeschwindigkeit;
		}
			NdPoint newLocation = new NdPoint(neuXAchsenLocation, 2d);
			continuousSpace.moveByDisplacement(this, newLocation.getX());	
	}
	
	public void fahren() {
		Double neuXAchsenLocation = continuousSpace.getLocation(this).getX() + hoechstgeschwindigkeit;
		NdPoint newLocation = new NdPoint(neuXAchsenLocation, 2d);
		continuousSpace.moveByDisplacement(this, newLocation.getX());	
	}

	public void abbremsen() {
		Double differenz = aktuelleGeschwindigkeit - maxNegativeBeschleunigung;
		Double neuXAchsenLocation;
		
		if(differenz > 0) {
			neuXAchsenLocation = continuousSpace.getLocation(this).getX() + aktuelleGeschwindigkeit + maxNegativeBeschleunigung;
			aktuelleGeschwindigkeit = aktuelleGeschwindigkeit + maxPositiveBeschleunigung;
		}else {
			neuXAchsenLocation = continuousSpace.getLocation(this).getX() + aktuelleGeschwindigkeit + differenz;
			aktuelleGeschwindigkeit = 0d;
		}
		NdPoint newLocation = new NdPoint(neuXAchsenLocation, 2d);
		continuousSpace.moveByDisplacement(this, newLocation.getX());	
		
	}

}
