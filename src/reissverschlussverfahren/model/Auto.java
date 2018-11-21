package reissverschlussverfahren.model;

import java.util.LinkedList;
import java.util.List;

import reissverschlussverfahren.IMyAgent;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;

public class Auto extends IMyAgent {

	public final Double hoechstgeschwindigkeit;
	public Double aktuelleGeschwindigkeit = 0d;
	public final Double maxPositiveBeschleunigung;
	public final Double maxNegativeBeschleunigung;
	private ContinuousSpace<Object> continuousSpace;

	public Auto(ContinuousSpace<Object> continuousSpace, Double hoechstgeschwindigkeit,
			Double maxPositiveBeschleunigung, Double maxNegativeBeschleunigung) {
		this.continuousSpace = continuousSpace;
		this.hoechstgeschwindigkeit = hoechstgeschwindigkeit;
		this.maxPositiveBeschleunigung = maxPositiveBeschleunigung;
		this.maxNegativeBeschleunigung = maxNegativeBeschleunigung;
		ScheduleParameters sp = ScheduleParameters.createRepeating(1, 1);
		RunEnvironment.getInstance().getCurrentSchedule().schedule(sp, this, "step");
	}

	public void step() {

		if (!agentenInRadiusFinden()) {
			if (aktuelleGeschwindigkeit < hoechstgeschwindigkeit) {
				beschleunigen();
			}
			if (aktuelleGeschwindigkeit.equals(hoechstgeschwindigkeit)) {
				fahren();
			}
		}
	}

	public void beschleunigen() {
		Double differenz = hoechstgeschwindigkeit - aktuelleGeschwindigkeit;
		Double neuXAchsenLocation;

		if (differenz > maxPositiveBeschleunigung) {
			neuXAchsenLocation = continuousSpace.getLocation(this).getX()
					+ (aktuelleGeschwindigkeit + maxPositiveBeschleunigung) / 100;
			aktuelleGeschwindigkeit = aktuelleGeschwindigkeit + maxPositiveBeschleunigung;
		} else {
			neuXAchsenLocation = continuousSpace.getLocation(this).getX() + (aktuelleGeschwindigkeit + differenz) / 100;
			aktuelleGeschwindigkeit = hoechstgeschwindigkeit;
		}
		NdPoint newLocation = new NdPoint(neuXAchsenLocation, continuousSpace.getLocation(this).getY());
		continuousSpace.moveTo(this, newLocation.getX(), newLocation.getY());
		System.out.println("beschleunigen\n" + "Besch." + aktuelleGeschwindigkeit + "Pos."
				+ continuousSpace.getLocation(this).getX());
	}

	public void fahren() {
		Double neuXAchsenLocation = continuousSpace.getLocation(this).getX() + (hoechstgeschwindigkeit / 100);
		NdPoint newLocation = new NdPoint(neuXAchsenLocation, continuousSpace.getLocation(this).getY());
		continuousSpace.moveTo(this, newLocation.getX(), newLocation.getY());
		System.out.println(
				"fahren\n" + "Besch." + aktuelleGeschwindigkeit + "Pos." + continuousSpace.getLocation(this).getX());

	}

	public void abbremsen() {
		Double differenz = aktuelleGeschwindigkeit - maxNegativeBeschleunigung;
		Double neuXAchsenLocation;

		if (differenz > 0) {
			neuXAchsenLocation = continuousSpace.getLocation(this).getX() + aktuelleGeschwindigkeit
					+ maxNegativeBeschleunigung;
			aktuelleGeschwindigkeit = aktuelleGeschwindigkeit + maxPositiveBeschleunigung;
		} else {
			neuXAchsenLocation = continuousSpace.getLocation(this).getX() + aktuelleGeschwindigkeit + differenz;
			aktuelleGeschwindigkeit = 0d;
			
		}
		NdPoint newLocation = new NdPoint(neuXAchsenLocation, 2d);
		continuousSpace.moveTo(this, newLocation.getX(), newLocation.getY());

	}

	@SuppressWarnings("unchecked")
	public boolean agentenInRadiusFinden() {

		boolean sollBremsen = false;
		ContinuousWithin<Object> withinDistance = new ContinuousWithin<Object>(continuousSpace, this, 2d);
		for (Object agent : withinDistance.query()) {
			if (agent.getClass() == Auto.class) {
				double locationOtherCarX = continuousSpace.getLocation(agent).getX();
				double locationThisCarX = continuousSpace.getLocation(this).getX();
				if (locationOtherCarX > locationThisCarX) {
					double difference = locationOtherCarX - locationThisCarX;
					if (difference < 2d) {
						sollBremsen = true;
					}
				}
			}
			if (agent.getClass() == Hindernis.class) {
				double locationHindernis = continuousSpace.getLocation(agent).getX();
				double locationThisCarX_2 = continuousSpace.getLocation(this).getX();
				if (locationHindernis > locationThisCarX_2) {
					double difference = locationHindernis - locationThisCarX_2;
					if (difference < 3d) {
						sollBremsen = true;
					}
				}

			}

		}
		return sollBremsen;
	}

}
