package store;

import lombok.Data;
import models.Group;

import java.util.Map;

@Data
public class GroupStore {

    private Map<Long, Group> groupMap;
}
