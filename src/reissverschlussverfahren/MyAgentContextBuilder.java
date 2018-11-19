package reissverschlussverfahren;

import reissverschlussverfahren.model.Auto;
import reissverschlussverfahren.model.Hindernis;
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
		Auto auto = new Auto(continuousSpace, 100d, 6d, -6d);
		Auto auto1 = new Auto(continuousSpace, 50d, 6d, -6d);

		Hindernis hindernis = new Hindernis();
		context.add(auto);
		context.add(auto1);

		continuousSpace.moveTo(auto, 0d, 1.5d);
		continuousSpace.moveTo(auto1, 0d, 4.5d);

		context.add(hindernis);
		continuousSpace.moveTo(hindernis, 50.0d, 5d);
	}

}
