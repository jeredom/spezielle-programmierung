package reissverschlussverfahren.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import reissverschlussverfahren.IMyAgent;
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

		if (!findAgentsinRadius()) {
			if (aktuelleGeschwindigkeit < hoechstgeschwindigkeit) {
				if(shouldAccelerate()) {
					beschleunigen();
				}else {
					driveWithCurrentSpeed();
				}
			}
			if (aktuelleGeschwindigkeit.equals(hoechstgeschwindigkeit)) {
				driveWithMaximumSpeed();
			}
		}
	}

	private void beschleunigen() {
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
	}

	private void driveWithMaximumSpeed() {
		Double neuXAchsenLocation = continuousSpace.getLocation(this).getX() + (hoechstgeschwindigkeit / 100);
		NdPoint newLocation = new NdPoint(neuXAchsenLocation, continuousSpace.getLocation(this).getY());
		continuousSpace.moveTo(this, newLocation.getX(), newLocation.getY());
		System.out.println(
				"fahren\n" + "Besch." + aktuelleGeschwindigkeit + "Pos." + continuousSpace.getLocation(this).getX());

	}
	
	private void driveWithCurrentSpeed() {
		Double neuXAchsenLocation = continuousSpace.getLocation(this).getX() + (aktuelleGeschwindigkeit / 100);
		NdPoint newLocation = new NdPoint(neuXAchsenLocation, continuousSpace.getLocation(this).getY());
		continuousSpace.moveTo(this, newLocation.getX(), newLocation.getY());
		System.out.println(
				"fahren\n" + "Besch." + aktuelleGeschwindigkeit + "Pos." + continuousSpace.getLocation(this).getX());

	}
	
	private boolean shouldAccelerate() {
		ContinuousWithin<Object> withinDistanceQuery = new ContinuousWithin<Object>(continuousSpace, this, 2d);
		List<Object> orderedAgentXAxisPositionList = new ArrayList<Object>();
		boolean shouldAccelerate = false;
		for (Object cars : withinDistanceQuery.query()) {
			if(continuousSpace.getLocation(cars).getX() > continuousSpace.getLocation(this).getX()) {	
				orderedAgentXAxisPositionList.add(cars);
			}
		}
		
		if(orderedAgentXAxisPositionList.isEmpty()) { 
			shouldAccelerate = true;
		}
		
		return shouldAccelerate;
	}

	private void abbremsen() {
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
	private boolean findAgentsinRadius() {

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
						changeFromLeftToRightLane();
					}
				}

			}

		}
		return sollBremsen;
	}

	private void changeFromLeftToRightLane() {
		ContinuousWithin<Object> withinDistanceQuery = new ContinuousWithin<Object>(continuousSpace, this, 5d);
		List<Double> orderedAgentXAxisPositionList = new ArrayList<Double>();
		double rightLaneYPosition = 1.5d;
		for (Object cars : withinDistanceQuery.query()) {
			if(continuousSpace.getLocation(cars).getY() == rightLaneYPosition && continuousSpace.getLocation(cars).getX() < continuousSpace.getLocation(this).getX()+1d) {	
				orderedAgentXAxisPositionList.add(continuousSpace.getLocation(cars).getX());
			}
		}
		
		double locationNearestCarRightLine;
		if(orderedAgentXAxisPositionList.isEmpty()) {
			moveCarFromLeftToRigtLane();
			return;
		}else {
			locationNearestCarRightLine = Collections.max(orderedAgentXAxisPositionList);

		}
		
		if (locationNearestCarRightLine > continuousSpace.getLocation(this).getX()-3d ) {
			return;
		}
		else {
			moveCarFromLeftToRigtLane();
		}
		System.out.println(orderedAgentXAxisPositionList);
	}
	
	private void moveCarFromLeftToRigtLane() {
		double distancebetweenLanes = 3d;
		continuousSpace.moveTo(this, continuousSpace.getLocation(this).getX(), continuousSpace.getLocation(this).getY() - distancebetweenLanes);
	}
	
	private void moveCarFromRightToLeftLane() {
		double distancebetweenLanes = 3d;
		continuousSpace.moveTo(this, continuousSpace.getLocation(this).getX(), continuousSpace.getLocation(this).getY() + distancebetweenLanes);

	}
}
