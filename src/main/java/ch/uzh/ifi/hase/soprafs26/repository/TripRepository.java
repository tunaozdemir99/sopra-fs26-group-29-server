package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ClassName: TripRepository
 * Package: ch.uzh.ifi.hase.soprafs26.repository
 * Description:
 *
 * @ author Stella_Xiao
 * @ create 2026/3/30 15:35
 * @ version 1.0
 */
@Repository("tripRepository")
public interface TripRepository extends JpaRepository<Trip, Long> {
    // prepared for S5
    Optional<Trip> findByInviteUrl(String inviteUrl);

    List<Trip> findByMembers_Id(Long userId);
}
