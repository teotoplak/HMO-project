package fer.models;

import fer.ProblemParameters;
import fer.store.StudentStore;

import java.util.List;
import java.util.stream.Collectors;

public class SolutionPoints {

    public Long pointsA, pointsB, pointsC, pointsD, pointsE, totalPoints;

    public SolutionPoints(Long pointsA, Long pointsB, Long pointsC, Long pointsD, Long pointsE, Long totalPoints) {
        this.pointsA = pointsA;
        this.pointsB = pointsB;
        this.pointsC = pointsC;
        this.pointsD = pointsD;
        this.pointsE = pointsE;
        this.totalPoints = totalPoints;
    }

    public static SolutionPoints calculateSolutionPoints(Solution solution) {
        // calculating points
        Long pointsA, pointsB, pointsC, pointsD, pointsE;
        pointsA = pointsB = pointsC = pointsD = pointsE = 0L;
        for (Student student : StudentStore.studentMap.values()) {
            List<StudentActivity> studentActivitiesWithRequest =
                    StudentStore.getStudentActivitiesOfStudent(solution, student.getId()).stream()
                            .filter(StudentActivity::hasRequest)
                            .collect(Collectors.toList());
            Integer countOfSolvedActivities = 0;
            for (StudentActivity studentActivity : studentActivitiesWithRequest) {
                if (studentActivity.isChangedFromInitial()) {
                    pointsA += studentActivity.getSwapWeight();
                    countOfSolvedActivities++;
                }
            }
            pointsB += calculateAwardActivityPoints(countOfSolvedActivities);
            if (countOfSolvedActivities.equals(studentActivitiesWithRequest.size())) {
                pointsC += ProblemParameters.awardStudent;
            }
        }
        for (Group group : solution.getGroupMap().values()) {
            if (group.getStudentCount() < group.getMinPreferred()) {
                pointsD += (group.getMinPreferred() - group.getStudentCount()) * ProblemParameters.minMaxPenalty;
            }
            if (group.getStudentCount() > group.getMaxPreferred()) {
                pointsE += (group.getStudentCount() - group.getMaxPreferred()) * ProblemParameters.minMaxPenalty;
            }
        }

        Long totalPoints = pointsA + pointsB + pointsC - pointsD - pointsE;

        return new SolutionPoints(pointsA, pointsB, pointsC, pointsD, pointsE, totalPoints);
    }

    private static Long calculateAwardActivityPoints(Integer numOfActivitiesSolved) {
        if (numOfActivitiesSolved == 0) {
            return 0L;
        }
        if (numOfActivitiesSolved > ProblemParameters.awardActivities.size()) {
            return ProblemParameters.awardActivities.get(ProblemParameters.awardActivities.size() - 1);
        }
        return ProblemParameters.awardActivities.get(numOfActivitiesSolved - 1);
    }
}
