package org.astu.attendancetracker.presentation.services.impl;

import lombok.RequiredArgsConstructor;
import org.astu.attendancetracker.core.domain.Discipline;
import org.astu.attendancetracker.core.domain.RatingScore;
import org.astu.attendancetracker.core.domain.StudentProfile;
import org.astu.attendancetracker.persistence.repositories.DisciplineRepository;
import org.astu.attendancetracker.persistence.repositories.ProfileRepository;
import org.astu.attendancetracker.persistence.repositories.RatingScoreRepository;
import org.astu.attendancetracker.presentation.services.RatingScoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingScoreServiceImpl implements RatingScoreService {

    private static final double MAX_SCORE = 100.0;

    private final ProfileRepository profileRepository;
    private final DisciplineRepository disciplineRepository;
    private final RatingScoreRepository ratingScoreRepository;

    @Override
    @Transactional
    public int awardMaxScoreWithoutDelayToAllStudents() {
        LocalDate awardedDate = LocalDate.now();
        LocalDate deadlineDate = awardedDate.plusDays(1);

        List<RatingScore> toSave = new ArrayList<>();
        for (StudentProfile student : profileRepository.findAllStudentProfiles()) {
            if (student.getGroup() == null) {
                continue;
            }
            for (Discipline discipline : disciplineRepository.findByGroup(student.getGroup())) {
                ratingScoreRepository.deleteByStudentProfile_IdAndDiscipline_Id(student.getId(), discipline.getId());
                toSave.add(RatingScore.builder()
                        .score(MAX_SCORE)
                        .awardedDate(awardedDate)
                        .deadlineDate(deadlineDate)
                        .studentProfile(student)
                        .discipline(discipline)
                        .build());
            }
        }

        ratingScoreRepository.saveAll(toSave);
        return toSave.size();
    }

    @Override
    @Transactional
    public long deleteAllScores() {
        long count = ratingScoreRepository.count();
        ratingScoreRepository.deleteAllInBatch();
        return count;
    }
}
