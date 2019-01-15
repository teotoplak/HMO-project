package fer;

import fer.models.Solution;

public class Test {

    public static void main(String[] args) {
        Solution initSolution = CSVReader.read(args);
        SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing();
        simulatedAnnealing.start(initSolution);
        CSVReader.write(simulatedAnnealing.getBestSolution().getStudentActivityMap());
        System.exit(0);
    }
}
