package store;

import lombok.Data;
import models.StudentActivity;

import java.util.Map;

@Data
public class StudentActivityStore {

    private Map<String, StudentActivity> studentActivityMap;
}
