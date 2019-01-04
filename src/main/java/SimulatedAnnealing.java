import models.Activity;
import models.Group;
import models.StudentActivity;
import store.ActivityStore;
import store.GroupStore;
import store.StudentActivityStore;
import store.StudentStore;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SimulatedAnnealing {

    private void start() {

        // todo flag to stop the iterations

        // iterate through activities
        ActivityStore.activities.forEach(activity -> {
            // for each student activity find possible groups to select and do it randomly
            List<StudentActivity> studentActivities = activity.getStudentIds().stream()
                    .map(studentId -> StudentActivityStore.getStudentActivity(studentId, activity.getId()))
                    .collect(Collectors.toList());
            for (StudentActivity studentActivity : studentActivities) {

                // if current studentActivity group is at min limit (hard constraint) - skip
                if (GroupStore.groupMap.get(studentActivity.getSelectedGroupId()).isAtMinNumOfStudents()) {
                    continue;
                }

                // all groups for student activity which can be selected (including currently selected)
                List<Long> allCurrentGroupsIdsOfStudent = StudentStore.getAllSelectedGroupIdsForStudent(studentActivity.getStudentId());
                List<Long> possibleGroupIdsToSelect = studentActivity.getPossibleGroupIds().stream()
                        .map(groupId -> GroupStore.groupMap.get(groupId))
                        // group should not be full
                        .filter(group -> !group.isFull())
                        // should not have overlap with other student group
                        .filter(group -> !hasIntersection(group.getOverlapGroupIds(), allCurrentGroupsIdsOfStudent))
                        .map(Group::getId)
                        .collect(Collectors.toList());

                // randomize group selection
                Long newRandomSelectedGroupId = randomItemFromList(possibleGroupIdsToSelect);
                studentActivity.selectNewGroup(newRandomSelectedGroupId);
            }

        });

        // todo calculate points

        // todo memorize biggest solution

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

    private void calculateNeighbour() {

    }

    private int calculatePoints() {

        return 0;
    }
}
