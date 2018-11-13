package reissverschlussverfahren.model;

import reissverschlussverfahren.IMyAgent;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;

public class Auto implements IMyAgent{
	
	public final Double höchstgeschwindigkeit;
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
	
	@Override
	public void step() {
		
	}
	
	public void moveTo(NdPoint ndPoint) {
		if(continuousSpace.getLocation(this) != ndPoint) {
			
		}
	}

	@Override
	public void accelerate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void abbremsen() {
		// TODO Auto-generated method stub
		
	}

}
