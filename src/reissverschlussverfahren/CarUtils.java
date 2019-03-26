package reissverschlussverfahren;

import java.io.FileWriter;
import java.io.IOException;

import reissverschlussverfahren.model.Car;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;

/*  This class contains utilities for the model.
 *  It can be used to collect data and to provide it via CSV-files to the user.
 */

public class CarUtils {
	
	private static String filename;
	
	public static void setFilename(String filename) {
		CarUtils.filename = filename;
	}

	public static String getFilename() {
		return filename;
	}

	/* This method calculates the current average speed of all cars in the model.
	 * This is done by getting all current speed values of the cars and dividing them by their count as saved in the parameter "CarCount".
	 */	
	
	private static int calculateAverageSpeedAllCars(ContinuousSpace<Object> continuousSpace) {
		Parameters p = RunEnvironment.getInstance().getParameters();

		int carCount = p.getInteger("CarCount");
		int summ = 0;

		for (Object car : continuousSpace.getObjects()) {
			if (car instanceof Car) {
				summ += ((Car) car).currentVelocity;
			}
		}
		return summ / carCount;
	}
	
	/*  This method writes the result of the method calculateAverageSpeedAllCars into a CSV file.
	 *  The format is one column with multiple rows. 
	 *  Whenever the method is called, it appends another row.
	 *  
	 *  The target file is src\AverageSpeedAllCars.csv
	 */

	public static void writeAverageSpeedAllCars(ContinuousSpace<Object> continuousSpace) {

		FileWriter fileWriter = null;
		final String NEW_LINE_SEPARATOR = "\n";

		try {
			//fileWriter = new FileWriter("AverageSpeedAllCars.csv", true);
			fileWriter = new FileWriter(getFilename(), true);
			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(String.valueOf(calculateAverageSpeedAllCars(continuousSpace)));

		} catch (Exception e) {
			System.out.println("Error in CSVfileWriter !!!");
			e.printStackTrace();
		} finally {

			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
				e.printStackTrace();
			}
		}
	}
}