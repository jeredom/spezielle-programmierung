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
import repast.simphony.ui.probe.ProbeID;

public class Auto extends IMyAgent {

	private double rightLaneYPosition = 1.5d;
	private double leftLaneYPosition = 4.5d;

	public final Double hoechstgeschwindigkeit;
	public Double aktuelleGeschwindigkeit = 0d;
	public final Double maxPositiveBeschleunigung;
	public final Double maxNegativeBeschleunigung;
	private ContinuousSpace<Object> continuousSpace;
	private String signalState = " ";
	private double stdBuffer = 3d;
	private int carId;
	private boolean agressiveness;

	public Auto(ContinuousSpace<Object> continuousSpace, Double hoechstgeschwindigkeit,
			Double maxPositiveBeschleunigung, Double maxNegativeBeschleunigung, Double paraBuffer, int carId,
			boolean agressiveness) {
		this.continuousSpace = continuousSpace;
		this.hoechstgeschwindigkeit = hoechstgeschwindigkeit;
		this.maxPositiveBeschleunigung = maxPositiveBeschleunigung;
		this.maxNegativeBeschleunigung = maxNegativeBeschleunigung;
		// this.stdBuffer = paraBuffer;
		this.carId = carId;
		this.agressiveness = agressiveness;

		ScheduleParameters sp = ScheduleParameters.createRepeating(1, 1);
		RunEnvironment.getInstance().getCurrentSchedule().schedule(sp, this, "step");
	}

	public boolean isAgressiveness() {
		return agressiveness;
	}

	public void setAgressiveness(boolean agressiveness) {
		this.agressiveness = agressiveness;
	}

	public String getSignalState() {
		return signalState;
	}

	public void setSignalState(String signalState) {
		this.signalState = signalState;
	}

	public void step() {
		if (!areCarsInRadius() && !isHindernisInRadius() && !letCarLane()) {
			if (aktuelleGeschwindigkeit < hoechstgeschwindigkeit) {
				if (shouldAccelerate()) {
					accelerate();
				} else {
					driveWithCurrentSpeed();
				}
			} else if (aktuelleGeschwindigkeit.equals(hoechstgeschwindigkeit)) {
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
		ContinuousWithin<Object> withinDistanceQuery = new ContinuousWithin<Object>(continuousSpace, this,
				this.stdBuffer);
		List<Object> orderedAgentXAxisPositionList = new ArrayList<Object>();
		boolean shouldAccelerate = false;
		for (Object cars : withinDistanceQuery.query()) {
			if (cars instanceof Auto) {
				if (continuousSpace.getLocation(cars).getX() > continuousSpace.getLocation(this).getX()) {
					orderedAgentXAxisPositionList.add(cars);
				}
			}
		}

		if (orderedAgentXAxisPositionList.isEmpty()) {
			shouldAccelerate = true;
		}

		return shouldAccelerate;
	}

	@SuppressWarnings("unchecked")
	private boolean areCarsInRadius() {

		boolean sollBremsen = false;
		double locationThisCarX;
		double locationThisCarY;
		ContinuousWithin<Object> withinDistance = new ContinuousWithin<Object>(continuousSpace, this, 5);
		for (Object agent : withinDistance.query()) {
			if (agent instanceof Auto) {
				double locationOtherCarX = continuousSpace.getLocation(agent).getX();
				double locationOtherCarY = continuousSpace.getLocation(agent).getY();
				locationThisCarX = continuousSpace.getLocation(this).getX();
				locationThisCarY = continuousSpace.getLocation(this).getY();
				if (locationOtherCarX > locationThisCarX && locationOtherCarY == locationThisCarY) {
					double difference = locationOtherCarX - locationThisCarX;
					if (difference < stdBuffer) {
						sollBremsen = true;
						changeLaneIfPossible();
					}
				}
			}
		}
		return sollBremsen;
	}

	private boolean isHindernisInRadius() {

		boolean sollBremsen = false;
		double locationThisCarX = continuousSpace.getLocation(this).getX();
		double locationThisCarY = continuousSpace.getLocation(this).getY();

		double locationHindernis = Hindernis.getInstance().getLocX();

		if (locationHindernis > locationThisCarX && locationThisCarY == Hindernis.getInstance().getLocY()) {

			double difference = locationHindernis - locationThisCarX;
			if (difference < 30d) {
				this.setSignalState("R");
				if (difference < stdBuffer) {
					sollBremsen = true;
					changeLaneIfPossible();
				}
			} else {
				this.setSignalState(" ");
			}
		}

		return sollBremsen;
	}

	private boolean letCarLane() {
		boolean sollBremsen = false;
		double locationXHindernis = Hindernis.getInstance().getLocX();
		double locationYHindernis = Hindernis.getInstance().getLocY();
		double locationThisCarX = continuousSpace.getLocation(this).getX();
		double locationThisCarY = continuousSpace.getLocation(this).getY();

		double diff = locationXHindernis - locationThisCarX;
		if (locationThisCarY != locationYHindernis && diff <= 7 && diff > 6 && this.agressiveness == false) {
			ContinuousWithin<Object> withinDistance = new ContinuousWithin<Object>(continuousSpace,
					Hindernis.getInstance(), 5);
			for (Object agent : withinDistance.query()) {
				if (agent instanceof Auto) {
					double locationAgentCarX = continuousSpace.getLocation(agent).getX();
					double locationAgentCarY = continuousSpace.getLocation(agent).getY();
					if (locationAgentCarY == locationYHindernis) {
						if (locationAgentCarX < locationXHindernis) {
							sollBremsen = true;
						}
					}
				}
			}
		}
		return sollBremsen;

	}

	private void changeLaneIfPossible() {
		List<Double> carsInRadiusOnOppositeLane = getCarsInRadiusOnOppositeLane();
		double locationNearestCarOppositeLane;
		double locationXHindernis = Hindernis.getInstance().getLocX();
		double locationYHindernis = Hindernis.getInstance().getLocY();

		double locationThisCarX = continuousSpace.getLocation(this).getX();
		double locationThisCarY = continuousSpace.getLocation(this).getY();

		double difference = locationXHindernis - locationThisCarX;
		if (carsInRadiusOnOppositeLane.isEmpty() && ((difference > 30d || difference < 4)
				&& (locationThisCarY == locationYHindernis || locationThisCarX > locationXHindernis))) {
			moveCarToOppositeLane();
		} else if (!carsInRadiusOnOppositeLane.isEmpty() && ((difference > 30d || difference < 4)
				&& (locationThisCarY == locationYHindernis || locationThisCarX > locationXHindernis))) {
			locationNearestCarOppositeLane = Collections.max(carsInRadiusOnOppositeLane);
			if (locationNearestCarOppositeLane < continuousSpace.getLocation(this).getX() - this.stdBuffer) {
				moveCarToOppositeLane();
			}
		}
	}

	private List<Double> getCarsInRadiusOnOppositeLane() {
		ContinuousWithin<Object> withinDistanceQuery = new ContinuousWithin<Object>(continuousSpace, this,
				this.stdBuffer + 1);
		List<Double> orderedAgentXAxisPositionList = new ArrayList<Double>();
		double locationThisCarXAxis = continuousSpace.getLocation(this).getX();
		double locationThisCarYAxis = continuousSpace.getLocation(this).getY();

		for (Object cars : withinDistanceQuery.query()) {
			if (cars instanceof Auto) {
				double locationCarXAxis = continuousSpace.getLocation(cars).getX();
				double locationCarYAxis = continuousSpace.getLocation(cars).getY();
				if (locationThisCarYAxis == leftLaneYPosition) {
					if (locationCarYAxis == rightLaneYPosition
							&& locationCarXAxis < locationThisCarXAxis + this.stdBuffer) {
						orderedAgentXAxisPositionList.add(locationCarXAxis);
					}
				} else if (locationThisCarYAxis == rightLaneYPosition) {
					if (locationCarYAxis == leftLaneYPosition
							&& locationCarXAxis < locationThisCarXAxis + this.stdBuffer) {
						orderedAgentXAxisPositionList.add(locationCarXAxis);
					}
				}
			}
		}
		return orderedAgentXAxisPositionList;
	}

	private void moveCarToOppositeLane() {
		double changeInYAxisPosition = 0d;
		double locationThisCarYAxis = continuousSpace.getLocation(this).getY();

		if (locationThisCarYAxis == leftLaneYPosition) {
			this.setSignalState("R");
			changeInYAxisPosition = -3d;
		} else if (locationThisCarYAxis == rightLaneYPosition) {
			this.setSignalState("L");
			changeInYAxisPosition = 3d;
		}
		continuousSpace.moveTo(this, continuousSpace.getLocation(this).getX(),
				continuousSpace.getLocation(this).getY() + changeInYAxisPosition);
		this.setSignalState(" ");
	}

	@ProbeID
	@Override
	public String toString() {
		return "Car " + this.carId;
	}
}
