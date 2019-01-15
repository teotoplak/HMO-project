package fer.store;

import fer.models.Solution;
import lombok.Data;
import fer.models.Student;
import fer.models.StudentActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class StudentStore {

    public static Map<Long, Student> studentMap = new HashMap<>();

    public static List<StudentActivity> getStudentActivitiesOfStudent(Solution solution, Long studentId) {
        return StudentStore.studentMap.get(studentId)
                .getActivityIds()
                .stream()
                .map(activityId -> solution.getStudentActivity(studentId, activityId))
                .collect(Collectors.toList());
    }

}
