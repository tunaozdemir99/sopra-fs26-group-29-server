package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByBucketItem_BucketItemIdAndUser_Id(Long bucketItemId, Long userId);
    List<Vote> findByBucketItem_BucketItemId(Long bucketItemId);
}
