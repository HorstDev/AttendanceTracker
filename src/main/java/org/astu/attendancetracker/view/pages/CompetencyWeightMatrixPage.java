package org.astu.attendancetracker.view.pages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.presentation.services.GroupService;
import org.astu.attendancetracker.presentation.viewModels.CompetencyWeightMatrixViews;
import org.astu.attendancetracker.view.layouts.AppLayoutBasic;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Route(value = "/competency-weights", layout = AppLayoutBasic.class)
public class CompetencyWeightMatrixPage extends VerticalLayout {

    private final GroupService groupService;

    private final ComboBox<Group> groupCombo = new ComboBox<>("Группа");
    private final Button saveButton = new Button("Загрузить матрицу");
    private final Button randomButton = new Button("РАНДОМОЕ ЗАПОЛНЕНИЕ");
    private final Grid<MatrixRowVm> grid = new Grid<>();
    private final Span hint = new Span("Выберите группу с загруженным учебным планом.");

    private List<CompetencyWeightMatrixViews.ColumnDto> columns = List.of();
    private List<MatrixRowVm> rows = List.of();

    public CompetencyWeightMatrixPage(GroupService groupService) {
        this.groupService = groupService;

        setPadding(true);
        setSpacing(true);

        groupCombo.setItemLabelGenerator(Group::getName);
        groupCombo.setWidth("24em");
        groupCombo.setItems(groupService.getAllGroups());

        saveButton.addClickListener(e -> onSave());
        randomButton.addClickListener(e -> onRandomFill());

        HorizontalLayout toolbar = new HorizontalLayout(groupCombo, saveButton, randomButton);
        toolbar.setAlignItems(Alignment.END);
        toolbar.setWidthFull();

        grid.setWidthFull();
        grid.setAllRowsVisible(true);

        add(new H3("Матрица весов дисциплин УП и компетенций"), hint, toolbar, grid);

        groupCombo.addValueChangeListener(e -> {
            Group g = e.getValue();
            if (g == null) {
                hint.setText("Выберите группу с загруженным учебным планом.");
                clearGrid();
                return;
            }
            loadMatrix(g.getId());
        });
    }

    private void clearGrid() {
        columns = List.of();
        rows = List.of();
        grid.removeAllColumns();
        grid.setItems();
    }

    private void loadMatrix(UUID groupId) {
        try {
            CompetencyWeightMatrixViews.MatrixDto matrix = groupService.getCompetencyWeightMatrix(groupId);
            columns = matrix.columns();
            if (columns.isEmpty() || matrix.rows().isEmpty()) {
                hint.setText("Для группы нет дисциплин УП или компетенций. Загрузите учебный план.");
                clearGrid();
                return;
            }
            hint.setText("Строки - дисциплины из учебного плана, столбцы - аббревиатуры компетенций. "
                    + "Где связи нет, отображается 0. По каждому столбцу сумма должна быть 1.");

            rows = new ArrayList<>();
            for (CompetencyWeightMatrixViews.RowDto rowDto : matrix.rows()) {
                List<MatrixCellVm> cells = new ArrayList<>();
                for (CompetencyWeightMatrixViews.CellDto cellDto : rowDto.cells()) {
                    if (!cellDto.linked()) {
                        cells.add(MatrixCellVm.unlinked());
                    } else {
                        NumberField nf = createWeightField(cellDto.weight());
                        cells.add(MatrixCellVm.linked(cellDto.linkId(), nf));
                    }
                }
                rows.add(new MatrixRowVm(rowDto.disciplineName(), cells));
            }

            rebuildGridColumns();
            grid.setItems(rows);
        } catch (Exception ex) {
            Notification.show("Ошибка: " + ex.getMessage(), 6000, Notification.Position.BOTTOM_END);
            clearGrid();
        }
    }

    private void rebuildGridColumns() {
        grid.removeAllColumns();
        grid.addColumn(MatrixRowVm::disciplineName)
                .setHeader("Дисциплина (УП)")
                .setFrozen(true)
                .setFlexGrow(0)
                .setWidth("18em");

        for (int c = 0; c < columns.size(); c++) {
            final int colIndex = c;
            CompetencyWeightMatrixViews.ColumnDto col = columns.get(c);
            grid.addColumn(new ComponentRenderer<>(row -> {
                MatrixCellVm cell = row.cells().get(colIndex);
                if (!cell.linked())
                    return new Span("0");
                return cell.field();
            })).setHeader(col.abbreviation()).setAutoWidth(true).setFlexGrow(0);
        }
    }

    private void onRandomFill() {
        if (rows.isEmpty() || columns.isEmpty()) {
            Notification.show("Сначала выберите группу и загрузите матрицу.", 4000, Notification.Position.BOTTOM_END);
            return;
        }
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int c = 0; c < columns.size(); c++) {
            List<NumberField> fieldsInColumn = new ArrayList<>();
            for (MatrixRowVm row : rows) {
                MatrixCellVm cell = row.cells().get(c);
                if (cell.linked() && cell.field() != null)
                    fieldsInColumn.add(cell.field());
            }
            if (fieldsInColumn.isEmpty())
                continue;
            int n = fieldsInColumn.size();
            int left = 100;
            for (int i = 0; i < n - 1; i++) {
                int minForRest = n - 1 - i;
                int max = left - minForRest;
                int cents = rnd.nextInt(1, max + 1);
                setWeightFieldHundredths(fieldsInColumn.get(i), cents);
                left -= cents;
            }
            setWeightFieldHundredths(fieldsInColumn.get(n - 1), left);
        }
        for (MatrixRowVm row : rows) {
            for (MatrixCellVm cell : row.cells()) {
                if (cell.linked() && cell.field() != null)
                    applyWeightEmptyState(cell.field());
            }
        }
        Notification.show("Матрица заполнена случайно (по каждой компетенции сумма = 1). Сохраните при необходимости.",
                5000, Notification.Position.BOTTOM_END);
    }

    private void onSave() {
        Group g = groupCombo.getValue();
        if (g == null || rows.isEmpty()) {
            Notification.show("Нет данных для сохранения.", 4000, Notification.Position.BOTTOM_END);
            return;
        }
        boolean hasEmpty = false;
        for (MatrixRowVm row : rows) {
            for (MatrixCellVm cell : row.cells()) {
                if (!cell.linked() || cell.field() == null)
                    continue;
                applyWeightEmptyState(cell.field());
                if (cell.field().getValue() == null)
                    hasEmpty = true;
            }
        }
        if (hasEmpty) {
            Notification.show("Укажите вес для всех редактируемых ячеек.", 5000, Notification.Position.BOTTOM_END);
            return;
        }

        List<CompetencyWeightMatrixViews.LinkWeightUpdate> updates = new ArrayList<>();
        for (MatrixRowVm row : rows) {
            for (MatrixCellVm cell : row.cells()) {
                if (!cell.linked() || cell.field() == null || cell.linkId() == null)
                    continue;
                Double v = cell.field().getValue();
                updates.add(new CompetencyWeightMatrixViews.LinkWeightUpdate(cell.linkId(), roundToHundredths(v)));
            }
        }
        try {
            groupService.saveCompetencyWeightMatrix(g.getId(), updates);
            Notification.show("Веса сохранены.", 4000, Notification.Position.BOTTOM_END);
            loadMatrix(g.getId());
        } catch (IllegalArgumentException ex) {
            Notification.show(ex.getMessage(), 8000, Notification.Position.BOTTOM_END);
        } catch (Exception ex) {
            Notification.show("Ошибка: " + ex.getMessage(), 6000, Notification.Position.BOTTOM_END);
        }
    }

    private static NumberField createWeightField(double initialWeight) {
        NumberField nf = new NumberField();
        nf.setWidth("6em");
        nf.setManualValidation(true);
        nf.setMin(0);
        nf.setMax(1);
        nf.setStep(0.01);
        setWeightFieldHundredths(nf, toCents(initialWeight));
        nf.addBlurListener(ev -> {
            Double v = nf.getValue();
            if (v != null)
                setWeightFieldHundredths(nf, toCents(v));
            applyWeightEmptyState(nf);
        });
        nf.addValueChangeListener(ev -> applyWeightEmptyState(nf));
        return nf;
    }

    private static void setWeightFieldHundredths(NumberField nf, int cents) {
        nf.setValue(hundredthsFromCents(cents));
        nf.setInvalid(false);
        nf.setErrorMessage(null);
    }

    private static void applyWeightEmptyState(NumberField nf) {
        boolean empty = nf.getValue() == null;
        nf.setInvalid(empty);
        nf.setErrorMessage(empty ? "Укажите вес" : null);
    }

    private static int toCents(double w) {
        return (int) Math.round(w * 100.0);
    }

    /** Значение для NumberField: ровно два знака после запятой, без артефактов float. */
    private static double hundredthsFromCents(int cents) {
        return Double.parseDouble(String.format(Locale.US, "%.2f", cents / 100.0));
    }

    private static double roundToHundredths(double w) {
        return hundredthsFromCents(toCents(w));
    }

    private record MatrixRowVm(String disciplineName, List<MatrixCellVm> cells) {}

    private record MatrixCellVm(UUID linkId, boolean linked, NumberField field) {
        static MatrixCellVm unlinked() {
            return new MatrixCellVm(null, false, null);
        }

        static MatrixCellVm linked(UUID linkId, NumberField field) {
            return new MatrixCellVm(linkId, true, field);
        }
    }
}
