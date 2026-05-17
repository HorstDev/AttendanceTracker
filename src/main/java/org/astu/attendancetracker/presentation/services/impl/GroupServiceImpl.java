package org.astu.attendancetracker.presentation.services.impl;

import jakarta.transaction.Transactional;
import org.astu.attendancetracker.core.application.CurriculumAnalyzer;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGroupSchedule;
import org.astu.attendancetracker.core.application.schedule.ClasspathApiTableReader;
import org.astu.attendancetracker.core.application.schedule.GroupBuilder;
import org.astu.attendancetracker.core.application.schedule.ScheduleManager;
import org.astu.attendancetracker.core.application.schedule.impl.GroupBuilderImpl;
import org.astu.attendancetracker.core.domain.Discipline;
import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.core.domain.TeacherProfile;
import org.astu.attendancetracker.core.domain.Competency;
import org.astu.attendancetracker.core.domain.DisciplineCurriculum;
import org.astu.attendancetracker.core.domain.DisciplineCurriculumCompetency;
import org.astu.attendancetracker.persistence.repositories.CompetencyRepository;
import org.astu.attendancetracker.persistence.repositories.DisciplineCurriculumCompetencyRepository;
import org.astu.attendancetracker.persistence.repositories.DisciplineCurriculumRepository;
import org.astu.attendancetracker.persistence.repositories.DisciplineRepository;
import org.astu.attendancetracker.persistence.repositories.GroupRepository;
import org.astu.attendancetracker.persistence.repositories.ProfileRepository;
import org.astu.attendancetracker.presentation.mappers.GroupMapper;
import org.astu.attendancetracker.presentation.services.GroupService;
import org.astu.attendancetracker.presentation.viewModels.CompetencyWeightMatrixViews;
import org.astu.attendancetracker.presentation.viewModels.GroupDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {
    private final ScheduleManager scheduleManager;
    private final ClasspathApiTableReader classpathApiTableReader;
    private final ProfileRepository profileRepository;
    private final GroupRepository groupRepository;
    private final DisciplineRepository disciplineRepository;
    private final CompetencyRepository competencyRepository;
    private final DisciplineCurriculumRepository disciplineCurriculumRepository;
    private final DisciplineCurriculumCompetencyRepository disciplineCurriculumCompetencyRepository;
    private final GroupMapper groupMapper;

    private static final double WEIGHT_SUM_EPS = 1e-5;
    private static final double HUNDREDTH_STEP = 0.01;
    /** Допуск для проверки «значение кратно 0.01» из-за двоичного представления double */
    private static final double HUNDREDTH_TOLERANCE = 1e-4;

    private static double roundToHundredths(double w) {
        return Math.rint(w * 100.0) / 100.0;
    }

    private static boolean isMultipleOfHundredth(double w) {
        return Math.abs(w * 100.0 - Math.rint(w * 100.0)) < HUNDREDTH_TOLERANCE;
    }

    public GroupServiceImpl(ScheduleManager scheduleFetcher,
                            ClasspathApiTableReader classpathApiTableReader,
                            ProfileRepository profileRepository,
                            GroupRepository groupRepository, DisciplineRepository disciplineRepository,
                            CompetencyRepository competencyRepository,
                            DisciplineCurriculumRepository disciplineCurriculumRepository,
                            DisciplineCurriculumCompetencyRepository disciplineCurriculumCompetencyRepository,
                            GroupMapper groupMapper) {
        this.scheduleManager = scheduleFetcher;
        this.classpathApiTableReader = classpathApiTableReader;
        this.profileRepository = profileRepository;
        this.groupRepository = groupRepository;
        this.disciplineRepository = disciplineRepository;
        this.competencyRepository = competencyRepository;
        this.disciplineCurriculumRepository = disciplineCurriculumRepository;
        this.disciplineCurriculumCompetencyRepository = disciplineCurriculumCompetencyRepository;
        this.groupMapper = groupMapper;
    }

    public CompletableFuture<ApiTableGroupSchedule> getApiTableGroupSchedule(String groupName) {
        return scheduleManager.getGroupSchedule(groupName);
    }

    @Override
    public ApiTableGroupSchedule getApiTableGroupScheduleFromClasspathJson() {
        try {
            return classpathApiTableReader.readGroupSchedule();
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать " + ClasspathApiTableReader.GROUP_SCHEDULE_RESOURCE, e);
        }
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    @Override
    @Transactional
    public List<Group> getSupervisedGroupsForTeacher(UUID userId) {
        return profileRepository.findTeacherProfileByUserIdWithDisciplinesAndGroups(userId)
                .map(teacher -> teacher.getDisciplines().stream()
                        .map(Discipline::getGroup)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(Group::getId, g -> g, (a, b) -> a, LinkedHashMap::new))
                        .values()
                        .stream()
                        .sorted(Comparator.comparing(Group::getName, String.CASE_INSENSITIVE_ORDER))
                        .toList())
                .orElse(List.of());
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

    @Override
    public int getCurrentWeekNumberFromClasspathJson() {
        try {
            return classpathApiTableReader.readCurrentWeekNumber();
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать weekOverride из " + ClasspathApiTableReader.TEACHERS_RESOURCE, e);
        }
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

        List<DisciplineCurriculum> curriculumDisciplines = disciplineCurriculumRepository.findByGroup_IdOrderByNameAsc(group.getId());
        for (Discipline d : allGroupDisciplines) {
            CurriculumAnalyzer.matchScheduleDisciplineToCurriculum(d, curriculumDisciplines)
                    .ifPresent(cd -> d.getCompetencies().addAll(cd.getLinkedCompetencies()));
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
                boolean alreadyLinked = dc.getCompetencyLinks().stream()
                        .anyMatch(l -> l.getCompetency().getId().equals(competency.getId()));
                if (!alreadyLinked)
                    dc.addCompetencyLink(competency, 0.0);
            }
        }
        disciplineCurriculumRepository.saveAll(curriculumByName.values());
        assignEqualCompetencyWeightsForGroup(group.getId());
    }

    private void assignEqualCompetencyWeightsForGroup(UUID groupId) {
        List<DisciplineCurriculumCompetency> links =
                disciplineCurriculumCompetencyRepository.findByDisciplineCurriculum_Group_Id(groupId);
        Map<UUID, List<DisciplineCurriculumCompetency>> byCompetency = links.stream()
                .collect(Collectors.groupingBy(l -> l.getCompetency().getId()));
        for (List<DisciplineCurriculumCompetency> list : byCompetency.values()) {
            if (list.isEmpty())
                continue;
            int n = list.size();
            int baseCents = 100 / n;
            int remainderCents = 100 % n;
            for (int i = 0; i < n; i++) {
                int cents = baseCents + (i < remainderCents ? 1 : 0);
                list.get(i).setWeight(cents * HUNDREDTH_STEP);
            }
        }
        disciplineCurriculumCompetencyRepository.saveAll(links);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public CompetencyWeightMatrixViews.MatrixDto getCompetencyWeightMatrix(UUID groupId) {
        findGroupById(groupId);
        List<Competency> columns = competencyRepository.findByGroup_IdOrderByAbbreviationAsc(groupId);
        List<DisciplineCurriculum> rowEntities = disciplineCurriculumRepository.findByGroup_IdOrderByNameAsc(groupId);

        Map<UUID, Map<UUID, DisciplineCurriculumCompetency>> linkLookup = new HashMap<>();
        for (DisciplineCurriculumCompetency link : disciplineCurriculumCompetencyRepository.findAllFetchedByGroupId(groupId)) {
            linkLookup
                    .computeIfAbsent(link.getDisciplineCurriculum().getId(), k -> new HashMap<>())
                    .put(link.getCompetency().getId(), link);
        }

        List<CompetencyWeightMatrixViews.ColumnDto> columnDtos = columns.stream()
                .map(c -> new CompetencyWeightMatrixViews.ColumnDto(c.getId(), c.getAbbreviation()))
                .toList();

        List<CompetencyWeightMatrixViews.RowDto> rowDtos = new ArrayList<>();
        for (DisciplineCurriculum dc : rowEntities) {
            List<CompetencyWeightMatrixViews.CellDto> cells = new ArrayList<>();
            Map<UUID, DisciplineCurriculumCompetency> rowLinks = linkLookup.getOrDefault(dc.getId(), Map.of());
            for (Competency col : columns) {
                DisciplineCurriculumCompetency link = rowLinks.get(col.getId());
                if (link == null)
                    cells.add(new CompetencyWeightMatrixViews.CellDto(null, false, 0.0));
                else
                    cells.add(new CompetencyWeightMatrixViews.CellDto(link.getId(), true, roundToHundredths(link.getWeight())));
            }
            rowDtos.add(new CompetencyWeightMatrixViews.RowDto(dc.getId(), dc.getName(), cells));
        }

        return new CompetencyWeightMatrixViews.MatrixDto(columnDtos, rowDtos);
    }

    @Override
    @Transactional
    public void saveCompetencyWeightMatrix(UUID groupId, List<CompetencyWeightMatrixViews.LinkWeightUpdate> updates) {
        findGroupById(groupId);
        List<DisciplineCurriculumCompetency> links =
                disciplineCurriculumCompetencyRepository.findByDisciplineCurriculum_Group_Id(groupId);
        Map<UUID, DisciplineCurriculumCompetency> byId = links.stream()
                .collect(Collectors.toMap(DisciplineCurriculumCompetency::getId, l -> l));

        for (CompetencyWeightMatrixViews.LinkWeightUpdate u : updates) {
            DisciplineCurriculumCompetency link = byId.get(u.linkId());
            if (link == null)
                throw new IllegalArgumentException("Связь не относится к выбранной группе: " + u.linkId());
            double w = u.weight();
            if (w < -WEIGHT_SUM_EPS || w > 1.0 + WEIGHT_SUM_EPS)
                throw new IllegalArgumentException("Вес должен быть в диапазоне [0, 1]");
            if (!isMultipleOfHundredth(w))
                throw new IllegalArgumentException("Вес указывается с точностью до сотых (два знака после запятой).");
            link.setWeight(roundToHundredths(w));
        }

        Map<UUID, Double> sumByCompetency = new HashMap<>();
        for (DisciplineCurriculumCompetency link : byId.values()) {
            sumByCompetency.merge(link.getCompetency().getId(), link.getWeight(), Double::sum);
        }

        for (Competency c : competencyRepository.findByGroup_IdOrderByAbbreviationAsc(groupId)) {
            boolean hasLinks = byId.values().stream().anyMatch(l -> l.getCompetency().getId().equals(c.getId()));
            if (!hasLinks)
                continue;
            double sum = sumByCompetency.getOrDefault(c.getId(), 0.0);
            if (Math.abs(sum - 1.0) > WEIGHT_SUM_EPS)
                throw new IllegalArgumentException(
                        "Сумма весов по компетенции «" + c.getAbbreviation()
                                + "» должна быть равна 1 (сейчас " + String.format(Locale.US, "%.2f", sum)
                                + "). Заполните матрицу корректно.");
        }

        disciplineCurriculumCompetencyRepository.saveAll(byId.values());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<GroupDto> findGroupsByPartOfName(String partOfName) {
        List<Group> groups = groupRepository.findAllGroupsByPartOfName(partOfName);
        return groupMapper.toDto(groups);
    }
}
