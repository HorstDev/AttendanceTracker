package org.astu.attendancetracker.presentation.services.impl;

import jakarta.transaction.Transactional;
import org.astu.attendancetracker.core.application.CurriculumAnalyzer;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGroupSchedule;
import org.astu.attendancetracker.core.application.schedule.GroupBuilder;
import org.astu.attendancetracker.core.application.schedule.ScheduleManager;
import org.astu.attendancetracker.core.application.schedule.impl.GroupBuilderImpl;
import org.astu.attendancetracker.core.domain.Discipline;
import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.core.domain.TeacherProfile;
import org.astu.attendancetracker.core.domain.Competency;
import org.astu.attendancetracker.core.domain.DisciplineCurriculum;
import org.astu.attendancetracker.persistence.repositories.CompetencyRepository;
import org.astu.attendancetracker.persistence.repositories.DisciplineCurriculumRepository;
import org.astu.attendancetracker.persistence.repositories.DisciplineRepository;
import org.astu.attendancetracker.persistence.repositories.GroupRepository;
import org.astu.attendancetracker.persistence.repositories.ProfileRepository;
import org.astu.attendancetracker.presentation.mappers.GroupMapper;
import org.astu.attendancetracker.presentation.services.GroupService;
import org.astu.attendancetracker.presentation.viewModels.GroupDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class GroupServiceImpl implements GroupService {
    private final ScheduleManager scheduleManager;
    private final ProfileRepository profileRepository;
    private final GroupRepository groupRepository;
    private final DisciplineRepository disciplineRepository;
    private final CompetencyRepository competencyRepository;
    private final DisciplineCurriculumRepository disciplineCurriculumRepository;
    private final GroupMapper groupMapper;

    public GroupServiceImpl(ScheduleManager scheduleFetcher, ProfileRepository profileRepository,
                            GroupRepository groupRepository, DisciplineRepository disciplineRepository,
                            CompetencyRepository competencyRepository,
                            DisciplineCurriculumRepository disciplineCurriculumRepository,
                            GroupMapper groupMapper) {
        this.scheduleManager = scheduleFetcher;
        this.profileRepository = profileRepository;
        this.groupRepository = groupRepository;
        this.disciplineRepository = disciplineRepository;
        this.competencyRepository = competencyRepository;
        this.disciplineCurriculumRepository = disciplineCurriculumRepository;
        this.groupMapper = groupMapper;
    }

    public CompletableFuture<ApiTableGroupSchedule> getApiTableGroupSchedule(String groupName) {
        return scheduleManager.getGroupSchedule(groupName);
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    // Возвращает все группы, в которых принимает участие преподаватель в текущем семестре
    public CompletableFuture<HashSet<String>> getAllGroupsForTeacher(String teacherName) {
        return scheduleManager.getGroupsForTeacher(teacherName);
    }

    // Сохранение группы
    public Group saveGroup(String groupName) {
        Group group = new Group();
        group.setName(groupName);
        return groupRepository.save(group);
    }

    // Поиск группы по идентификатору
    public Group findGroupById(UUID groupId) {
        return groupRepository.findById(groupId).orElseThrow(() ->
                new IllegalArgumentException("Группа с id " +  groupId + " не найдена"));
    }

    // Возвращает номер текущей недели
    @Cacheable(value = "schedule", key = "'current-week-number'")
    public CompletableFuture<Integer> getCurrentWeekNumber() {
        return scheduleManager.getCurrentWeekNumber();
    }

    // Загрузка семестра для группы (дисциплины, преподаватели, занятия и т.д.)
    @Transactional
    public void uploadSemesterForGroup(Group group, ApiTableGroupSchedule apiTableGroupSchedule,
                                        int currentWeekNumber, int currentSemester) {

        List<Discipline> disciplinesWithCurrentSemester = disciplineRepository.findByGroupAndSemester(group, currentSemester);
        if (!disciplinesWithCurrentSemester.isEmpty())
            throw new RuntimeException("Данные о семестре уже были загружены ранее");

        // Преподаватели, которые преподают у группы groupName
        Set<TeacherProfile> teacherProfilesInSchedule = getTeacherProfilesFromDatabaseOnGroupSchedule(apiTableGroupSchedule);

        // Загружаем в обновленную группу текущий семестр
        Group updatedGroup = groupBuilder()
                .setGroupSchedule(apiTableGroupSchedule)
                .setGroup(group)
                .setDisciplines(currentSemester)
                .setTeachersForDisciplines(teacherProfilesInSchedule)
                .setLessons(currentWeekNumber)
                .build();

        groupRepository.save(updatedGroup);
    }

    public GroupBuilder groupBuilder() {
        return new GroupBuilderImpl();
    }

    // Возвращает преподавателей из БД на основе имен в расписании (ApiTableGroupSchedule)
    private Set<TeacherProfile> getTeacherProfilesFromDatabaseOnGroupSchedule(ApiTableGroupSchedule schedule) {
        Set<String> teacherNames = schedule.getTeacherNames();
        Set<TeacherProfile> teachersFromDb = profileRepository.findAllTeacherProfilesByNameIn(teacherNames);
        // Если рамзеры не равны (т.е. какие-то преподаватели в apiTableGroupSchedule отсутствуют в БД)
        if (teachersFromDb.size() != teacherNames.size())
            throw new RuntimeException("Преподаватели у группы " + schedule.name() + " отсутствуют в БД. Необходимо обновить базу данных преподавателей");
        return teachersFromDb;
    }

    @Override
    @Transactional
    public void uploadCurriculumForGroup(UUID groupId, MultipartFile curriculumFile) {
        if (curriculumFile.isEmpty()) {
            throw new RuntimeException("Файл (учебный план) пуст");
        }
        Group group = findGroupById(groupId);
        byte[] curriculumBytes;
        try {
            curriculumBytes = curriculumFile.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении файла " + curriculumFile.getOriginalFilename());
        }
        group.setCurriculumFile(curriculumBytes);

        List<Discipline> disciplinesInCurrentSemester = disciplineRepository.findByGroupInCurrentSemester(group.getId());
        CurriculumAnalyzer.uploadInformationForDisciplines(curriculumBytes, disciplinesInCurrentSemester);
        disciplineRepository.saveAll(disciplinesInCurrentSemester);

        List<Discipline> allGroupDisciplines = disciplineRepository.findByGroup(group);
        for (Discipline d : allGroupDisciplines) {
            d.getCompetencies().removeIf(c -> c.getGroup() != null && group.getId().equals(c.getGroup().getId()));
        }
        disciplineRepository.saveAll(allGroupDisciplines);

        disciplineCurriculumRepository.deleteByGroup_Id(group.getId());

        List<CurriculumAnalyzer.CompetencyExtraction> extractions =
                CurriculumAnalyzer.extractCompetenciesWithCurriculumDisciplines(curriculumBytes);
        persistCurriculumExtractions(group, extractions);

        List<DisciplineCurriculum> curriculumDisciplines = disciplineCurriculumRepository.findByGroup_Id(group.getId());
        for (Discipline d : allGroupDisciplines) {
            CurriculumAnalyzer.matchScheduleDisciplineToCurriculum(d, curriculumDisciplines)
                    .ifPresent(cd -> d.getCompetencies().addAll(cd.getCurriculumCompetencies()));
        }
        disciplineRepository.saveAll(allGroupDisciplines);
        groupRepository.save(group);
    }

    private void persistCurriculumExtractions(Group group, List<CurriculumAnalyzer.CompetencyExtraction> extractions) {
        Map<String, DisciplineCurriculum> curriculumByName = new LinkedHashMap<>();
        for (CurriculumAnalyzer.CompetencyExtraction ext : extractions) {
            CurriculumAnalyzer.CompetencyData data = ext.competency();
            Competency competency = competencyRepository
                    .findByAbbreviationAndGroup_Id(data.abbreviation(), group.getId())
                    .map(existing -> {
                        existing.setDescription(data.description());
                        return competencyRepository.save(existing);
                    })
                    .orElseGet(() -> competencyRepository.save(new Competency(data.abbreviation(), data.description(), group)));

            for (String raw : ext.curriculumDisciplineNames()) {
                String name = raw.trim();
                if (name.isEmpty())
                    continue;
                DisciplineCurriculum dc = curriculumByName.computeIfAbsent(name, n -> new DisciplineCurriculum(n, group));
                dc.getCurriculumCompetencies().add(competency);
            }
        }
        disciplineCurriculumRepository.saveAll(curriculumByName.values());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<GroupDto> findGroupsByPartOfName(String partOfName) {
        List<Group> groups = groupRepository.findAllGroupsByPartOfName(partOfName);
        return groupMapper.toDto(groups);
    }
}
