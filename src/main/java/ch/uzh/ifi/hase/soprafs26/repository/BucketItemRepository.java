package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.BucketItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository("bucketItemRepository")
public interface BucketItemRepository extends JpaRepository<BucketItem, Long> {
    List<BucketItem> findByBucketTrip_TripId(Long tripID);
}
