package reissverschlussverfahren;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.RandomCartesianAdder;

public class MyAgentContextBuilder implements ContextBuilder<IMyAgent> {

	@SuppressWarnings("unchecked")
	@Override
	public Context<IMyAgent> build(Context<IMyAgent> context) {
		// The x dimension of the physical space
		int xdim = 20;
		// The y dimension of the physical space
		int ydim = 20;
		// Create a new 2D continuous space to model the physical space
		ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null).createContinuousSpace("ContinuousSpace2D",
				context, new RandomCartesianAdder(), new repast.simphony.space.continuous.StickyBorders(), xdim, ydim);
		NetworkFactoryFinder.createNetworkFactory(null).createNetwork("SensorNetwork", context, false);
		// The environment parameters contain the user-editable values that appear in
		// the GUI.
		// Get the parameters p and then specifically the initial numbers of wolves and
		// sheep.
		Parameters p = RunEnvironment.getInstance().getParameters();
		for (String s : p.getSchema().parameterNames()) {
			System.out.println(s + "; ");
		}

		int width = p.getInteger("spacewidth");
		int height = p.getInteger("spaceheight");
		
		return context;
	}

}
