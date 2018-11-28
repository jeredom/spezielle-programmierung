package reissverschlussverfahren.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public Double aggressiveness;
	public boolean indicatingLaneChange;
	public boolean isStanding;


	public Auto(ContinuousSpace<Object> continuousSpace, Double hoechstgeschwindigkeit,
			Double maxPositiveBeschleunigung, Double maxNegativeBeschleunigung, Double aggressiveness) {
		this.continuousSpace = continuousSpace;
		this.hoechstgeschwindigkeit = hoechstgeschwindigkeit;
		this.maxPositiveBeschleunigung = maxPositiveBeschleunigung;
		this.maxNegativeBeschleunigung = maxNegativeBeschleunigung;
		this.aggressiveness = aggressiveness;
		ScheduleParameters sp = ScheduleParameters.createRepeating(1, 1);
		RunEnvironment.getInstance().getCurrentSchedule().schedule(sp, this, "step");
	}

	public void step() {
		isStanding = shouldBreakBecauseOfAgentsInFront();
			if (!isStanding) {
				if (aktuelleGeschwindigkeit < hoechstgeschwindigkeit) {
					if(shouldAccelerate()) {
						accelerate();
					}else {
						driveWithCurrentSpeed();
					}
				}else if (aktuelleGeschwindigkeit.equals(hoechstgeschwindigkeit)) {
					driveWithMaximumSpeed();
				}
			}
	}

	private void accelerate() {
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

	}
	
	private void driveWithCurrentSpeed() {
		Double neuXAchsenLocation = continuousSpace.getLocation(this).getX() + (aktuelleGeschwindigkeit / 100);
		NdPoint newLocation = new NdPoint(neuXAchsenLocation, continuousSpace.getLocation(this).getY());
		continuousSpace.moveTo(this, newLocation.getX(), newLocation.getY());

	}
	
	private boolean shouldAccelerate() {
		ContinuousWithin<Object> withinDistanceQuery = new ContinuousWithin<Object>(continuousSpace, this, 3d);
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
	
	@SuppressWarnings("unchecked")
	private boolean shouldBreakBecauseOfAgentsInFront() {

		boolean sollBremsen = false;
		double locationThisCarX;
		ContinuousWithin<Object> withinDistance = new ContinuousWithin<Object>(continuousSpace, this, 3d);
		for (Object agent : withinDistance.query()) {
			if (agent.getClass() == Auto.class) {
				double locationOtherCarX = continuousSpace.getLocation(agent).getX();
				locationThisCarX = continuousSpace.getLocation(this).getX();
				if (locationOtherCarX > locationThisCarX) {
					double difference = locationOtherCarX - locationThisCarX;
					if (difference < 4d) {
						sollBremsen = true;
						if (locationThisCarX > 50.0d) {
							changeLaneIfPossible();
						}
					}
				}
			}
			if (agent.getClass() == Hindernis.class) {
				
				double locationHindernis = continuousSpace.getLocation(agent).getX();
				locationThisCarX = continuousSpace.getLocation(this).getX();
				double locationThisCarY = continuousSpace.getLocation(this).getY();
				if (locationThisCarY == 4.5d) {
					if (locationHindernis > locationThisCarX) {
						double difference = locationHindernis - locationThisCarX;
						if (difference < 4d) {
							sollBremsen = true;
							changeLaneIfPossible();
						}
					}
				}
			}

		}
		return sollBremsen;
	}

	private boolean shouldBreakForCarToChangeLane() {
		Map<Double, Object> orderedAgentMap = getCarsInRadiusOnOppositeLane();
		List<Double> carsInRadiusOnOppositeLane = new ArrayList<Double>(orderedAgentMap.keySet());
		double locationNearestCarOppositeLane;
		double locationThisCarXAxis = continuousSpace.getLocation(this).getX();
		boolean shouldBreak = false;
		if(!orderedAgentMap.isEmpty()) {
			locationNearestCarOppositeLane = Collections.max(carsInRadiusOnOppositeLane);
			System.out.println("other car: "
					+ locationNearestCarOppositeLane + " this car: " +  locationThisCarXAxis );
			if(locationNearestCarOppositeLane > locationThisCarXAxis +3d && locationNearestCarOppositeLane < locationThisCarXAxis + 5d) {
				Auto auto = (Auto) orderedAgentMap.get(locationNearestCarOppositeLane);
				if(auto.isIndicatingLaneChange() == true) {
					shouldBreak = true;
				}
			}
		}
		return shouldBreak;
		
	} 
	
	private void changeLaneIfPossible() {
		Map<Double, Object> orderedAgentMap = getCarsInRadiusOnOppositeLane();
		List<Double> carsInRadiusOnOppositeLane = new ArrayList<Double>(orderedAgentMap.keySet());
		double locationNearestCarOppositeLane;
		double locationThisCarXAxis = continuousSpace.getLocation(this).getX();
		this.setIndicatingLaneChange(true);
		if(carsInRadiusOnOppositeLane.isEmpty()) {
			moveCarToOppositeLane();
		}else if(!carsInRadiusOnOppositeLane.isEmpty()) {
			locationNearestCarOppositeLane = Collections.max(carsInRadiusOnOppositeLane);
			if (locationNearestCarOppositeLane < locationThisCarXAxis - 3d) {
				moveCarToOppositeLane();
			}
		}
	}
	
	private Map<Double, Object> getCarsInRadiusOnOppositeLane() {
		ContinuousWithin<Object> withinDistanceQuery = new ContinuousWithin<Object>(continuousSpace, this, 5d);
		Map<Double, Object> orderedAgentMap = new HashMap<Double, Object>();
		double locationThisCarXAxis = continuousSpace.getLocation(this).getX();
		double locationThisCarYAxis = continuousSpace.getLocation(this).getY();
		double rightLaneYPosition = 1.5d;		
		double leftLaneYPosition = 4.5d;
		for (Object car : withinDistanceQuery.query()) {
			double locationCarXAxis = continuousSpace.getLocation(car).getX();
			double locationCarYAxis = continuousSpace.getLocation(car).getY();
			if(locationThisCarYAxis == leftLaneYPosition) {
				if(locationCarYAxis == rightLaneYPosition &&
						locationCarXAxis < locationThisCarXAxis + 3d) {	
					orderedAgentMap.put(locationCarXAxis, car);
				}
			} else if(locationThisCarYAxis == rightLaneYPosition) {
				if(locationCarYAxis == leftLaneYPosition &&
						locationCarXAxis < locationThisCarXAxis + 3d) {	
					orderedAgentMap.put(locationCarXAxis, car);
				}
			}	
		}
		return orderedAgentMap;
	}
	
	private void moveCarToOppositeLane() {
		double changeInYAxisPosition = 0d;
		double locationThisCarYAxis = continuousSpace.getLocation(this).getY();
		double rightLaneYPosition = 1.5d;		
		double leftLaneYPosition = 4.5d;
		if(locationThisCarYAxis == leftLaneYPosition) {
			changeInYAxisPosition = -3d;
		}else if(locationThisCarYAxis == rightLaneYPosition) {
			changeInYAxisPosition = 3d;
		}
		continuousSpace.moveTo(this, continuousSpace.getLocation(this).getX(), continuousSpace.getLocation(this).getY() + changeInYAxisPosition);
	}

	public boolean isIndicatingLaneChange() {
		return indicatingLaneChange;
	}

	public void setIndicatingLaneChange(boolean indicatingLaneChange) {
		this.indicatingLaneChange = indicatingLaneChange;
	}
}
