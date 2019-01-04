package store;

import lombok.Data;
import models.Student;
import models.StudentActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class StudentStore {

    public static Map<Long, Student> studentMap = new HashMap<>();

    public static List<Long> getAllSelectedGroupIdsForStudent(Long studentId) {
        return StudentStore.studentMap.get(studentId).getActivityIds().stream()
                .map(activityId -> StudentActivityStore.getStudentActivity(studentId, activityId))
                .map(StudentActivity::getSelectedGroupId)
                // .filter(groupId -> !groupId.equals(group.getId()))
                .collect(Collectors.toList());
    }

}
