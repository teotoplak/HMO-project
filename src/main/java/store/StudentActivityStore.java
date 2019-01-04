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
}
