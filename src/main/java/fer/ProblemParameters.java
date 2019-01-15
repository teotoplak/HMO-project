package fer;

import java.util.ArrayList;
import java.util.List;

public class ProblemParameters {

    public static List<Long> awardActivities = new ArrayList<>();
    public static Long awardStudent = 0L;
    public static Long minMaxPenalty = 0L;
    public static Long timeout = 0L;

    public static final long reportIntervalPeriodInSeconds = 5L;
    public static final Double differenceBetweenNeighbours = 0.02;

    /**
     * Simulated annealing heuristic params
     */
    /*
        for calculated avg deltaF = 38.7 (average points diff between random neighbours):
        acceptance 70% temp = 110
        acceptance 50% temp = 50
     */
    public static final Double intialTemperature = 100.0;
    public static final Double finalTemperature = 0.01;
    public static Double coeficientForTemperatureReduction = null;
    public static Double expectedIterationsPerSecond = 70D;

    // using geometric function of temp reduction
    public static Double temperatureUpdateFunction(Double temperature) {
        return temperature * coeficientForTemperatureReduction;
    }

    /**
     * this function calculates geometric reduction coefficient in way that
     * temperature approximately reaches final temperature at the same time as timeout happens
     *
     * you have to know average num of iterations for given timeout
     *
     * todo change this so that the user doesn't have to call this method before algorithm starts
     */
    public static void calculateReductionCoefficientBasedOnAproxIterationNum() {
        coeficientForTemperatureReduction =
                Math.pow(Math.E, (Math.log10(finalTemperature) - Math.log10(intialTemperature))
                        / (expectedIterationsPerSecond * timeout));
    }

}
