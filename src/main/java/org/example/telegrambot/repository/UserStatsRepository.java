package org.example.telegrambot.repository;

import org.example.telegrambot.model.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserStatsRepository extends JpaRepository<UserStats, Long> {

    /** Count users that were active on or after the given date. */
    long countByLastActiveDateGreaterThanEqual(LocalDate date);

    /** Top 10 users sorted by streak (for future leaderboard). */
    List<UserStats> findTop10ByOrderByStreakDesc();
}
