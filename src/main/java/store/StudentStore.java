package store;

import lombok.Data;
import models.Student;

import java.util.Map;

@Data
public class StudentStore {

    private Map<Long, Student> studentMap;
}
