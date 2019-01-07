import models.Group;
import models.Student;
import models.StudentActivity;
import store.ActivityStore;
import store.GroupStore;
import store.StudentActivityStore;
import store.StudentStore;

import java.util.List;
import java.util.Random;
import java.util.*;
import java.util.stream.Collectors;

public class SimulatedAnnealing {

    // key is in format studentId:activityId
    // value is group ID
    private Map<String, Long> bestSolution = new HashMap<>();
    private Long bestSolutionValue = 0L;
    private Long numberOfIterations = 0L;
    private final Long numOfIterationsForReport = 100000L;

    public void start() {

        TimeoutTimer timeoutTimer = new TimeoutTimer(ProblemParameters.timeout);

        while (!timeoutTimer.isFinished()) {
            randomizeSolution();
            numberOfIterations++;
            Long totalPoints = numberOfIterations % numOfIterationsForReport == 0 ?
                    calculateSolutionPoints(true) : calculateSolutionPoints(false);
            if (totalPoints > bestSolutionValue) {
                memorizeBestSolution(totalPoints);
            }
        }

        System.out.println("Best solution is: " + bestSolutionValue);
        System.out.println("Iterations number: " + numberOfIterations);
        printBestSolutionMap();
    }

    // randomize solution and put it into stores
    private void randomizeSolution() {
        // iterate through activities
        ActivityStore.activities.forEach(activity -> {
            // for each student activity find possible groups to select and do it randomly
            List<StudentActivity> studentActivities = activity.getStudentIds().stream()
                    .map(studentId -> StudentActivityStore.getStudentActivity(studentId, activity.getId()))
                    .filter(StudentActivity::hasRequest)
                    .collect(Collectors.toList());
            for (StudentActivity studentActivity : studentActivities) {

                // if current studentActivity group is at min limit (hard constraint) - skip
                if (GroupStore.groupMap.get(studentActivity.getSelectedGroupId()).isAtMinNumOfStudents()) {
                    continue;
                }

                List<Long> allCurrentGroupsIdsOfStudent =
                        StudentStore.getStudentActivitiesOfStudent(studentActivity.getStudentId()).stream()
                                .map(StudentActivity::getSelectedGroupId)
                                .collect(Collectors.toList());
                // all groups for student activity which can be selected (including currently selected)
                List<Long> possibleGroupIdsToSelect = studentActivity.getPossibleGroupIds().stream()
                        .map(groupId -> GroupStore.groupMap.get(groupId))
                        // if group is not selected one it should not be full (can't move to other group)
                        .filter(group -> group.getId().equals(studentActivity.getSelectedGroupId()) || !group.isFull())
                        // should not have overlap with other student group
                        .filter(group -> !hasIntersection(group.getOverlapGroupIds(), allCurrentGroupsIdsOfStudent))
                        .map(Group::getId)
                        .collect(Collectors.toList());

                // randomize group selection
                Long newRandomSelectedGroupId = randomItemFromList(possibleGroupIdsToSelect);
                studentActivity.selectNewGroup(newRandomSelectedGroupId);
            }

        });
    }

    private Long calculateSolutionPoints(boolean showReport) {
        // calculating points
        Long pointsA, pointsB, pointsC, pointsD, pointsE;
        pointsA = pointsB = pointsC = pointsD = pointsE = 0l;
        for (Student student : StudentStore.studentMap.values()) {
            List<StudentActivity> studentActivitiesWithRequest =
                    StudentStore.getStudentActivitiesOfStudent(student.getId()).stream()
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
        for (Group group : GroupStore.groupMap.values()) {
            if (group.getStudentCount() < group.getMinPreferred()) {
                pointsD += (group.getMinPreferred() - group.getStudentCount()) * ProblemParameters.minMaxPenalty;
            }
            if (group.getStudentCount() > group.getMaxPreferred()) {
                pointsE += (group.getStudentCount() - group.getMaxPreferred()) * ProblemParameters.minMaxPenalty;
            }
        }

        Long totalPoints = pointsA + pointsB + pointsC - pointsD - pointsE;

        if (showReport) {
            System.out.println("A=" + pointsA + " B=" + pointsB + " C=" + pointsC + " D=" + pointsD + " E=" + pointsE);
            System.out.println("total: " + totalPoints);
            System.out.println("+++++++++++++++++++++");
        }
        return totalPoints;
    }

    // if two lists have intersection items
    private boolean hasIntersection(List<Long> l1, List<Long> l2) {
        for (Long item : l1) {
            if (l2.contains(item)) {
                return true;
            }
        }
        return false;
    }

    private Long randomItemFromList(List<Long> list) {
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }

    private Long calculateAwardActivityPoints(Integer numOfActivitiesSolved) {
        if (numOfActivitiesSolved == 0) {
            return 0L;
        }
        if (numOfActivitiesSolved > ProblemParameters.awardActivities.size()) {
            return ProblemParameters.awardActivities.get(ProblemParameters.awardActivities.size() - 1);
        }
        return ProblemParameters.awardActivities.get(numOfActivitiesSolved - 1);
    }

    private void memorizeBestSolution(long totalPoints) {
        bestSolution = StudentActivityStore.studentActivityMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().getSelectedGroupId()));
        bestSolutionValue = totalPoints;
    }

    private void printBestSolutionMap() {
        for (Map.Entry<String, Long> entry : bestSolution.entrySet())  {
            System.out.println(entry.getKey() + " / " + entry.getValue());
        }
    }

}
