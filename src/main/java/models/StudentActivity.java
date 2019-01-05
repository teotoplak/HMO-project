package models;

import lombok.AllArgsConstructor;
import lombok.Data;
import store.GroupStore;

import java.util.List;

@Data
@AllArgsConstructor
public class StudentActivity {

    private Long studentId;
    private Long activityId;
    private Long selectedGroupId;
    private Long initialGroupId;
    private List<Long> possibleGroupIds;
    private Long swapWeight;

    public void selectNewGroup(Long groupId) {
        // change counts of groups influenced
        GroupStore.groupMap.get(selectedGroupId).decreaseStudentCount();
        GroupStore.groupMap.get(groupId).increaseStudentCount();
        selectedGroupId = groupId;
    }

}
