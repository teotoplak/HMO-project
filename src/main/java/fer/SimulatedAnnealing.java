package fer;

import fer.models.*;
import fer.store.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimulatedAnnealing {

    private SolutionWithPoints bestCurrentSolution;
    private SolutionWithPoints bestTotalSolution;
    private Long numberOfIterations = 0L;
    private boolean showReportInNextIteration = false;
    private int numOfShowedReport = 0;
    private Random random = new Random();

    private Double currentTemperature = ProblemParameters.intialTemperature;

    public void start(Solution initSolution) {

        ProblemParameters.calculateReductionCoefficientBasedOnAproxIterationNum();
        System.out.println("Coefficient for reduction calculated: " + ProblemParameters.coeficientForTemperatureReduction);

        TimeoutTimer timeoutTimer = new TimeoutTimer(
                ProblemParameters.timeout, getTimerTaskForReports(), ProblemParameters.reportIntervalPeriodInSeconds);

        Solution currentSolution = new Solution(initSolution);

        System.out.println("Starting points: ");
        printSolutionPoints(SolutionPoints.calculateSolutionPoints(currentSolution));

        // greedy first
        currentSolution = greedyGroupExchange(currentSolution);
        System.out.println("Points after greedy: " + calculateTotalPointsOfSolution(currentSolution));

        bestTotalSolution = new SolutionWithPoints(currentSolution);
        bestCurrentSolution = new SolutionWithPoints(currentSolution);

        // iterate until timeout time or final temperature
        while (!timeoutTimer.isFinished() && currentTemperature > ProblemParameters.finalTemperature) {

            currentSolution =
                    ((double) numOfShowedReport * ProblemParameters.reportIntervalPeriodInSeconds)
                            / (double) ProblemParameters.timeout < random.nextDouble() ?
                            randomizeSolution(currentSolution) : intensifySolution(currentSolution);
            numberOfIterations++;

            SolutionPoints currentSolutionPoints = SolutionPoints.calculateSolutionPoints(currentSolution);

            if (currentSolutionPoints.totalPoints > bestCurrentSolution.getSolutionPoints().totalPoints) {
                if (currentSolutionPoints.totalPoints > bestTotalSolution.getSolutionPoints().totalPoints) {
                    bestTotalSolution = new SolutionWithPoints(currentSolution, currentSolutionPoints);
                }
                bestCurrentSolution = new SolutionWithPoints(currentSolution, currentSolutionPoints);
            } else if (currentSolutionPoints.totalPoints < bestCurrentSolution.getSolutionPoints().totalPoints
                    && simulatedAnnealingCriterion(currentSolutionPoints.totalPoints, bestCurrentSolution.getSolutionPoints().totalPoints)) {
                bestCurrentSolution = new SolutionWithPoints(currentSolution, currentSolutionPoints);
            } else {
                // return best solution to current
                currentSolution = new Solution(bestCurrentSolution.getSolution());
            }

            currentTemperature = ProblemParameters.temperatureUpdateFunction(currentTemperature);

            if (showReportInNextIteration) {
                System.out.println("+++++++++++++++++++++");
                System.out.println("Best Current Solution: ");
                printSolutionPoints(bestCurrentSolution.getSolutionPoints());
                System.out.println("Best Total Solution: ");
                printSolutionPoints(bestTotalSolution.getSolutionPoints());
                System.out.println("Iterations number: " + numberOfIterations);
                System.out.println("Temperature: " + currentTemperature);
                showReportInNextIteration = false;
                numOfShowedReport++;
                ProblemParameters.calculateNewExpectedIterations(numberOfIterations, numOfShowedReport);
            }
        }

        System.out.println("Best solution is: " + bestTotalSolution.getSolutionPoints().totalPoints);
        System.out.println("Iterations number: " + numberOfIterations);
        System.out.println("End temperature: " + currentTemperature
                + " (final threshold:) " + ProblemParameters.finalTemperature);
        // printBestSolutionMap();
    }


    private Solution intensifySolution(Solution currentSolution) {

        // iterate through activities
        ActivityStore.activities.forEach(activity -> {
            List<StudentActivity> studentActivities = activity.getStudentIds().stream()
                    .map(studentId -> currentSolution.getStudentActivityMap().get(studentId + ":" + activity.getId()))
                    .filter(studentActivity -> (!studentActivity.isChangedFromInitial() &&
                            studentActivity.hasRequest()) ||
                            random.nextDouble() < ProblemParameters.differenceBetweenNeighbours)
                    .sorted((o1, o2) -> {
                        int a = o2.getSwapWeight().intValue() - o1.getSwapWeight().intValue();
                        if (a != 0) {
                            return a;
                        }
                        return (currentSolution.getGroupMap().get(o2.getSelectedGroupId()).getStudentCount().intValue() -
                                currentSolution.getGroupMap().get(o2.getSelectedGroupId()).getMaxPreferred().intValue()) -
                                (currentSolution.getGroupMap().get(o1.getSelectedGroupId()).getStudentCount().intValue() -
                                 currentSolution.getGroupMap().get(o1.getSelectedGroupId()).getMaxPreferred().intValue());
                    })
                    .collect(Collectors.toList());

            for (StudentActivity studentActivity : studentActivities) {

                // if current studentActivity group is at min limit (hard constraint) - skip
                if (currentSolution.getGroupMap().get(studentActivity.getSelectedGroupId()).isAtMinNumOfStudents()) {
                    continue;
                }

                List<Long> allCurrentGroupsIdsOfStudent =
                        StudentStore.getStudentActivitiesOfStudent(currentSolution, studentActivity.getStudentId()).stream()
                                .map(StudentActivity::getSelectedGroupId)
                                .collect(Collectors.toList());
                // all groups for student activity which can be selected (including currently selected)
                List<Long> possibleGroupIdsToSelect = getPossibleGroupsToSelectForStudentActivity(currentSolution, studentActivity)
                        // should not have overlap with other student group
                        .filter(group -> !hasIntersection(group.getOverlapGroupIds(), allCurrentGroupsIdsOfStudent))
                        .map(Group::getId)
                        .collect(Collectors.toList());

                // if student has overlaps in all options select overlap at random
                if (possibleGroupIdsToSelect.size() == 0) {
                    possibleGroupIdsToSelect = getPossibleGroupsToSelectForStudentActivity(currentSolution, studentActivity)
                            .map(Group::getId)
                            .collect(Collectors.toList());
                }

                //  group selection
                Long newSelectedGroupId = 0L;

                // ensure that switching groups happens if a user had requested a swap
                if (possibleGroupIdsToSelect.size() > 1) {
                    possibleGroupIdsToSelect.remove(studentActivity.getInitialGroupId());
                }

                // greedy pick group
                for (Long group : possibleGroupIdsToSelect) {
                    if (currentSolution.getGroupMap().get(group).noPrefferedOverflowIfStudentAdded()) {
                        newSelectedGroupId = group;
                        break;
                    }
                }

                // if group is not found in previous block of code
                if(newSelectedGroupId.equals(0L)) {
                    newSelectedGroupId = randomItemFromList(possibleGroupIdsToSelect);
                }
                studentActivity.selectNewGroup(currentSolution.getGroupMap(), newSelectedGroupId);
            }
        });

        return currentSolution;
    }

    // randomize solution and put it into stores
    private Solution randomizeSolution(Solution currentSolution) {

        // iterate through activities
        ActivityStore.activities.forEach(activity -> {
            // for each student activity find possible groups to select and do it randomly
            List<StudentActivity> studentActivities = activity.getStudentIds().stream()
                    .map(studentId -> currentSolution.getStudentActivity(studentId, activity.getId()))
                    .filter(StudentActivity::hasRequest)
                    .filter(studentActivity -> random.nextDouble() < ProblemParameters.differenceBetweenNeighbours)
                    .collect(Collectors.toList());

            Collections.shuffle(studentActivities);
            for (StudentActivity studentActivity : studentActivities) {

                // if current studentActivity group is at min limit (hard constraint) - skip
                if (currentSolution.getGroupMap().get(studentActivity.getSelectedGroupId()).isAtMinNumOfStudents()) {
                    continue;
                }

                List<Long> allCurrentGroupsIdsOfStudent =
                        StudentStore.getStudentActivitiesOfStudent(currentSolution, studentActivity.getStudentId()).stream()
                                .map(StudentActivity::getSelectedGroupId)
                                .collect(Collectors.toList());
                // all groups for student activity which can be selected (including currently selected)
                List<Long> possibleGroupIdsToSelect = getPossibleGroupsToSelectForStudentActivity(currentSolution, studentActivity)
                        // should not have overlap with other student group
                        .filter(group -> !hasIntersection(group.getOverlapGroupIds(), allCurrentGroupsIdsOfStudent))
                        .map(Group::getId)
                        .collect(Collectors.toList());

                // if student has overlaps in all options select overlap at random
                if (possibleGroupIdsToSelect.size() == 0) {
                    possibleGroupIdsToSelect = getPossibleGroupsToSelectForStudentActivity(currentSolution, studentActivity)
                            .map(Group::getId)
                            .collect(Collectors.toList());
                }
                Long newRandomSelectedGroupId = 0L;

                if (random.nextDouble() > 0.5) {
                    // try to find most appropriate group to add to
                    for (Long group : possibleGroupIdsToSelect) {
                        if (currentSolution.getGroupMap().get(group).noPrefferedOverflowIfStudentAdded()
                                && !group.equals(studentActivity.getInitialGroupId())) {
                            newRandomSelectedGroupId = group;
                            break;
                        }
                    }
                }

                // if group is not found in previous block of code
                if(newRandomSelectedGroupId.equals(0L)) {
                    // randomize group selection
                    newRandomSelectedGroupId = randomItemFromList(possibleGroupIdsToSelect);
                }

                // select new group
                studentActivity.selectNewGroup(currentSolution.getGroupMap(), newRandomSelectedGroupId);
            }
        });

        return currentSolution;
    }

    private Solution greedyGroupExchange(Solution solution) {

        Long previousSolutionPoints = calculateTotalPointsOfSolution(solution);

        // loop until no better solution is gained
        while(true) {

            // iterate through activities
            ActivityStore.activities.forEach(activity -> {

                List<StudentActivity> studentActivitiesNotChangedFromInitial = activity.getStudentIds().stream()
                        .map(studentId -> solution.getStudentActivity(studentId, activity.getId()))
                        .filter(StudentActivity::hasRequest)
                        .filter(studentActivity -> !studentActivity.isChangedFromInitial())
                        // sort by amount of requests
                        .sorted(((o1, o2) -> {
                            Integer first = o1.getPossibleGroupIds().size();
                            Integer second = o2.getPossibleGroupIds().size();
                            return first.compareTo(second);
                        }))
                        .collect(Collectors.toList());

                // key init groupId, values wanted possible groups to select
                List<SAAndRequests> requests = new LinkedList<>();

                for (StudentActivity studentActivity : studentActivitiesNotChangedFromInitial) {

                    List<Long> allCurrentGroupsIdsOfStudent =
                            StudentStore.getStudentActivitiesOfStudent(solution, studentActivity.getStudentId()).stream()
                                    .map(StudentActivity::getSelectedGroupId)
                                    .collect(Collectors.toList());

                    // all groups for student activity which can be selected (including currently selected)
                    List<Long> possibleGroupIdsToSelect = studentActivity.getPossibleGroupIds().stream()
                            // filter initial group
                            .filter(groupId -> !groupId.equals(studentActivity.getInitialGroupId()))
                            .map(groupId -> solution.getGroupMap().get(groupId))
                            // should not have overlap with other student group
                            .filter(group -> !hasIntersection(group.getOverlapGroupIds(), allCurrentGroupsIdsOfStudent))
                            .map(Group::getId)
                            .collect(Collectors.toList());

                    requests.add(new SAAndRequests(studentActivity, possibleGroupIdsToSelect, studentActivity.getSelectedGroupId()));

                }

                for (SAAndRequests request : requests) {
                    if (request.isSolved()) {
                        continue;
                    }
                    for (Long groupId : request.getRequests()) {
                        // try to find matching request
                        for (SAAndRequests possibleMatch : requests.stream()
                                .filter(saAndRequests -> !saAndRequests.isSolved())
                                .filter(saAndRequests -> !saAndRequests.equals(request))
                                .collect(Collectors.toList())) {
                            if (possibleMatch.getSelectedGroupId().equals(groupId)
                                    && possibleMatch.getRequests().contains(request.getSelectedGroupId())) {
                                // change solution
                                request.getStudentActivity().selectNewGroup(solution.getGroupMap(), groupId);
                                possibleMatch.getStudentActivity().selectNewGroup(solution.getGroupMap(), request.getSelectedGroupId());
                                request.solved();
                                possibleMatch.solved();
                                break;
                            }
                        }
                        if (request.isSolved()) {
                            break;
                        }
                    }
                }


            });

            Long newSolutionPoints = calculateTotalPointsOfSolution(solution);
            if (newSolutionPoints.equals(previousSolutionPoints)) {
                break;
            }
            previousSolutionPoints = newSolutionPoints;

        }

        return solution;
    }

    // filters option if group is full
    private Stream<Group> getPossibleGroupsToSelectForStudentActivity(Solution solution, StudentActivity studentActivity) {
        return studentActivity.getPossibleGroupIds().stream()
                .map(groupId -> solution.getGroupMap().get(groupId))
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
        return list.get(random.nextInt(list.size()));
    }

    private TimerTask getTimerTaskForReports() {
        return new TimerTask() {
            public void run() {
                showReportInNextIteration = true;
            }
        };
    }

    public Solution getBestSolution() {
        return bestTotalSolution.getSolution();
    }

    private Long calculateTotalPointsOfSolution(Solution solution) {
        return SolutionPoints.calculateSolutionPoints(solution).totalPoints;
    }


    private class SAAndRequests {

        private StudentActivity studentActivity;
        private List<Long> requests;
        private Long selectedGroupId;
        private boolean solved;

        public SAAndRequests(StudentActivity studentActivity, List<Long> requests, Long selectedGroupId) {
            this.studentActivity = studentActivity;
            this.requests = requests;
            this.selectedGroupId = selectedGroupId;
            solved = false;
        }

        public StudentActivity getStudentActivity() {
            return studentActivity;
        }

        public List<Long> getRequests() {
            return requests;
        }

        public Long getSelectedGroupId() {
            return selectedGroupId;
        }

        public void solved() {
            solved = true;
        }

        public boolean isSolved() {
            return solved;
        }
    }

}
