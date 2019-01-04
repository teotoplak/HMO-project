package store;

import lombok.Data;
import models.StudentActivity;

import java.util.Map;

@Data
public class StudentActivityStore {

    public static Map<String, StudentActivity> studentActivityMap;

    public int calculatePoints(Long studentId, Long activityId) {
        return 0;
    }

    public static StudentActivity getStudentActivity(Long studentId, Long activityId) {
        // todo
        return null;
    }
}
