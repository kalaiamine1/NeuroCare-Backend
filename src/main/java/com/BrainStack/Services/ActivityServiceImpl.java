package com.BrainStack.Services;

import com.BrainStack.Entity.Activity;
import com.BrainStack.Repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    @Override
    public Activity createActivity(Activity activity) {
        activity.setCreatedAt(LocalDateTime.now());
        return activityRepository.save(activity);
    }

    @Override
    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    @Override
    public Optional<Activity> getActivityById(Long id) {
        return activityRepository.findById(id);
    }

    @Override
    public Activity updateActivity(Long id, Activity updatedActivity) {
        return activityRepository.findById(id).map(existing -> {
            existing.setTitle(updatedActivity.getTitle());
            existing.setDescription(updatedActivity.getDescription());
            existing.setType(updatedActivity.getType());
            existing.setAgeRange(updatedActivity.getAgeRange());
            existing.setDifficultyLevel(updatedActivity.getDifficultyLevel());
            existing.setCategory(updatedActivity.getCategory());
            existing.setFileUrl(updatedActivity.getFileUrl());
            existing.setPointsReward(updatedActivity.getPointsReward());
            existing.setAiRecommended(updatedActivity.isAiRecommended());
            existing.setUpdatedAt(LocalDateTime.now());
            return activityRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Activity not found with id: " + id));
    }

    @Override
    public void deleteActivity(Long id) {
        if (!activityRepository.existsById(id)) {
            throw new RuntimeException("Activity not found with id: " + id);
        }
        activityRepository.deleteById(id);
    }
}
