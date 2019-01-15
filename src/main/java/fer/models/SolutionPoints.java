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

    public static Long calculatePointsDifferenceOfGroupSwap(StudentActivity studentActivity, Long newGroupId, Solution currentSolution) {

        if (!studentActivity.getPossibleGroupIds().contains(newGroupId)) {
            System.err.println("Mistake!!");
        }

        Long pointsDiff = 0L;

        if (studentActivity.getSelectedGroupId().equals(newGroupId)) {
            return 0L;
        }

        // points A
        boolean solvedGood = false;
        boolean solvedBad = false;

        if (studentActivity.getInitialGroupId().equals(newGroupId)) {
            pointsDiff -= studentActivity.getSwapWeight();
            solvedBad = true;
        }
        if (!studentActivity.isChangedFromInitial()) {
            pointsDiff += studentActivity.getSwapWeight();
            solvedGood = true;
        }

        // points B
        List<StudentActivity> studentActivitiesWithRequest =
                StudentStore.getStudentActivitiesOfStudent(currentSolution, studentActivity.getStudentId()).stream()
                        .filter(StudentActivity::hasRequest)
                        .collect(Collectors.toList());
        Integer countOfSolvedActivities = 0;
        for (StudentActivity sa : studentActivitiesWithRequest) {
            if (sa.isChangedFromInitial()) {
                countOfSolvedActivities++;
            }
        }
        Long currPoints = calculateAwardActivityPoints(countOfSolvedActivities);
        if (solvedGood) {
            pointsDiff += calculateAwardActivityPoints(countOfSolvedActivities + 1) - currPoints;
        }
        if (solvedBad) {
            pointsDiff -= currPoints - calculateAwardActivityPoints(countOfSolvedActivities - 1);
        }

        // points C
        if (solvedGood && studentActivitiesWithRequest.size() == countOfSolvedActivities + 1) {
            pointsDiff += ProblemParameters.awardStudent;
        }
        if (solvedBad && studentActivitiesWithRequest.size() == countOfSolvedActivities) {
            pointsDiff -= ProblemParameters.awardStudent;
        }

        // points D
        Group addingGroup = currentSolution.getGroupMap().get(newGroupId);
        if (!addingGroup.isInPrefferedNumberWithCount(addingGroup.getStudentCount() + 1)
                && addingGroup.isInPrefferedNumberWithCount(addingGroup.getStudentCount())) {
            pointsDiff -= ProblemParameters.minMaxPenalty;
        }
        if (addingGroup.isInPrefferedNumberWithCount(addingGroup.getStudentCount() + 1)
                && !addingGroup.isInPrefferedNumberWithCount(addingGroup.getStudentCount())) {
            pointsDiff += ProblemParameters.minMaxPenalty;
        }

        // points E
        Group removingGroup = currentSolution.getGroupMap().get(studentActivity.getSelectedGroupId());
        if (!removingGroup.isInPrefferedNumberWithCount(removingGroup.getStudentCount() - 1)
                && removingGroup.isInPrefferedNumberWithCount(removingGroup.getStudentCount())) {
            pointsDiff -= ProblemParameters.minMaxPenalty;
        }
        if (removingGroup.isInPrefferedNumberWithCount(removingGroup.getStudentCount() - 1)
                && !removingGroup.isInPrefferedNumberWithCount(removingGroup.getStudentCount())) {
            pointsDiff += ProblemParameters.minMaxPenalty;
        }

        return pointsDiff;
    }
}
