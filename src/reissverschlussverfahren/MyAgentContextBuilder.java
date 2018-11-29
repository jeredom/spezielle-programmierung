package reissverschlussverfahren;

import java.util.Random;

import reissverschlussverfahren.model.Auto;
import reissverschlussverfahren.model.Hindernis;
import reissverschlussverfahren.model.Street;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;

public class MyAgentContextBuilder implements ContextBuilder<IMyAgent> {

	private double stdBuffer = 4d;
	private int carCount = 20;
	private double xHindernis;
	private int agresivenessRatio;
	private double maxAcceleration;

	public int getCarCount() {
		return carCount;
	}

	public void setCarCount(int carCount) {
		this.carCount = carCount;
	}

	public double getStdBuffer() {
		return stdBuffer;
	}

	public void setStdBuffer(double stdBuffer) {
		this.stdBuffer = stdBuffer;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Context<IMyAgent> build(Context<IMyAgent> context) {
		// RunEnvironment.getInstance().setScheduleTickDelay(20);

		Parameters p = RunEnvironment.getInstance().getParameters();
		for (String s : p.getSchema().parameterNames()) {
			System.out.println(s + "; ");
		}

		this.setCarCount(p.getInteger("CarCount"));
		this.setStdBuffer(p.getDouble("stdBuffer"));

		xHindernis = p.getDouble("hindernisXLoc");
		maxAcceleration = p.getDouble("maxAcceleration");
		agresivenessRatio = p.getInteger("agresivenessRatio");

		ContinuousSpace<Object> continuousSpace = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null)
				.createContinuousSpace("space", context, new RandomCartesianAdder(),
						new repast.simphony.space.continuous.WrapAroundBorders(), 100, 7);
		NetworkFactoryFinder.createNetworkFactory(null).createNetwork("SensorNetwork", context, false);

		this.addAgentsToContinuousSpace(context, continuousSpace);

		return context;
	}

	private void addAgentsToContinuousSpace(Context<IMyAgent> context, ContinuousSpace<Object> continuousSpace) {

		Street street = new Street();
		context.add(street);
		continuousSpace.moveTo(street, 50.0d, 3.5d);

		Double spawnPoint = 0d;
		for (int i = 0; i < this.getCarCount(); i++) {
			Auto auto = new Auto(continuousSpace, geschwindigkeit(60d, 100d), beschleunigung(maxAcceleration), -6d, this.getStdBuffer(), i + 1,
					false);
			if (i < this.getCarCount() * agresivenessRatio / 100) {
				auto.setAgressiveness(true);
			}
			context.add(auto);
			spawnPoint = spawnPoint + 2;
			continuousSpace.moveTo(auto, spawnPoint, spur());
		}

		Hindernis hindernis = Hindernis.getInstance();
		hindernis.setLocX(xHindernis);
		context.add(hindernis);
		continuousSpace.moveTo(hindernis, hindernis.getLocX(), hindernis.getLocY());
	}

	private double geschwindigkeit(double min, double max) {
		Random r = new Random();
		return min + (max - min) * r.nextDouble();
	}
	
	private double beschleunigung(double max) {
		Random r = new Random();
		return 6d + (max - 6d) * r.nextDouble();
	}

	private double spur() {
		Random random = new Random();
		boolean ersteSpur = random.nextBoolean();
		if (ersteSpur)
			return 1.5d;
		else
			return 4.5d;
	}
}
