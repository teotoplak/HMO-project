import models.Group;
import models.Student;
import models.StudentActivity;
import store.ActivityStore;
import store.GroupStore;
import store.StudentActivityStore;
import store.StudentStore;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimulatedAnnealing {

    private BestSolution bestSolution;
    private BestSolution reallyBestSolution;
    private Long numberOfIterations = 0L;
    private boolean showReportInNextIteration = false;
    private int numOfShowedReport = 0;


    public BestSolution getReallyBestSolution() {
        return reallyBestSolution;
    }

    private Double currentTemperature = ProblemParameters.intialTemperature;

    public void start() {

        ProblemParameters.calculateReductionCoefficientBasedOnAproxIterationNum();
        System.out.println("Coefficient for reduction calculated: " + ProblemParameters.coeficientForTemperatureReduction);

        TimeoutTimer timeoutTimer = new TimeoutTimer(
                ProblemParameters.timeout, getTimerTaskForReports(), ProblemParameters.reportIntervalPeriodInSeconds);

        // initial solution
        SolutionPoints initialSolutionPoints = calculateSolutionPoints();
        System.out.println("Starting points: ");
        printSolutionPoints(initialSolutionPoints);
        memorizeBestSolution(initialSolutionPoints);
        memorizeReallyBestSolution(initialSolutionPoints);


        Random random = new Random();
        // iterate until timeout time or final temperature
        while (!timeoutTimer.isFinished() && currentTemperature > ProblemParameters.finalTemperature) {

            if(((double)numOfShowedReport*ProblemParameters.reportIntervalPeriodInSeconds)/(double)ProblemParameters.timeout < random.nextDouble()) {
                randomizeSolution();
            } else {
                intensifySolution();
            }
            numberOfIterations++;

            SolutionPoints currentSolutionPoints = calculateSolutionPoints();

            if (currentSolutionPoints.totalPoints > bestSolution.solutionPoints.totalPoints) {
                if (currentSolutionPoints.totalPoints > reallyBestSolution.solutionPoints.totalPoints) {
                    memorizeReallyBestSolution(currentSolutionPoints);
                }
                memorizeBestSolution(currentSolutionPoints);
            } else if (currentSolutionPoints.totalPoints < bestSolution.solutionPoints.totalPoints
                    && simulatedAnnealingCriterion(currentSolutionPoints.totalPoints, bestSolution.solutionPoints.totalPoints)) {
                memorizeBestSolution(currentSolutionPoints);
            } else {
                // return best solution to current
                StudentActivityStore.studentActivityMap = bestSolution.getStudentActivityMap();
                GroupStore.groupMap = bestSolution.getGroupMap();
            }

            currentTemperature = ProblemParameters.temperatureUpdateFunction(currentTemperature);

            if (showReportInNextIteration) {
                System.out.println("+++++++++++++++++++++");
                printSolutionPoints(bestSolution.solutionPoints);
                printSolutionPoints(reallyBestSolution.solutionPoints);
                System.out.println("Iterations number: " + numberOfIterations);
                System.out.println("Temperature: " + currentTemperature);
                showReportInNextIteration = false;
                numOfShowedReport++;
                calculateNewExpectedIterations();
            }
        }

        System.out.println("Best solution is: " + reallyBestSolution.solutionPoints.totalPoints);
        System.out.println("Iterations number: " + numberOfIterations);
        System.out.println("End temperature: " + currentTemperature
                + " (final threshold:) " + ProblemParameters.finalTemperature);
        // printBestSolutionMap();
    }

    private void calculateNewExpectedIterations() {
        ProblemParameters.expectedIterationsPerSecond = Math.ceil(numberOfIterations / (numOfShowedReport * ProblemParameters.reportIntervalPeriodInSeconds));
        ProblemParameters.calculateReductionCoefficientBasedOnAproxIterationNum();
    }

    private void intensifySolution() {
        Random random = new Random();
        // iterate through activities
        ActivityStore.activities.forEach(activity -> {

            List<StudentActivity> studentActivities = activity.getStudentIds().stream()
                    .map(studentId -> bestSolution.getStudentActivityMap().get(studentId + ":" + activity.getId()))
                    .filter(StudentActivity::hasRequest)
                    .filter(studentActivity -> (!studentActivity.isChangedFromInitial() && studentActivity.hasRequest()) || random.nextDouble() < ProblemParameters.differenceBetweenNeighbours)
                    .sorted((o1, o2) -> {
                        int a = o2.getSwapWeight().intValue() - o1.getSwapWeight().intValue();
                        if (a != 0) {
                            return a;
                        }
                        return (GroupStore.groupMap.get(o2.getSelectedGroupId()).getStudentCount().intValue() - GroupStore.groupMap.get(o2.getSelectedGroupId()).getMaxPreferred().intValue()) -
                                (GroupStore.groupMap.get(o1.getSelectedGroupId()).getStudentCount().intValue() - GroupStore.groupMap.get(o1.getSelectedGroupId()).getMaxPreferred().intValue());
                    })

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
                List<Long> possibleGroupIdsToSelect = getPossibleGroupsToSelectForStudentActivity(studentActivity)
                        // should not have overlap with other student group
                        .filter(group -> !hasIntersection(group.getOverlapGroupIds(), allCurrentGroupsIdsOfStudent))
                        .map(Group::getId)
                        .collect(Collectors.toList());

                // if student has overlaps in all options select overlap at random
                if (possibleGroupIdsToSelect.size() == 0) {
                    possibleGroupIdsToSelect = getPossibleGroupsToSelectForStudentActivity(studentActivity)
                            .map(Group::getId)
                            .collect(Collectors.toList());
                }

                //  group selection
                Long newSelectedGroupId = 0L;

                // greedy pick group
                for (Long group : possibleGroupIdsToSelect) {
                    if (GroupStore.groupMap.get(group).noPrefferedOverflowIfStudentAdded()
                            && !group.equals(studentActivity.getInitialGroupId())) {
                        newSelectedGroupId = group;
                        break;
                    }
                }

                // if group is not found in previous block of code
                if(newSelectedGroupId.equals(0L)) {
                    // ensure that switching groups happens if a user had requested a swap
                    if (possibleGroupIdsToSelect.size() > 1) {
                        possibleGroupIdsToSelect.remove(studentActivity.getInitialGroupId());
                    }
                    newSelectedGroupId = randomItemFromList(possibleGroupIdsToSelect);
                }
                studentActivity.selectNewGroup(newSelectedGroupId);
            }
        });
    }

    // randomize solution and put it into stores
    private void randomizeSolution() {
        // iterate through activities
        Random random = new Random();
        ActivityStore.activities.forEach(activity -> {
            // for each student activity find possible groups to select and do it randomly
            List<StudentActivity> studentActivities = activity.getStudentIds().stream()
                    .map(studentId -> StudentActivityStore.getStudentActivity(studentId, activity.getId()))
                    .filter(StudentActivity::hasRequest)
                    .filter(studentActivity -> random.nextDouble() < 5*ProblemParameters.differenceBetweenNeighbours)
                    .collect(Collectors.toList());

            Collections.shuffle(studentActivities);
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
                List<Long> possibleGroupIdsToSelect = getPossibleGroupsToSelectForStudentActivity(studentActivity)
                        // should not have overlap with other student group
                        .filter(group -> !hasIntersection(group.getOverlapGroupIds(), allCurrentGroupsIdsOfStudent))
                        .map(Group::getId)
                        .collect(Collectors.toList());

                // if student has overlaps in all options select overlap at random
                if (possibleGroupIdsToSelect.size() == 0) {
                    possibleGroupIdsToSelect = getPossibleGroupsToSelectForStudentActivity(studentActivity)
                            .map(Group::getId)
                            .collect(Collectors.toList());
                }
                Long newRandomSelectedGroupId = 0L;

                if (random.nextDouble() > 0.5) {
                    // try to find most appropriate group to add to
                    for (Long group : possibleGroupIdsToSelect) {
                        if (GroupStore.groupMap.get(group).noPrefferedOverflowIfStudentAdded()
                                && !group.equals(studentActivity.getInitialGroupId())) {
                            newRandomSelectedGroupId = group;
                            break;
                        }
                    }
                }

                // if group is not found in previous block of code
                if(newRandomSelectedGroupId.equals(0L)) {
                    newRandomSelectedGroupId = randomItemFromList(possibleGroupIdsToSelect);
                }

                // randomize group selection
                studentActivity.selectNewGroup(newRandomSelectedGroupId);
            }
        });
    }

    // filters option if group is full
    private Stream<Group> getPossibleGroupsToSelectForStudentActivity(StudentActivity studentActivity) {
        return studentActivity.getPossibleGroupIds().stream()
                .map(groupId -> GroupStore.groupMap.get(groupId))
                // if group is not selected one it should not be full (can't move to other group)
                .filter(group -> group.getId().equals(studentActivity.getSelectedGroupId()) || !group.isFull());
    }

    // chance of accepting bad solutions
    private boolean simulatedAnnealingCriterion(long currentSolutionValue, long bestSolutionValue) {
        if (currentTemperature == 0L) {
            return false;
        }
        long deltaF = Math.abs(currentSolutionValue - bestSolutionValue);
        double chanceOfAccepting = Math.pow(Math.E, (-1) * deltaF / currentTemperature);
        Random generator = new Random();
        double generatedNum = generator.nextDouble();
        return generatedNum < chanceOfAccepting;
    }

    private SolutionPoints calculateSolutionPoints() {
        // calculating points
        Long pointsA, pointsB, pointsC, pointsD, pointsE;
        pointsA = pointsB = pointsC = pointsD = pointsE = 0L;
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

        return new SolutionPoints(pointsA, pointsB, pointsC, pointsD, pointsE, totalPoints);
    }

    private void printSolutionPoints(SolutionPoints solutionPoints) {

        System.out.println("A=" + solutionPoints.pointsA
                + " B=" + solutionPoints.pointsB
                + " C=" + solutionPoints.pointsC
                + " D=" + solutionPoints.pointsD
                + " E=" + solutionPoints.pointsE);
        System.out.println("total: " + solutionPoints.totalPoints);
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

    private void memorizeBestSolution(SolutionPoints totalPoints) {
        bestSolution = new BestSolution(GroupStore.groupMap, StudentActivityStore.studentActivityMap, totalPoints);
    }

    private void memorizeReallyBestSolution(SolutionPoints totalPoints) {
        reallyBestSolution = new BestSolution(GroupStore.groupMap, StudentActivityStore.studentActivityMap, totalPoints);
    }

    private void printBestSolutionMap() {
        Map<String, Long> activityMapWithPoints = bestSolution.getStudentActivityMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().getSelectedGroupId()));
        for (Map.Entry<String, Long> entry : activityMapWithPoints.entrySet())  {
            System.out.println(entry.getKey() + " / " + entry.getValue());
        }
    }

    private TimerTask getTimerTaskForReports() {
        return new TimerTask() {
            public void run() {
                showReportInNextIteration = true;
            }
        };
    }

    public class BestSolution {

        private Map<Long, Group> groupMap;
        private Map<String, StudentActivity> studentActivityMap;
        private SolutionPoints solutionPoints;

        // cloning maps
        public BestSolution(Map<Long, Group> groupMap, Map<String, StudentActivity> studentActivityMap, SolutionPoints solutionPoints) {
            this.groupMap = new HashMap<>(groupMap);
            this.studentActivityMap = new HashMap<>(studentActivityMap);
            this.solutionPoints = solutionPoints;
        }

        public Map<Long, Group> getGroupMap() {
            return groupMap;
        }

        public Map<String, StudentActivity> getStudentActivityMap() {
            return studentActivityMap;
        }

        public SolutionPoints getSolutionPoints() {
            return solutionPoints;
        }
    }


    private class SolutionPoints {
        Long pointsA, pointsB, pointsC, pointsD, pointsE, totalPoints;
        public SolutionPoints(Long pointsA, Long pointsB, Long pointsC, Long pointsD, Long pointsE, Long totalPoints) {
            this.pointsA = pointsA;
            this.pointsB = pointsB;
            this.pointsC = pointsC;
            this.pointsD = pointsD;
            this.pointsE = pointsE;
            this.totalPoints = totalPoints;
        }
    }
}
