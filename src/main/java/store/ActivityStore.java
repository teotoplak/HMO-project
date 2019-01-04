package store;

import lombok.Data;
import models.Activity;

import java.util.List;

@Data
public class ActivityStore {

    public static List<Activity> activities;
}
