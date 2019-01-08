package store;

import lombok.Data;
import models.StudentActivity;

import java.util.HashMap;
import java.util.Map;

@Data
public class StudentActivityStore {

    // key is in format studentId:activityId
    public static Map<String, StudentActivity> studentActivityMap = new HashMap<>();

    public static StudentActivity getStudentActivity(Long studentId, Long activityId) {
        String key = studentId + ":" + activityId;
        return studentActivityMap.get(key);
    }
}
