package models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Activity {

    private Long id;
    private List<Long> studentIds;
}
