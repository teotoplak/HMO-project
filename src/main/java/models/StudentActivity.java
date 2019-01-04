package models;

import lombok.Data;

import java.util.List;

@Data
public class StudentActivity {

    private Long studentId;
    private Long activityId;
    private Long selectedGroupId;
    private Long initialGroupId;
    private List<Long> possibleGroupIds;
}
