package models;

import lombok.Data;
import store.GroupStore;

import java.util.List;

@Data
public class StudentActivity {

    private Long studentId;
    private Long activityId;
    private Long selectedGroupId;
    private Long initialGroupId;
    private List<Long> possibleGroupIds;
    private int swapWeight;

    public void selectNewGroup(Long groupId) {
        // change counts of groups influenced
        GroupStore.groupMap.get(selectedGroupId).decreaseStudentCount();
        GroupStore.groupMap.get(groupId).increaseStudentCount();
        selectedGroupId = groupId;
    }

}
