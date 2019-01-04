package store;

import lombok.Data;
import models.Group;

import java.util.Map;

@Data
public class GroupStore {

    public static Map<Long, Group> groupMap;

    public int calculatePoints(Long groupId) {
        return 0;
    }

}
