package reissverschlussverfahren;

import reissverschlussverfahren.model.Auto;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.continuous.ContinuousSpace;

public class MyAgentContextBuilder implements ContextBuilder<Auto> {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Context<Auto> build(Context<Auto> context) {
		int xdim = 20;
		int ydim = 20;
		
		ContinuousSpace<Object> continuousSpace = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null).createContinuousSpace("ContinuousSpace2D",
				context, new RandomCartesianAdder(), new repast.simphony.space.continuous.StickyBorders(), xdim, ydim);
		NetworkFactoryFinder.createNetworkFactory(null).createNetwork("SensorNetwork", context, false);
		Parameters p = RunEnvironment.getInstance().getParameters();
		
		for (String s : p.getSchema().parameterNames()) {
			System.out.println(s + "; ");
		}

		int width = p.getInteger("spacewidth");
		int height = p.getInteger("spaceheight");
		
		return context;
	}
	
	private void addAutosToContinuousSpace(Context<Auto> context, ContinuousSpace<Object> continuousSpace) {
		Auto auto = new Auto(continuousSpace, 50d, 30d, 30d);
		context.add(auto);
	}

}
