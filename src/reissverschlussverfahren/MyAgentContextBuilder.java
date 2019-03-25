package reissverschlussverfahren;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import reissverschlussverfahren.model.Car;
import reissverschlussverfahren.model.Obstacle;
import reissverschlussverfahren.model.Street;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;

/*  This class creates the context for a repast model.
 *  The context gets connected to the continuous space.
 *  It adds all the agents to the space and offers functionality to do that in a default way or by calling parameters.
 */

public class MyAgentContextBuilder implements ContextBuilder<IMyAgent> {

	/*  Description of attributes :
	 * 		connected with parameters : 
	 *  		- carCount			number of cars in the model
	 *  		- xObstacle			x-Location of the obstacle in the space / on the street
	 *  		- agresivnessRatio	ratio of aggressive drivers/cars in the model
	 *  		- maxAcceleration	defines the maximum acceleration drivers/cars will be able to use
	 *  	other (constants):
	 *  		- maxAccList			List of values for maximum acceleration 
	 *  		- rightLaneYPosition	location of right Lane axis
	 *  		- leftLaneYPosition		location of left Lane axis
	 *  		- stoppingAcc			negative value for acceleration of break process
	 *  		- stdBuffer				safety distance between the next car in a lane and the car before
	 */
	
	private int carCount = 20;
	private double xObstacle;
	private int agresivenessRatio;
	private double speedLimit;
	private double speedAmplitude;
	
	final private double rightLaneYPosition = 1.5d;
	final private double leftLaneYPosition = 4.5d;
	final private double stoppingAcc = -6.0d;
	final private double defaultGap = 4.0d;
	final private double streetXSize = 100.0d;
	final private double streetYSize = 7.0d;
	
	/*    	Pattern
	 * 		- 20% category fast
	 *   	- 50% category normal
	 *   	- 30% category slow
	 *   
	 *   	- category fast = 30d
	 *   	- category normal = 22d
	 *   	- category slow = 15d
	 *   
	 *   Could be integrated as parameters
	 */
	
	final private int ratioFast = 20;
	final private double accFast = 30d;
	
	final private int ratioNormal = 50;
	final private double accNormal = 22d;
	
	final private int ratioSlow = 30;
	final private double accSlow = 15d;

	
	public int getCarCount() {
		return carCount;
	}

	public void setCarCount(int carCount) {
		this.carCount = carCount;
	}

	/*
	 *  This method fills a list with values relative to the pattern, described in the attributes. 
	 */
	
	private double[] fillMaxAccList() {
		double maxAccList[] = new double[this.getCarCount()];
		int carnumber = 0;
		for (int i = 0; i < this.getCarCount()*ratioFast/100; i++) {
			maxAccList[carnumber] = accFast;
			carnumber++;
		}
		for (int i = 0; i < this.getCarCount()*ratioNormal/100; i++) {
			maxAccList[carnumber] = accNormal;
			carnumber++;
		}
		for (int i = 0; i < this.getCarCount()*ratioSlow/100; i++) {
			maxAccList[carnumber] = accSlow;
			carnumber++;
		}	
		while(carnumber < this.getCarCount()){
			maxAccList[carnumber] = accNormal;
			carnumber++;
		}
		
		Collections.shuffle(Arrays.asList(maxAccList));
		return maxAccList;
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Context<IMyAgent> build(Context<IMyAgent> context) {
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		
		// Debugging - Print all parameters in the console to see if the supposed ones are available
		
		for (String s : p.getSchema().parameterNames()) {
			System.out.println(s);
		}

		// Set class attributes to parameter values
		
		carCount =  p.getInteger("CarCount");
		xObstacle = p.getDouble("hindernisXLoc");
		agresivenessRatio = p.getInteger("agresivenessRatio");
		speedLimit = p.getDouble("speedLimit");
		speedAmplitude = p.getDouble("speedAmplitude");
		
		// Create the continuous space from default factory
		Street street = Street.getInstance();
		
		
		ContinuousSpace<Object> continuousSpace = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null)
				.createContinuousSpace("space", context, new RandomCartesianAdder(),
						new repast.simphony.space.continuous.WrapAroundBorders(), street.getSizeX(), street.getSizeY());
		
		/**
		 *  Sensor Network, could be integrated for analyzing traffic in an additional view
		 *  NetworkFactoryFinder.createNetworkFactory(null).createNetwork("SensorNetwork", context, false);
		 */

		// Add agents via method addAgentsToContinuousSpace from defined space to context
		
		this.addAgentsToContinuousSpace(context, continuousSpace);

		return context;
	}

	/*  This method creates the following agents and places them in the space :
	 *  - Street
	 *  - Cars
	 *  - Obstacle
	 */
	
	private void addAgentsToContinuousSpace(Context<IMyAgent> context, ContinuousSpace<Object> continuousSpace) {

		// Add the street to its respective position
		Street street = Street.getInstance();
		context.add(street);
		continuousSpace.moveTo(street, street.getLocX(), street.getLocY());

		// Add cars one by one and create a continuous pattern by starting to spawn them from a starting point at x=0 
		Double spawnPoint = 0d;
		double maxAccList[] = new double[this.getCarCount()];
		maxAccList = fillMaxAccList();
		for (int i = 0; i < this.getCarCount(); i++) {
			Car car = new Car(continuousSpace,maxAccList[i] , stoppingAcc, defaultGap, i + 1,
					false);
			
			// Determine whether the car is aggressive respective to the given aggressiveness ratio
			if (i < this.getCarCount() * agresivenessRatio / 100) {
				car.setAgressiveness(true);
				// If it is, increase the speed
				car.setHoechstgeschwindigkeit(adaptTopSpeed(speedLimit,speedAmplitude));
			}
			// Else, decrease the speed
			else car.setHoechstgeschwindigkeit(adaptTopSpeed(speedLimit,-speedAmplitude));
			context.add(car);
			// Move the next spawn location
			spawnPoint = spawnPoint + 2;
			// Add car at given location in determined lane
			continuousSpace.moveTo(car, spawnPoint, lane());
		}

		// Add the obstacle to its respective location after setting the x-location to the given parameter
		Obstacle obstacle = Obstacle.getInstance();
		obstacle.setLocX(xObstacle);
		context.add(obstacle);
		continuousSpace.moveTo(obstacle, obstacle.getLocX(), obstacle.getLocY());
	}

	/*  This Method adapts the top speed of the car.
	 *  It requires the general speed limit of the road and adapts it by a random value within the given amplitude.
	 *  The given amplitude can be positive or negative. The method will either increase or decrease the top speed. 
	 */
	
	private double adaptTopSpeed(double speedLimit, double amp) {
		Random r = new Random();
		double adaptedTopSpeed = speedLimit + amp * r.nextDouble();
		return adaptedTopSpeed;
	}

	// This method determines the start lane of a car randomly
	
	private double lane() {
		Random random = new Random();
		boolean leftLane = random.nextBoolean();
		if (leftLane)
			return leftLaneYPosition;
		else
			return rightLaneYPosition;
	}
}
