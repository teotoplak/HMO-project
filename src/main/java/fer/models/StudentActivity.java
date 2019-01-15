package fer.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

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

    public StudentActivity(StudentActivity studentActivity) {
        this.studentId = studentActivity.studentId;
        this.activityId = studentActivity.activityId;
        this.selectedGroupId = studentActivity.selectedGroupId;
        this.initialGroupId = studentActivity.initialGroupId;
        this.possibleGroupIds = studentActivity.possibleGroupIds;
        this.swapWeight = studentActivity.swapWeight;
        this.hasRequest = studentActivity.possibleGroupIds.size() > 1;
    }

    public void selectNewGroup(Map<Long, Group> groupMap, Long groupId) {
        // change counts of groups influenced
        groupMap.get(selectedGroupId).decreaseStudentCount();
        groupMap.get(groupId).increaseStudentCount();
        selectedGroupId = groupId;
    }

    public boolean isChangedFromInitial() {
        return !initialGroupId.equals(selectedGroupId);
    }

    public boolean hasRequest() {
        return hasRequest;
    }

}
