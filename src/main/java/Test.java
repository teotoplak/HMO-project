public class Test {

    public static void main(String[] args) {
        CSVReader.read(args);
        SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing();
        simulatedAnnealing.start();
        CSVReader.write(simulatedAnnealing.getReallyBestSolution().getStudentActivityMap());
    }
}
