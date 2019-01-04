package models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Group {

    private Long id;
    private Long studentCount;
    private Long min;
    private Long max;
    private Long minPreferred;
    private Long maxPreferred;
    private List<Long> overlapGroupIds;
}
