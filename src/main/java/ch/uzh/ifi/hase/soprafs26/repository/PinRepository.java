package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.Pin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ClassName: PinRepository
 * Package: ch.uzh.ifi.hase.soprafs26.repository
 * Description:
 *
 * @ author Stella_Xiao
 * @ create 2026/4/21 22:16
 * @ version 1.0
 */
@Repository("pinRepository")
public interface PinRepository extends JpaRepository<Pin, Long> {

    List<Pin> findByTrip_TripId(Long tripId);
}
