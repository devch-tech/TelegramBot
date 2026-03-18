package org.example.telegrambot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "user_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStats {

    @Id
    private Long chatId;

    private String firstName;
    private String username;

    private String primaryLanguage;
    private String secondLanguage;

    private int exercisesDone;
    private int issuesExplored;
    private int quizzesCompleted;
    private int totalQuizScore;

    private int     streak;
    private LocalDate lastActiveDate;
    private LocalDate lastDailyDate;
    private LocalDate registeredAt;
}
