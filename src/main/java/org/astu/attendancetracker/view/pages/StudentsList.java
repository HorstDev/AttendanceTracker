package org.astu.attendancetracker.view.pages;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import org.astu.attendancetracker.presentation.services.AuthService;
import org.astu.attendancetracker.presentation.services.GroupService;
import org.astu.attendancetracker.presentation.services.ProfileService;
import org.astu.attendancetracker.presentation.viewModels.AuthorizationDto;
import org.astu.attendancetracker.presentation.viewModels.GroupDto;
import org.astu.attendancetracker.presentation.viewModels.StudentProfileDto;
import org.astu.attendancetracker.view.layouts.AppLayoutBasic;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Route(value = "/students", layout = AppLayoutBasic.class)
public class StudentsList extends HorizontalLayout {

    private final GroupService groupService;
    private final ProfileService profileService;
    private final AuthService authService;

    Grid<GroupDto> groupsGrid = new Grid<>();
    GroupDto selectedGroup;
    private Text textForChooseGroup = new Text("Группа не выбрана");

    List<StudentProfileDto> studentProfiles = new ArrayList<>();
    Grid<StudentProfileDto> studentProfilesGrid = new Grid<>();

    TextField studentNameTextField = new TextField("Имя студента");
    Button addStudentToGroupButton = new Button("Добавить в группу");

    public StudentsList(GroupService groupService, ProfileService profileService, AuthService authService) {
        initGroupsGrid();
        initStudentProfilesGrid();
        initPageComponents();

        this.groupService = groupService;
        this.profileService = profileService;
        this.authService = authService;


        VerticalLayout leftLayout = leftLayout();
        VerticalLayout rightLayout = rightLayout();

        add(leftLayout, rightLayout);
    }

    private VerticalLayout leftLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("50%");

        TextField tf = new TextField("Введите группу");
        Button findGroupsByPartOfNameButton = new Button("Найти группы");

        findGroupsByPartOfNameButton.addClickListener(e -> {
           List<GroupDto> groupsList = groupService.findGroupsByPartOfName(tf.getValue());
           groupsGrid.setItems(groupsList);
        });

        layout.add(tf, findGroupsByPartOfNameButton, groupsGrid);
        return layout;
    }

    private VerticalLayout rightLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("50%");

        addStudentToGroupButton.addClickListener(e -> {
           if (studentNameTextField.getValue().isEmpty()) {
               Notification.show("Ошибка: не введено имя студента!", 7000, Notification.Position.BOTTOM_END);
           } else if (selectedGroup == null) {
               Notification.show("Ошибка: не выбрана группа", 7000, Notification.Position.BOTTOM_END);
           } else {
               StudentProfileDto studentProfile = profileService.addStudentToGroup(selectedGroup.getId(), studentNameTextField.getValue());
               studentProfiles.add(studentProfile);
               studentProfilesGrid.setItems(studentProfiles);
           }
        });

        layout.add(textForChooseGroup, studentNameTextField, addStudentToGroupButton, studentProfilesGrid);
        return layout;
    }

    private void initPageComponents() {
        studentNameTextField.setVisible(false);
        addStudentToGroupButton.setVisible(false);
    }

    private void initGroupsGrid() {
        groupsGrid.addColumn(GroupDto::getName)
                .setHeader("Название");

        groupsGrid.addSelectionListener(e -> {
            selectedGroup = e.getFirstSelectedItem().get();
            textForChooseGroup.setText("Студенты в группе " + selectedGroup.getName() + ":");

            studentProfiles = profileService.getStudentProfilesByGroupId(selectedGroup.getId());
            studentProfilesGrid.setItems(studentProfiles);

            studentNameTextField.setVisible(true);
            addStudentToGroupButton.setVisible(true);
        });
    }

    private void initStudentProfilesGrid() {
        studentProfilesGrid.addColumn(StudentProfileDto::getName)
                .setHeader("Студент");
        studentProfilesGrid.addColumn(new ComponentRenderer<>(student -> new Button("Изменить данные для входа", e -> {
            Dialog dialog = new Dialog();
            dialog.setModal(true);
            dialog.setDraggable(false);
            dialog.setResizable(false);

            TextField login = new TextField("Логин");
            TextField password = new TextField("Пароль");

            Button saveButton = new Button("Сохранить", event -> {
                AuthorizationDto authDto = AuthorizationDto.builder()
                        .login(login.getValue())
                        .password(password.getValue())
                        .build();

                try {
                    authService.changeAuthorizationData(student.getId(), authDto);
                    Notification.show("Данные для входа успешно изменены!");
                } catch (Exception ex) {
                    Notification.show("Ошибка при изменении данных для входа: " + ex.getMessage());
                    log.error(ex.getMessage());
                }


                dialog.close();
            });

            VerticalLayout layout = new VerticalLayout(
                    login,
                    password,
                    saveButton
            );

            dialog.add(layout);
            dialog.open();
        })));
    }
}
