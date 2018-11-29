package reissverschlussverfahren;

import java.io.FileWriter;
import java.io.IOException;

import reissverschlussverfahren.model.Auto;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;

public class AutoUtils {

	private static int calculateAverageSpeedAllCars(ContinuousSpace<Object> continuousSpace) {
		Parameters p = RunEnvironment.getInstance().getParameters();

		int carCount = p.getInteger("CarCount");
		int summ = 0;

		for (Object car : continuousSpace.getObjects()) {
			if (car instanceof Auto) {
				summ += ((Auto) car).aktuelleGeschwindigkeit;
			}
		}
		return summ / carCount;
	}

	public static void writeAverageSpeedAllCars(ContinuousSpace<Object> continuousSpace) {

		FileWriter fileWriter = null;
		final String NEW_LINE_SEPARATOR = "\n";

		try {
			fileWriter = new FileWriter("AverageSpeedAllCars", true);

			fileWriter.append(NEW_LINE_SEPARATOR);
			fileWriter.append(String.valueOf(calculateAverageSpeedAllCars(continuousSpace)));

		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
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
