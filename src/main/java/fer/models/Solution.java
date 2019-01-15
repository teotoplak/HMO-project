package fer.models;


import java.util.HashMap;
import java.util.Map;

// representing solution
// consists of groups and student activities
public class Solution {

    private Map<Long, Group> groupMap;

    // key is in format studentId:activityId
    private Map<String, StudentActivity> studentActivityMap;

    public Solution(Map<Long, Group> groupMap, Map<String, StudentActivity> studentActivityMap) {
        this.groupMap = groupMap;
        this.studentActivityMap = studentActivityMap;
    }

    public Solution(Solution solution) {
        groupMap = new HashMap<>(solution.getGroupMap());
        studentActivityMap = new HashMap<>(solution.getStudentActivityMap());
    }

    public StudentActivity getStudentActivity(Long studentId, Long activityId) {
        return studentActivityMap.get(studentId + ":" + activityId);
    }

    public Group getGroup(Long groupId) {
        return groupMap.get(groupId);
    }

    public Map<Long, Group> getGroupMap() {
        return groupMap;
    }

    public Map<String, StudentActivity> getStudentActivityMap() {
        return studentActivityMap;
    }
}
