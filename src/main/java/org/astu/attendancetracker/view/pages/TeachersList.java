package org.astu.attendancetracker.view.pages;

import com.vaadin.flow.component.UI;
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
import org.astu.attendancetracker.core.domain.TeacherProfile;
import org.astu.attendancetracker.presentation.services.AuthService;
import org.astu.attendancetracker.presentation.services.GroupService;
import org.astu.attendancetracker.presentation.services.ProfileService;
import org.astu.attendancetracker.presentation.viewModels.AuthorizationDto;
import org.astu.attendancetracker.presentation.viewModels.TeacherProfileDto;
import org.astu.attendancetracker.view.layouts.AppLayoutBasic;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Route(value = "/teachers", layout = AppLayoutBasic.class)
public class TeachersList extends HorizontalLayout {

    private final ProfileService profileService;
    private final AuthService authService;


    public TeachersList(ProfileService profileService, AuthService authService) {
        this.profileService = profileService;
        this.authService = authService;

        add(layoutWithTeachers());
    }

    public VerticalLayout layoutWithTeachers() {
        VerticalLayout layout = new VerticalLayout();

        TextField tf = new TextField("Преподаватель:");
        tf.setWidth("100%");
        Button buttonToUpload = new Button("Показать преподавателей");

        Grid<TeacherProfileDto> teacherProfilesGrid = getTeacherProfilesGrid();

        buttonToUpload.addClickListener(buttonClickEvent -> {
            List<TeacherProfileDto> teacherProfilesWithPartOfName = profileService.getTeachersWithPartOfName(tf.getValue());
            teacherProfilesGrid.setItems(teacherProfilesWithPartOfName);
        });

        layout.add(tf, buttonToUpload, teacherProfilesGrid);
        return layout;
    }

    // Grid с преподавателями
    private Grid<TeacherProfileDto> getTeacherProfilesGrid() {
        Grid<TeacherProfileDto> grid = new Grid<>(TeacherProfileDto.class);
        grid.setColumns();
        grid.addColumn("name").setHeader("Имена преподавателей");

        grid.addColumn(new ComponentRenderer<>(student -> new Button("Изменить данные для входа", e -> {
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

        return grid;
    }
}
