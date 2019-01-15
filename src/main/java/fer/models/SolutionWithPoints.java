package fer.models;

public class SolutionWithPoints {

    private Solution solution;
    private SolutionPoints solutionPoints;

    public SolutionWithPoints(Solution solution) {
        this.solution = new Solution(solution);
        this.solutionPoints = SolutionPoints.calculateSolutionPoints(solution);
    }

    public SolutionWithPoints(Solution solution, SolutionPoints solutionPoints) {
        this.solution = new Solution(solution);
        this.solutionPoints = solutionPoints;
    }

    public Solution getSolution() {
        return solution;
    }

    public SolutionPoints getSolutionPoints() {
        return solutionPoints;
    }
}
