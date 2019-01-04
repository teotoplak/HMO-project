package models;

import lombok.Data;

import java.util.List;

@Data
public class Student {

    private Long id;
    private List<Long> activityIds;
}
