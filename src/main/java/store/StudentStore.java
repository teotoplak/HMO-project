package store;

import lombok.Data;
import models.Student;

import java.util.HashMap;
import java.util.Map;

@Data
public class StudentStore {

    public static Map<Long, Student> studentMap = new HashMap<>();
}
