public class Test {

    public static void main(String[] args) {
        CSVReader.read(args);
        SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing();
        for (int i = 0; i < 15; i++) {
            simulatedAnnealing.start();
        }
    }
}
