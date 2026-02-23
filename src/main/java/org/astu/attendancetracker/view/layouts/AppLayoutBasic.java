package org.astu.attendancetracker.view.layouts;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import org.astu.attendancetracker.view.MainView;
import org.astu.attendancetracker.view.pages.GroupList;
import org.astu.attendancetracker.view.pages.TeachersList;
import org.astu.attendancetracker.view.pages.StudentsList;

public class AppLayoutBasic extends AppLayout {

    public AppLayoutBasic() {
        SideNav nav = getSideNav();

        addToDrawer(nav);
    }

    public SideNav getSideNav() {
        SideNav nav = new SideNav();

        SideNavItem groupsLink = new SideNavItem("Группы",
                GroupList.class, VaadinIcon.GROUP.create());
        SideNavItem usersLink = new SideNavItem("Студенты", StudentsList.class,
                VaadinIcon.USER.create());
        SideNavItem teachersLink = new SideNavItem("Преподаватели", TeachersList.class,
                VaadinIcon.BOOK.create());
        SideNavItem calendarLink = new SideNavItem("Отчёты",
                MainView.class, VaadinIcon.CALENDAR.create());
        SideNavItem vaadinLink = new SideNavItem("Vaadin website",
                "https://vaadin.com", VaadinIcon.VAADIN_H.create());

        nav.addItem(groupsLink, usersLink, teachersLink, calendarLink, vaadinLink);

        return nav;
    }
}
