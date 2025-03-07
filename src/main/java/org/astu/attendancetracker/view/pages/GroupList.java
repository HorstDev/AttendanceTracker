package org.astu.attendancetracker.view.pages;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import io.jsonwebtoken.lang.Collections;
import org.astu.attendancetracker.core.application.common.dto.apitable.ApiTableGroupSchedule;
import org.astu.attendancetracker.core.domain.Group;
import org.astu.attendancetracker.core.domain.TeacherProfile;
import org.astu.attendancetracker.presentation.services.GroupService;
import org.astu.attendancetracker.presentation.services.ProfileService;
import org.astu.attendancetracker.view.layouts.AppLayoutBasic;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Route(value = "/groups", layout = AppLayoutBasic.class)
public class GroupList extends HorizontalLayout {

    private final ProfileService profileService;
    private final GroupService groupService;

    // Группы, в которых принимает участие преподаватель
    Grid<String> teachableGroups = new Grid<>();
    Grid<Group> groupsInDatabase = new Grid<>(Group.class);
    RadioButtonGroup<String> radioGroupWithSemester = new RadioButtonGroup<>();

    public GroupList(ProfileService profileService, GroupService groupService) {
        this.profileService = profileService;
        this.groupService = groupService;

        VerticalLayout rightLayout = rightLayout();
        VerticalLayout leftLayout = leftLayout();

        add(rightLayout, leftLayout);
    }

    public VerticalLayout rightLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("50%");

        TextField tf = new TextField("Преподаватель, чьи группы показать");
        tf.setWidth("100%");
        Button buttonToUpload = new Button("Показать");
        Grid<TeacherProfile> teacherProfilesGrid = getTeacherProfilesGrid();

        buttonToUpload.addClickListener(buttonClickEvent -> {
            List<TeacherProfile> teacherProfilesWithPartOfName = profileService.getTeachersWithPartOfName(tf.getValue());
            teacherProfilesGrid.setItems(teacherProfilesWithPartOfName);
        });

        initTeachableGroupsGrid();
        layout.add(teachableGroups);

        layout.add(tf, buttonToUpload, teacherProfilesGrid, teachableGroups);
        return layout;
    }

    // Grid с преподавателями
    private Grid<TeacherProfile> getTeacherProfilesGrid() {
        Grid<TeacherProfile> grid = new Grid<>(TeacherProfile.class);
        grid.setColumns();
        grid.addColumn("name").setHeader("Имена преподавателей");
        grid.asSingleSelect().addValueChangeListener(selectionEvent -> {
            TeacherProfile teacher = selectionEvent.getValue();
            CompletableFuture<HashSet<String>> groups = groupService.getAllGroupsForTeacher(teacher.getName());
            try {
                teachableGroups.setItems(groups.get());
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        return grid;
    }

    private void initTeachableGroupsGrid() {
        teachableGroups.addColumn(groupName -> groupName).setHeader("Группы преподавателя");
        teachableGroups.addColumn(new ComponentRenderer<>(groupName -> new Button("Загрузить в БД", e -> {
            if (groupsInDatabase.getListDataView().getItems().noneMatch(group -> group.getName().equals(groupName)))
                groupService.saveGroup(groupName);
            else
                Notification.show("Группа уже существует в БД!");
            groupsInDatabase.setItems(groupService.getAllGroups());
        })));
    }

    public VerticalLayout leftLayout() {
        VerticalLayout layout = new VerticalLayout();
        radioGroupWithSemester.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroupWithSemester.setLabel("Перед загрузкой семестра выберите, какой он по чётности");
        radioGroupWithSemester.setItems("Чётный", "Нечётный");

        initGroupsInDatabase();
        layout.add(radioGroupWithSemester, groupsInDatabase);

        return layout;
    }

    private void initGroupsInDatabase() {
        groupsInDatabase.setColumns(); // Очищаем все столбцы
        groupsInDatabase.addColumn("name").setHeader("Группы в БД");
        groupsInDatabase.setItems(groupService.getAllGroups());
        groupsInDatabase.addColumn(new ComponentRenderer<>(group -> new Button("Загрузить семестр", e -> {
            if (radioGroupWithSemester.getValue() == null) {
                Notification.show("Выберите, какой сейчас семестр!");
                return;
            }
            boolean isEvenSemester = radioGroupWithSemester.getValue().equals("Чётный");

            try {
                int currentSemester = group.currentSemester(isEvenSemester);
                CompletableFuture<ApiTableGroupSchedule> scheduleFuture = groupService.getApiTableGroupSchedule(group.getName());
                CompletableFuture<Integer> currentWeekFuture = groupService.getCurrentWeekNumber();

                ApiTableGroupSchedule apiTableGroupSchedule = scheduleFuture.get();
                int currentWeek = currentWeekFuture.get();
                groupService.uploadSemesterForGroup(group, apiTableGroupSchedule, currentWeek, currentSemester);
                Notification.show("Успешно загружен семестр №" + currentSemester + " для группы " + group.getName());
            } catch(Exception ex) {
                Notification.show(ex.getMessage());
            }
        })));
    }
}
