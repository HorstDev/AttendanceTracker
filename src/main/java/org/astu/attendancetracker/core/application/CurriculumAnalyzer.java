package org.astu.attendancetracker.core.application;

import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.astu.attendancetracker.core.application.common.exceptions.CurriculumReadException;
import org.astu.attendancetracker.core.domain.Discipline;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CurriculumAnalyzer {
    // Минимально приемлемый индекс Жаккара для совмещения названий дисциплин по схожести
    private static final double ACCEPTABLE_JACCARD_INDEX = 0.75;
    private static final int PLAN_PAGE = 3;
    private static final int ROW_START_DISCIPLINES = 5;
    private static final int COLUMN_START_SEMESTERS = 3;
    private static final int COLUMN_END_SEMESTERS = 6;
    private static final int COLUMN_DISCIPLINE_NAME = 2;
    // Столбец с семестрами, в которые проводится экзамен
    private static final int COLUMN_EXAM_SEMESTERS = 3;
    // Столбец с семестрами, в которые проводится зачет
    private static final int COLUMN_CREDIT_SEMESTERS = 4;
    // Столбец с семестрами, в которые проводится зачет с оценкой
    private static final int COLUMN_CREDIT_WITH_ASSESSMENT_SEMESTERS = 5;
    // Столбец с семестрами, в которые проводится курсовой проект
    private static final int COLUMN_COURSE_PROJECT_SEMESTERS = 6;
    // Столбец с компетенциями в строке дисциплины
    private static final int COLUMN_COMPETENCIES = 90;
    // Страница с расшифровкой компетенций
    private static final int COMPETENCY_DESCRIPTIONS_PAGE = 4;
    // Столбец с аббревиатурами компетенций на странице расшифровки
    private static final int COLUMN_COMPETENCY_ABBREVIATION = 1;
    // Столбец с описаниями компетенций на странице расшифровки
    private static final int COLUMN_COMPETENCY_DESCRIPTION = 3;

    public record CompetencyData(String abbreviation, String description) {}

    public static void uploadInformationForDisciplines(byte[] curriculumBytes, List<Discipline> disciplines) {

        if (curriculumBytes == null || disciplines.isEmpty())
            throw new RuntimeException("Недостаточно данных для загрузки информации о дисциплинах");

        try (ByteArrayInputStream curriculumInputStream = new ByteArrayInputStream(curriculumBytes);
             Workbook workbook = new XSSFWorkbook(curriculumInputStream)) {
            Sheet sheet = workbook.getSheetAt(PLAN_PAGE);
            int semester = disciplines.getFirst().getSemester();
            // Находим строки с дисциплинами текущего семестра
            List<Row> rowsWithDisciplinesAtSemester = rowsWithDisciplinesAtSemester(sheet, semester);
            // Строки в учебном плане и соответствующие им дисциплины
            HashMap<Discipline, Row> disciplinesAndRelateRows = relateRowsWithDisciplines(rowsWithDisciplinesAtSemester, disciplines);
            setTypeOfExamForDisciplines(disciplinesAndRelateRows, semester);
        } catch(IOException e) {
            throw new CurriculumReadException("Ошибка при чтении учебного плана");
        }
    }

    // Возвращает строки с дисциплинами, которые относятся к семестру
    private static List<Row> rowsWithDisciplinesAtSemester(Sheet sheet, int semester) {
        List<Row> rowsWithDisciplinesAtSemester = new ArrayList<>();

        for (int currentRow = ROW_START_DISCIPLINES; currentRow <= sheet.getLastRowNum(); currentRow++) {
            Row row = sheet.getRow(currentRow);
            HashSet<Integer> semestersForDiscipline = semesterNumbersForDisciplineInRow(row);
            if (semestersForDiscipline.contains(semester))
                rowsWithDisciplinesAtSemester.add(row);
        }

        return rowsWithDisciplinesAtSemester;
    }

    // Возвращает семестры, к которым относится дисциплина в строке
    private static HashSet<Integer> semesterNumbersForDisciplineInRow(Row row) {
        HashSet<Integer> semesters = new HashSet<>();

        for (int currentColumn = COLUMN_START_SEMESTERS; currentColumn <= COLUMN_END_SEMESTERS; currentColumn++) {
            Cell cell = row.getCell(currentColumn);

            List<Integer> semestersInCell = semestersInCell(cell);
            semesters.addAll(semestersInCell);
        }

        return semesters;
    }

    private static List<Integer> semestersInCell(Cell cell) {
        List<Integer> semesters = new ArrayList<>();

        if (!isCellEmpty(cell)) {
            String semestersInCell = cell.getStringCellValue();
            for(int i = 0; i < semestersInCell.length(); i++) {
                int semester = semestersInCell.charAt(i) - '0';
                semesters.add(semester);
            }
        }

        return semesters;
    }

    private static boolean isCellEmpty(Cell cell) {
        return cell == null || cell.getCellType() == CellType.BLANK;
    }

    // Соотносим строки с названиями дисциплин и дисциплины с помощью коэффициента Жаккара
    private static HashMap<Discipline, Row> relateRowsWithDisciplines(List<Row> rows, List<Discipline> disciplines) {
        if (rows.isEmpty())
            throw new RuntimeException("Отсутствуют строки с соответствующими дисциплинами в учебном плане");

        HashMap<Discipline, Row> rowsWithDisciplines = new HashMap<>();

        for (Discipline discipline : disciplines) {
            double maxSimilarity = 0;           // максимальный коэффициент сходства с строкой
            Row relatedRow = rows.getFirst();   // строка, отвечающая максимальному сходству с дисциплиной

            for (Row row : rows) {
                JaccardSimilarity jaccardSimilarity = new JaccardSimilarity();
                String disciplineName = discipline.getName().toLowerCase().trim();
                String disciplineNameInRow = row.getCell(COLUMN_DISCIPLINE_NAME).getStringCellValue().toLowerCase().trim();
                double similarity = jaccardSimilarity.apply(disciplineName, disciplineNameInRow);

                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    relatedRow = row;
                }
            }

            // Если максимально найденный коэффициент Жаккара меньше допустимого, то дисциплина имеет недостаточное сходство
            // со всеми строками в учебном плане
            if (maxSimilarity < ACCEPTABLE_JACCARD_INDEX)
                relatedRow = null;

            rowsWithDisciplines.put(discipline, relatedRow);
        }
        return rowsWithDisciplines;
    }

    // Извлекает компетенции для дисциплин из учебного плана
    public static Map<Discipline, List<CompetencyData>> extractCompetenciesForDisciplines(
            byte[] curriculumBytes, List<Discipline> disciplines) {

        if (curriculumBytes == null || disciplines.isEmpty())
            throw new RuntimeException("Недостаточно данных для извлечения компетенций");

        try (ByteArrayInputStream stream = new ByteArrayInputStream(curriculumBytes);
             Workbook workbook = new XSSFWorkbook(stream)) {
            Sheet planSheet = workbook.getSheetAt(PLAN_PAGE);
            int semester = disciplines.getFirst().getSemester();

            List<Row> rowsAtSemester = rowsWithDisciplinesAtSemester(planSheet, semester);
            HashMap<Discipline, Row> disciplinesAndRows = relateRowsWithDisciplines(rowsAtSemester, disciplines);

            // Собираем аббревиатуры компетенций по каждой дисциплине
            Map<Discipline, List<String>> disciplineToAbbreviations = new HashMap<>();
            Set<String> allAbbreviations = new HashSet<>();

            for (Map.Entry<Discipline, Row> entry : disciplinesAndRows.entrySet()) {
                Row row = entry.getValue();
                if (row == null) continue;

                Cell cell = row.getCell(COLUMN_COMPETENCIES);
                if (isCellEmpty(cell)) continue;

                List<String> abbreviations = Arrays.stream(cell.getStringCellValue().split(";"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());

                disciplineToAbbreviations.put(entry.getKey(), abbreviations);
                allAbbreviations.addAll(abbreviations);
            }

            // Находим описания компетенций на странице расшифровки
            Map<String, String> abbreviationToDescription = new HashMap<>();
            Sheet competencySheet = workbook.getSheetAt(COMPETENCY_DESCRIPTIONS_PAGE);

            for (int i = 0; i <= competencySheet.getLastRowNum(); i++) {
                Row row = competencySheet.getRow(i);
                if (row == null) continue;

                Cell abbreviationCell = row.getCell(COLUMN_COMPETENCY_ABBREVIATION);
                if (isCellEmpty(abbreviationCell)) continue;

                String abbreviation = abbreviationCell.getStringCellValue().trim();
                if (allAbbreviations.contains(abbreviation)) {
                    Cell descriptionCell = row.getCell(COLUMN_COMPETENCY_DESCRIPTION);
                    String description = isCellEmpty(descriptionCell) ? "" : descriptionCell.getStringCellValue().trim();
                    abbreviationToDescription.put(abbreviation, description);
                }
            }

            // Строим итоговый результат: дисциплина → список CompetencyData
            Map<Discipline, List<CompetencyData>> result = new HashMap<>();
            for (Map.Entry<Discipline, List<String>> entry : disciplineToAbbreviations.entrySet()) {
                List<CompetencyData> competencies = entry.getValue().stream()
                        .map(abbr -> new CompetencyData(abbr, abbreviationToDescription.getOrDefault(abbr, "")))
                        .collect(Collectors.toList());
                result.put(entry.getKey(), competencies);
            }

            return result;
        } catch (IOException e) {
            throw new CurriculumReadException("Ошибка при чтении учебного плана");
        }
    }

    // Устанавливает тип экзамена для дисциплин
    private static void setTypeOfExamForDisciplines(HashMap<Discipline, Row> disciplinesAndRelatedRows, int semester) {

        for (Discipline discipline : disciplinesAndRelatedRows.keySet()) {
            Row row = disciplinesAndRelatedRows.get(discipline);
            if (row == null)
                continue;

            // Проходимся по колонкам  с семестрами в строке
            for (int currentColumn = COLUMN_START_SEMESTERS; currentColumn <= COLUMN_END_SEMESTERS; currentColumn++) {
                Cell cell = row.getCell(currentColumn);

                List<Integer> semestersInCell = semestersInCell(cell);
                if (semestersInCell.contains(semester)) {
                    switch (currentColumn){
                        case COLUMN_EXAM_SEMESTERS:
                            discipline.setExam(true);
                            break;
                        case COLUMN_CREDIT_SEMESTERS:
                            discipline.setCredit(true);
                            break;
                        case COLUMN_CREDIT_WITH_ASSESSMENT_SEMESTERS:
                            discipline.setCreditWithAssessment(true);
                            break;
                        case COLUMN_COURSE_PROJECT_SEMESTERS:
                            discipline.setCourseProject(true);
                            break;
                    }
                }
            }
        }
    }
}
