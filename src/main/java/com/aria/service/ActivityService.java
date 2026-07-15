package com.aria.service;

import com.aria.dto.ActivityDto;
import com.aria.dao.ActivityRepository;
import com.aria.model.Activity;
import com.aria.model.Status;
import com.aria.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
public class ActivityService {


    @Autowired
    ActivityRepository activityRepository;


    public ResponseEntity<?> saveActivity(ActivityDto activityDto, User loggedUser) {
        Activity Activity = new Activity(activityDto);
        //Activity.setCreated_date(LocalDateTime.now());
        //Activity.setCreated_by(loggedUser.getId());
        Activity result = activityRepository.save(Activity);
        return new ResponseEntity<Status>(new Status("200", "Activity created SuccessFully"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateActivity(ActivityDto activityDto, User loggedUser) {
        Activity activity = new Activity(activityDto);
        Activity result = activityRepository.save(activity);
        return new ResponseEntity<Status>(new Status("200", "Activity updated SuccessFully"), HttpStatus.OK);
    }


    public ResponseEntity<?> getActivity(String id) throws UsernameNotFoundException {
        Optional<Activity> optionalActivity = activityRepository.findById(Long.parseLong(id));
        if (optionalActivity.isPresent()) {
            ActivityDto activityDto = new ActivityDto(optionalActivity.get());
            return new ResponseEntity<ActivityDto>(activityDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<?> deleteActivity(String id) throws UsernameNotFoundException {
        try {
            Optional<Activity> optionalActivity = activityRepository.findById(Long.parseLong(id));
            if (optionalActivity.isPresent()) {
                Activity activity = optionalActivity.get();
                activityRepository.delete(activity);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }
}
