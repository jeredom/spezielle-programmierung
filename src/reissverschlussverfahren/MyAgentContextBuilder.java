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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Context<IMyAgent> build(Context<IMyAgent> context) {
		// RunEnvironment.getInstance().setScheduleTickDelay(20);

		Parameters p = RunEnvironment.getInstance().getParameters();
		for (String s : p.getSchema().parameterNames()) {
			System.out.println(s + "; ");
		}

		int xdim = p.getInteger("spacewidth");
		int ydim = p.getInteger("spaceheight");

		ContinuousSpace<Object> continuousSpace = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null)
				.createContinuousSpace("space", context, new RandomCartesianAdder(),
						new repast.simphony.space.continuous.WrapAroundBorders(), xdim, ydim);
		NetworkFactoryFinder.createNetworkFactory(null).createNetwork("SensorNetwork", context, false);

		this.addAgentsToContinuousSpace(context, continuousSpace);

		return context;
	}

	private void addAgentsToContinuousSpace(Context<IMyAgent> context, ContinuousSpace<Object> continuousSpace) {

		Street street = new Street();
		context.add(street);
		continuousSpace.moveTo(street, 50.0d, 3.5d);

		// 50 Autos mit Random Geschwindigkeit und Random Spur hinzuf�gen
		Double spawnPoint = 0d;
		for (int i = 0; i < 20; i++) {
			Auto auto = new Auto(continuousSpace, geschwindigkeit(60d, 100d), 6d, -6d);
			context.add(auto);
			spawnPoint = spawnPoint + 2;

			continuousSpace.moveTo(auto, spawnPoint, spur());
		}

		Hindernis hindernis = new Hindernis();
		context.add(hindernis);
		continuousSpace.moveTo(hindernis, 50.0d, 5d);
	}

	private double geschwindigkeit(double min, double max) {
		Random r = new Random();
		return min + (max - min) * r.nextDouble();
	}

	private double spur() {
		Random random = new Random();
		boolean ersteSpur = random.nextBoolean();
		if (ersteSpur)
			return 1.5d;
		else
			return 4.8d;
	}
}
