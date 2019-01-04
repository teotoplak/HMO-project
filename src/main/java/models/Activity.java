package models;

import lombok.Data;

import java.util.List;

@Data
public class Activity {

    private Long id;
    private List<Long> studentIds;

}
