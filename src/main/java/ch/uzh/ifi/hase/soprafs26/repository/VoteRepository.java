package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.BucketItem;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("voteRepository")
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByUserAndBucketItem(User user, BucketItem bucketItem);
}