package com.BrainStack.Services;

import com.BrainStack.Entity.Activity;
import java.util.List;
import java.util.Optional;

public interface ActivityService {
    Activity createActivity(Activity activity);
    List<Activity> getAllActivities();
    Optional<Activity> getActivityById(Long id);
    Activity updateActivity(Long id, Activity activity);
    void deleteActivity(Long id);
}
