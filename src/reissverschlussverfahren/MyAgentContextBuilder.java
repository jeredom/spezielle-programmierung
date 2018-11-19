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
		int xdim = 20;
		int ydim = 20;
		
		ContinuousSpace<Object> continuousSpace = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null).createContinuousSpace("space",
				context, new RandomCartesianAdder(), new repast.simphony.space.continuous.WrapAroundBorders(), xdim, ydim);
		NetworkFactoryFinder.createNetworkFactory(null).createNetwork("SensorNetwork", context, false);
		Parameters p = RunEnvironment.getInstance().getParameters();
		
		context.addProjection(continuousSpace);
		
		for (String s : p.getSchema().parameterNames()) {
			System.out.println(s + "; ");
		}

		int width = p.getInteger("spacewidth");
		int height = p.getInteger("spaceheight");
		
		this.addAgentsToContinuousSpace(context, continuousSpace);
		
		return context;
	}
	
	private void addAgentsToContinuousSpace(Context<IMyAgent> context, ContinuousSpace<Object> continuousSpace) {
		Auto auto = new Auto(continuousSpace, 50d, 6d, -6d);
		Hindernis hindernis = new Hindernis();
		context.add(auto);
		context.add(hindernis);
	}

}
