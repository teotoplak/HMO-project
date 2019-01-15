package fer.store;

import lombok.Data;
import fer.models.Activity;

import java.util.LinkedList;
import java.util.List;

@Data
public class ActivityStore {

    public static List<Activity> activities = new LinkedList<>();
}
