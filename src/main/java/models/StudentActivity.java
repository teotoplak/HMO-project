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
    // includes initial group
    private List<Long> possibleGroupIds;
    private Long swapWeight;
    private boolean hasRequest;

    public StudentActivity(Long studentId, Long activityId, Long selectedGroupId, Long initialGroupId, List<Long> possibleGroupIds, Long swapWeight) {
        this.studentId = studentId;
        this.activityId = activityId;
        this.selectedGroupId = selectedGroupId;
        this.initialGroupId = initialGroupId;
        this.possibleGroupIds = possibleGroupIds;
        this.swapWeight = swapWeight;
        this.hasRequest = possibleGroupIds.size() > 1;
    }

    public void selectNewGroup(Long groupId) {
        // change counts of groups influenced
        GroupStore.groupMap.get(selectedGroupId).decreaseStudentCount();
        GroupStore.groupMap.get(groupId).increaseStudentCount();
        selectedGroupId = groupId;
    }

    public boolean isChangedFromInitial() {
        return !initialGroupId.equals(selectedGroupId);
    }

    public boolean hasRequest() {
        return hasRequest;
    }

}
