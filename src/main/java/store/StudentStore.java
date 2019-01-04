package store;

import lombok.Data;
import lombok.Getter;
import models.Activity;
import models.Group;
import models.Student;
import models.StudentActivity;

import java.util.List;
import java.util.Map;

@Data
public class StudentStore {

    public static Map<Long, Student> studentMap;

}
