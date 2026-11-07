package at.salzburg.hackathon.hackathontourism03.service.logics;

import at.salzburg.hackathon.hackathontourism03.domain.*;
import at.salzburg.hackathon.hackathontourism03.persistence.PlaceRepository;
import at.salzburg.hackathon.hackathontourism03.persistence.ReviewRepository;
import at.salzburg.hackathon.hackathontourism03.persistence.UserRepository;
import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response.AspectRatingTO;
import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response.ReviewTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Map;

@Service
public class ReviewLogic {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    public boolean createReview(Long id, String locationName, String message, Map<String, Integer> ratings) {
        UserEntity user = userRepository.findUserById(id);
        if(user == null) {
            System.out.println("User with id " + id + " was not found.");
            return false;
        }

        PlaceEntity place = placeRepository.findPlaceEntityByLocationName(locationName);
        if(place == null) {
            System.out.println("Place with name " + locationName + " was not found.");
            return false;
        }

        ReviewEntity review = reviewRepository.findReviewById(new ReviewId(locationName, id));
        if(review != null) {
            System.out.println("This user already did a review on this place. Use the Put-Request instead to edit it.");
            return false;
        }

        review = new ReviewEntity();
        review.setId(new ReviewId(locationName, id));
        review.setLastEditedDate(null);
        review.setMessage(message);
        review.setPublishDate(LocalDateTime.now());
        for(var rating : ratings.entrySet()) {
            review.getRatings().put(rating.getKey(), rating.getValue());
        }

        user.setPoints(user.getPoints() + 10);
        place.addReview(review);
        user.addReview(review);
        reviewRepository.save(review);
        placeRepository.save(place);
        userRepository.save(user);
        return true;
    }

    public ReviewTO getAllReviewsOfUser(Long id) {
        UserEntity user = userRepository.findUserById(id);
        if(user == null) {
            System.out.println("Could not finde user with id " + id);
            return null;
        }

        ReviewTO result = new ReviewTO();
        for(var review : user.getReviews()) {
            result.setPlace(review.getPlace().getLocationName());
            result.setMessage(review.getMessage());
            result.setCreationDate(review.getPublishDate());
            result.setLastEditedDate(review.getLastEditedDate() == null ? null : review.getLastEditedDate());
            result.setAspects(new ArrayList<>());
            for(var aspect : review.getRatings().entrySet()) {
                result.getAspects().add(new AspectRatingTO(aspect.getKey(), aspect.getValue()));
            }
        }

        return result;
    }

    public boolean editReview(Long id, String locationName, String message, Map<String, Integer> ratings) {
        UserEntity user = userRepository.findUserById(id);
        if(user == null) {
            System.out.println("User with id " + id + " was not found.");
            return false;
        }

        PlaceEntity place = placeRepository.findPlaceEntityByLocationName(locationName);
        if(place == null) {
            System.out.println("Place with name " + locationName + " was not found.");
            return false;
        }

        ReviewEntity review = reviewRepository.findReviewById(new ReviewId(locationName, id));
        if(review == null) {
            System.out.println("This user has not published a review for this location yet.");
            return false;
        }

        review.setLastEditedDate(null);
        review.setMessage(message);
        review.setPublishDate(LocalDateTime.now());
        for(var rating : ratings.entrySet()) {
            review.getRatings().put(rating.getKey(), rating.getValue());
        }

        user.setPoints(user.getPoints() + 5);
        userRepository.save(user);
        reviewRepository.save(review);
        return true;
    }
}
