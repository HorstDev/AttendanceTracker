package org.astu.attendancetracker.view.pages;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.astu.attendancetracker.presentation.services.GroupService;
import org.astu.attendancetracker.presentation.services.ProfileService;
import org.astu.attendancetracker.presentation.viewModels.GroupDto;
import org.astu.attendancetracker.presentation.viewModels.StudentProfileDto;
import org.astu.attendancetracker.view.layouts.AppLayoutBasic;

import java.util.List;

@Route(value = "/students", layout = AppLayoutBasic.class)
public class UsersList extends HorizontalLayout {

    private final GroupService groupService;
    private final ProfileService profileService;

    Grid<GroupDto> groupsGrid = new Grid<>();
    GroupDto selectedGroup;
    private Text textForChooseGroup = new Text("Группа не выбрана");

    Grid<StudentProfileDto> studentProfilesGrid = new Grid<>();

    public UsersList(GroupService groupService, ProfileService profileService) {
        initGroupsGrid();
        initStudentProfilesGrid();

        this.groupService = groupService;
        this.profileService = profileService;


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

        layout.add(textForChooseGroup, studentProfilesGrid);
        return layout;
    }

    private void initGroupsGrid() {
        groupsGrid.addColumn(GroupDto::getName)
                .setHeader("Название");

        groupsGrid.addSelectionListener(e -> {
            selectedGroup = e.getFirstSelectedItem().get();
            textForChooseGroup.setText("Студенты в группе : " + selectedGroup.getName());

            List<StudentProfileDto> studentProfiles = profileService.getStudentProfilesByGroupId(selectedGroup.getId());
            studentProfilesGrid.setItems(studentProfiles);
        });
    }

    private void initStudentProfilesGrid() {
        studentProfilesGrid.addColumn(StudentProfileDto::getName)
                .setHeader("Студент");
    }
}
