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

	private double rightLaneYPosition = 1.5d;		
	private double leftLaneYPosition = 4.5d;
	
	public final Double hoechstgeschwindigkeit;
	public Double aktuelleGeschwindigkeit = 0d;
	public final Double maxPositiveBeschleunigung;
	public final Double maxNegativeBeschleunigung;
	private ContinuousSpace<Object> continuousSpace;
	private double labelState;
	private String signalState = "test";
	private double stdBuffer = 3d;
	

	public String getSignalState() {
		return signalState;
	}

	public void setSignalState(String signalState) {
		this.signalState = signalState;
	}

	public Auto(ContinuousSpace<Object> continuousSpace, Double hoechstgeschwindigkeit,
			Double maxPositiveBeschleunigung, Double maxNegativeBeschleunigung, Double paraBuffer) {
		this.continuousSpace = continuousSpace;
		this.hoechstgeschwindigkeit = hoechstgeschwindigkeit;
		this.maxPositiveBeschleunigung = maxPositiveBeschleunigung;
		this.maxNegativeBeschleunigung = maxNegativeBeschleunigung;
		//this.stdBuffer = paraBuffer;
		
		ScheduleParameters sp = ScheduleParameters.createRepeating(1, 1);
		RunEnvironment.getInstance().getCurrentSchedule().schedule(sp, this, "step");
	}

	public void step() {

		this.setLabelState(0.0);
		if (!areAgentsInRadius()) {
			setSignalState(" ");
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
		ContinuousWithin<Object> withinDistanceQuery = new ContinuousWithin<Object>(continuousSpace, this, this.stdBuffer);
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
	private boolean areAgentsInRadius() {

		boolean sollBremsen = false;
		double locationThisCarX;
		ContinuousWithin<Object> withinDistance = new ContinuousWithin<Object>(continuousSpace, this, this.stdBuffer);
		for (Object agent : withinDistance.query()) {
			if (agent.getClass() == Auto.class) {
				double locationOtherCarX = continuousSpace.getLocation(agent).getX();
				locationThisCarX = continuousSpace.getLocation(this).getX();
				if (locationOtherCarX > locationThisCarX) {
					double difference = locationOtherCarX - locationThisCarX;
					if (difference < stdBuffer) {
						sollBremsen = true;
						changeLaneIfPossible();
					}
				}
			}
			if (agent.getClass() == Hindernis.class) {
				double locationHindernis = continuousSpace.getLocation(agent).getX();
				locationThisCarX = continuousSpace.getLocation(this).getX();
				if (locationHindernis > locationThisCarX) {
					double difference = locationHindernis - locationThisCarX;
					if (difference < stdBuffer) {
						sollBremsen = true;
						changeLaneIfPossible();
						this.setSignalState("\\");
					}
						
					if (difference < 20d) {					
						//changeLaneIfPossible();
						this.setSignalState("\\");	
						
					}
				}
			}

		}
		return sollBremsen;
	}

	private void changeLaneIfPossible() {
		List<Double> carsInRadiusOnOppositeLane = getCarsInRadiusOnOppositeLane();
		double locationNearestCarOppositeLane;
		if(carsInRadiusOnOppositeLane.isEmpty()) {
			moveCarToOppositeLane();
		}else if(!carsInRadiusOnOppositeLane.isEmpty()) {
			locationNearestCarOppositeLane = Collections.max(carsInRadiusOnOppositeLane);
			if (locationNearestCarOppositeLane < continuousSpace.getLocation(this).getX()-this.stdBuffer ) {
				moveCarToOppositeLane();
			}
		}
	}
	
	private List<Double> getCarsInRadiusOnOppositeLane() {
		ContinuousWithin<Object> withinDistanceQuery = new ContinuousWithin<Object>(continuousSpace, this, this.stdBuffer+1);
		List<Double> orderedAgentXAxisPositionList = new ArrayList<Double>();
		double locationThisCarXAxis = continuousSpace.getLocation(this).getX();
		double locationThisCarYAxis = continuousSpace.getLocation(this).getY();
	
		for (Object cars : withinDistanceQuery.query()) {
			double locationCarXAxis = continuousSpace.getLocation(cars).getX();
			double locationCarYAxis = continuousSpace.getLocation(cars).getY();
			if(locationThisCarYAxis == leftLaneYPosition) {
				if(locationCarYAxis == rightLaneYPosition &&
						locationCarXAxis < locationThisCarXAxis + this.stdBuffer) {	
					orderedAgentXAxisPositionList.add(locationCarXAxis);
				}
			} else if(locationThisCarYAxis == rightLaneYPosition) {
				if(locationCarYAxis == leftLaneYPosition &&
						locationCarXAxis < locationThisCarXAxis + this.stdBuffer) {	
					orderedAgentXAxisPositionList.add(locationCarXAxis);
				}
			}	
		}
		return orderedAgentXAxisPositionList;
	}
	
	private void moveCarToOppositeLane() {
		double changeInYAxisPosition = 0d;
		double locationThisCarYAxis = continuousSpace.getLocation(this).getY();
		
		if(locationThisCarYAxis == leftLaneYPosition) {
			this.setLabelState(1.0);
			this.setSignalState("\\");
			changeInYAxisPosition = -3d;
		}else if(locationThisCarYAxis == rightLaneYPosition) {
			this.setLabelState(2.0);
			this.setSignalState("/");
			changeInYAxisPosition = 3d;
		}
		continuousSpace.moveTo(this, continuousSpace.getLocation(this).getX(), continuousSpace.getLocation(this).getY() + changeInYAxisPosition);
	}

	public double getLabelState() {
		return labelState;
	}

	public void setLabelState(double labelState) {
		this.labelState = labelState;
	}
}
