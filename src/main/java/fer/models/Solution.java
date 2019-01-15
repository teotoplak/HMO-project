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
        this.groupMap = new HashMap<>();//solution.groupMap);
        solution.getGroupMap().forEach((key, group) -> groupMap.put(key, new Group(group)));
        this.studentActivityMap = new HashMap<>();//solution.studentActivityMap);
        solution.getStudentActivityMap().forEach((key, studentActivity) -> studentActivityMap.put(key, new StudentActivity(studentActivity)));
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
