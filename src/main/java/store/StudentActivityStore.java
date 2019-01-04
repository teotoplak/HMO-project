package store;

import lombok.Data;
import models.StudentActivity;

import java.util.HashMap;
import java.util.Map;

@Data
public class StudentActivityStore {

    // key is in format studentId:activityId
    public static Map<String, StudentActivity> studentActivityMap = new HashMap<>();

    public int calculatePoints(Long studentId, Long activityId) {
        return 0;
    }
}
