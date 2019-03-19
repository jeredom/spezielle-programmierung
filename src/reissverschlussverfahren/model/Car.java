package reissverschlussverfahren.model;

//import java.io.FileWriter;
//import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import reissverschlussverfahren.CarUtils;
import reissverschlussverfahren.IMyAgent;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
//import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.ui.probe.ProbeID;

public class Car extends IMyAgent {

  private double hoechstgeschwindigkeit;
  public final Double maxPositiveBeschleunigung;
  public final Double maxNegativeBeschleunigung;
  public Double aktuelleGeschwindigkeit = 0d;
  public Double lowestSpeedAfterBrake = 20d;
  private double rightLaneYPosition = 1.5d;
  private double leftLaneYPosition = 4.5d;
  private ContinuousSpace<Object> continuousSpace;
  private String signalState = " ";
  private double stdRadius = 3d;
  private int carId;
  private boolean agressiveness;
  private int timeDelay = 100;
  private double distanceToStartFlashing = 30d;
  private double maxDistanceToLaneBeforeObstacle = 30d;
  private double minDistanceToLaneBeforeObstacle = 4d;
  private double distanceToLetCarLane = 7d;

  public Car(ContinuousSpace<Object> continuousSpace,
      Double maxPositiveBeschleunigung, Double maxNegativeBeschleunigung, Double paraBuffer,
      int carId,
      boolean agressiveness) {
    this.continuousSpace = continuousSpace;
    //this.hoechstgeschwindigkeit = hoechstgeschwindigkeit;
    this.maxPositiveBeschleunigung = maxPositiveBeschleunigung;
    this.maxNegativeBeschleunigung = maxNegativeBeschleunigung;
    this.carId = carId;
    this.agressiveness = agressiveness;

    ScheduleParameters sp = ScheduleParameters.createRepeating(1, 1);
    RunEnvironment.getInstance().getCurrentSchedule().schedule(sp, this, "step");
  }

  public void setHoechstgeschwindigkeit(double hoechstgeschwindigkeit) {
	this.hoechstgeschwindigkeit = hoechstgeschwindigkeit;
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
    if (checkAbilityToAccelerate()) {
      if (checkActualSpeedToAccelerate()) {
        if (shouldAccelerate()) {
          accelerate();
        } else {
          driveWithCurrentSpeed();
        }
      } else {
        driveWithMaximumSpeed();
      }
    }

    //Write to CSV File whenever the second car uses the method "step"
    if (this.carId == 2) {
      CarUtils.writeAverageSpeedAllCars(continuousSpace);
    }
  }

  /**
   * This method checks, if the car can accelerate
   * 
   */
  private boolean checkAbilityToAccelerate() {
    if (!checkCarsInRadius()) {
      if (!isHindernisInRadius()) {
        if (!letCarLane()) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  /**
   * if Actual < highest return true else return false
   */
  private boolean checkActualSpeedToAccelerate() {
    if (aktuelleGeschwindigkeit < hoechstgeschwindigkeit) {
      return false;
    } else {
      return false;
    }
  }

  /**
   * this methode get all the cars in the radius of the current car
   *
   * @param radius the wanted radius
   */
  private List<Car> getCarsInRadiusOfThisCar(double radius) {
    List<Car> getCarsInRadiusOfThisCar = new ArrayList<Car>();
    ContinuousWithin<Object> withinDistanceQuery = new ContinuousWithin<Object>(continuousSpace,
        this,
        radius);

    for (Object car : withinDistanceQuery.query()) {
      if (car instanceof Car) {
        getCarsInRadiusOfThisCar.add((Car) car);
      }
    }
    return getCarsInRadiusOfThisCar;
  }

  /**
   * this methode get all the cars in the radius(5) of the Obstacle
   */
  private List<Car> getCarsInRadiusOfObstacle() {
    List<Car> getCarsInRadiusOfObstacle = new ArrayList<Car>();
    ContinuousWithin<Object> withinDistanceQuery = new ContinuousWithin<Object>(continuousSpace,
        Obstacle.getInstance(), this.stdRadius + 2);

    for (Object car : withinDistanceQuery.query()) {
      if (car instanceof Car) {
        getCarsInRadiusOfObstacle.add((Car) car);
      }
    }
    return getCarsInRadiusOfObstacle;
  }

  /**
   * with DistanceQuery check all the objects near to the car if its a car add to the list of the
   * nearest objects then check the list if there is no car before it, it should accelerate
   */
  private boolean shouldAccelerate() {
    List<Object> orderedAgentXAxisPositionList = new ArrayList<Object>();

    for (Car cars : getCarsInRadiusOfThisCar(this.stdRadius)) {
      if (continuousSpace.getLocation(cars).getX() > continuousSpace.getLocation(this).getX()) {
        orderedAgentXAxisPositionList.add(cars);
      }
    }
    if (orderedAgentXAxisPositionList.isEmpty()) {
      return true;
    } else {
      return false;
    }
  }


  /**
   * this method move the care deppending on #neuXAchsenLocation after checking and also changing
   * the actual speed
   */
  private void accelerate() {
    Double differenz = hoechstgeschwindigkeit - aktuelleGeschwindigkeit;
    NdPoint newLocation = new NdPoint(getNewXLocation(differenz),
        continuousSpace.getLocation(this).getY());
    continuousSpace.moveTo(this, newLocation.getX(), newLocation.getY());
    updateTheActualSpeed(differenz);
  }

  /**
   * get the new car location for accelerate methode and update the actuale speed depending on the
   * max Positive acceleration and the
   */
  private double getNewXLocation(double difference) {
    Double neuXAchsenLocation;
    if (difference > maxPositiveBeschleunigung) {
      neuXAchsenLocation = continuousSpace.getLocation(this).getX()
          + (aktuelleGeschwindigkeit + maxPositiveBeschleunigung) / timeDelay;
    } else {
      neuXAchsenLocation =
          continuousSpace.getLocation(this).getX()
              + (aktuelleGeschwindigkeit + difference) / timeDelay;
    }
    return neuXAchsenLocation;
  }

  /*
   * changing the actual speed after acceleration
   */
  private void updateTheActualSpeed(double difference) {
    if (difference > maxPositiveBeschleunigung) {
      aktuelleGeschwindigkeit = aktuelleGeschwindigkeit + maxPositiveBeschleunigung;
    } else {
      aktuelleGeschwindigkeit = hoechstgeschwindigkeit;
    }
  }


  /**
   * move with out changing the speed
   */
  private void driveWithCurrentSpeed() {
    Double neuXAchsenLocation =
        continuousSpace.getLocation(this).getX() + (aktuelleGeschwindigkeit / timeDelay);
    NdPoint newLocation = new NdPoint(neuXAchsenLocation, continuousSpace.getLocation(this).getY());
    continuousSpace.moveTo(this, newLocation.getX(), newLocation.getY());
  }

  /**
   * move with the highest speed
   */
  private void driveWithMaximumSpeed() {
    Double neuXAchsenLocation =
        continuousSpace.getLocation(this).getX() + (hoechstgeschwindigkeit / timeDelay);
    NdPoint newLocation = new NdPoint(neuXAchsenLocation, continuousSpace.getLocation(this).getY());
    continuousSpace.moveTo(this, newLocation.getX(), newLocation.getY());
  }

  /**
   * this methode check if there is a car in radius (5) of the current care if yes, checked if it is
   * in a radius of 3, if yes the speed should reduce to the minimum and and chek if it is possible
   * to change the lane and return true
   */
  private boolean checkCarsInRadius() {
    for (Car car : getCarsInRadiusOfThisCar(this.stdRadius + 2)) {
      if (isCarBeforeInRadius(car) && isCarInSameLine(car)) {
        if (getDifferenceToCarBefore(car) < stdRadius) {
          this.aktuelleGeschwindigkeit = lowestSpeedAfterBrake;
          changeLaneIfPossible();
          return true;
        }
      }
    }
    return false;
  }

  /**
   * check if there is a car before the current car on the X achse
   *
   * @return true if there is a car and false if not
   */
  private boolean isCarBeforeInRadius(Car car) {
    double locationOtherCarX = continuousSpace.getLocation(car).getX();
    double locationThisCarX = continuousSpace.getLocation(this).getX();
    if (locationOtherCarX > locationThisCarX) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * calculate the space between the current car and the car before on the X achse
   *
   * @return difference
   */
  private double getDifferenceToCarBefore(Car car) {
    double locationOtherCarX = continuousSpace.getLocation(car).getX();
    double locationThisCarX = continuousSpace.getLocation(this).getX();
    return locationOtherCarX - locationThisCarX;
  }

  /**
   * check if there is a car on same line of the current car
   *
   * @return true if there is a car and false if not
   */
  private boolean isCarInSameLine(Car car) {
    double locationOtherCarY = continuousSpace.getLocation(car).getY();
    double locationThisCarY = continuousSpace.getLocation(this).getY();
    if (locationOtherCarY == locationThisCarY) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * check if the Obstacle of the same line of the current car
   *
   * @return true if there is and false if not
   */
  private boolean isObstacleInSameLine() {
    double locationThisCarY = continuousSpace.getLocation(this).getY();
    double locationHindernisY = Obstacle.getInstance().getLocY();
    if (locationThisCarY == locationHindernisY) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * check if the Obstacle before the current car
   *
   * @return true if there is and false if not
   */
  private boolean isObstacleBeforeCar() {
    double locationThisCarX = continuousSpace.getLocation(this).getX();
    double locationHindernisX = Obstacle.getInstance().getLocX();
    if (locationHindernisX > locationThisCarX) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * calculate the space between the current car and the Obstacle on the X achse
   *
   * @return difference
   */
  private double getDifferenceToObstacle() {
    double locationHindernisX = Obstacle.getInstance().getLocX();
    double locationThisCarX = continuousSpace.getLocation(this).getX();
    return locationHindernisX - locationThisCarX;
  }


  /**
   * this methode check if there the Obstacle before the car and at the same line if yes the care
   * will start flashing and chech if the car in radius (3) from the Obstacle, if yes the speed
   * should reduce to the minimum and and chek if it is possible to change the lane and return true
   */
  private boolean isHindernisInRadius() {
    boolean sollBremsen = false;
    if (isObstacleBeforeCar() && isObstacleInSameLine()) {
      if (getDifferenceToObstacle() < distanceToStartFlashing) {
        this.setSignalState("R");
        if (getDifferenceToObstacle() < stdRadius) {
          sollBremsen = true;
          this.aktuelleGeschwindigkeit = this.lowestSpeedAfterBrake;
          changeLaneIfPossible();
        }
      } else {
        this.setSignalState(" ");
      }
    }
    return sollBremsen;
  }

  /**
   * this method check if it is possible to let the other cars lane by checking 3 conditions if the
   * Obstacle on the same line of the other car, if the car in the position that allowed it to
   * allowed it to let cars lane and if the driver is not agrissive then if thos all true, checked
   * the cars in radius of the obstacle and allowed them to lane after reducing the speed
   */
  private boolean letCarLane() {
    boolean letCarLane = false;

    if (!isObstacleInSameLine()) {
      if (checkCarInPositionToLetCarLane()) {
        if (this.agressiveness == false) {
          for (Car car : getCarsInRadiusOfObstacle()) {
            if (checkCarsBetweenCarAndObstacle(car)) {
              this.aktuelleGeschwindigkeit = this.lowestSpeedAfterBrake;
              letCarLane = true;
            }
          }
        }
      }
    }
    return letCarLane;

  }

  /**
   * this Method check if the care is in distance to the Obstacle, that allowed it to let cars lane
   * wich is bet 6.1 and 7.0
   *
   * @return true if it is
   */
  private boolean checkCarInPositionToLetCarLane() {
    if (getDifferenceToObstacle() <= this.distanceToLetCarLane
        && getDifferenceToObstacle() > this.distanceToLetCarLane - 1) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * this Method check if there is cars between the current car and the Obstacle
   *
   * @param car the other car
   * @return true if there is
   */
  private boolean checkCarsBetweenCarAndObstacle(Car car) {
    double locationHindernisX = Obstacle.getInstance().getLocX();
    double locationHindernisY = Obstacle.getInstance().getLocY();
    double locationOtherCarX = continuousSpace.getLocation(car).getX();
    double locationOtherCarY = continuousSpace.getLocation(car).getY();

    if (locationOtherCarY == locationHindernisY && locationOtherCarX < locationHindernisX) {
      return true;
    } else {
      return false;
    }
  }


  /**
   *
   */
  private void changeLaneIfPossible() {
    List<Double> carsInRadiusOnOppositeLane = getCarsInRadiusOnOppositeLane();
    double locationNearestCarOppositeLane;

    if (carsInRadiusOnOppositeLane.isEmpty()) {
      if (checkCarInPositionToLane()) {
        if (((isObstacleInSameLine() || !isObstacleBeforeCar()))) {
          moveCarToOppositeLane();
        }
      }
    } else if (!carsInRadiusOnOppositeLane.isEmpty()) {
      if (checkCarInPositionToLane()) {
        if (isObstacleInSameLine() || !isObstacleBeforeCar()) {
          locationNearestCarOppositeLane = Collections.max(carsInRadiusOnOppositeLane);
          if (locationNearestCarOppositeLane
              < continuousSpace.getLocation(this).getX() - this.stdRadius) {
            moveCarToOppositeLane();
          }
        }
      }
    }
  }

  /**
   * this Methode check if the care in the position in the position that allowed it to lane
   *
   * @return true if it is
   */
  private boolean checkCarInPositionToLane() {
    double locationHindernisX = Obstacle.getInstance().getLocX();
    double locationThisCarX = continuousSpace.getLocation(this).getX();
    double difference = locationHindernisX - locationThisCarX;
    if (difference > this.maxDistanceToLaneBeforeObstacle) {
      return true;
    } else if (difference < this.minDistanceToLaneBeforeObstacle) {
      return true;
    } else {
      return false;
    }
  }

  /**
   *
   * @return
   */
  private List<Double> getCarsInRadiusOnOppositeLane() {

    List<Double> orderedAgentXAxisPositionList = new ArrayList<Double>();
    double locationThisCarXAxis = continuousSpace.getLocation(this).getX();
    double locationThisCarYAxis = continuousSpace.getLocation(this).getY();

    for (Car car : getCarsInRadiusOfThisCar(this.stdRadius + 1)) {
      double locationOtherXAxis = continuousSpace.getLocation(car).getX();
      double locationOtherYAxis = continuousSpace.getLocation(car).getY();
      if (locationThisCarYAxis == leftLaneYPosition) {
        if (locationOtherYAxis == rightLaneYPosition
            && locationOtherXAxis < locationThisCarXAxis + this.stdRadius) {
          orderedAgentXAxisPositionList.add(locationOtherXAxis);
        }
      } else if (locationThisCarYAxis == rightLaneYPosition) {
        if (locationOtherYAxis == leftLaneYPosition
            && locationOtherXAxis < locationThisCarXAxis + this.stdRadius) {
          orderedAgentXAxisPositionList.add(locationOtherXAxis);
        }
      }
    }
    return orderedAgentXAxisPositionList;
  }

  /**
   * check if the car on the right or the left lane and flashand move to the other lane
   */
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