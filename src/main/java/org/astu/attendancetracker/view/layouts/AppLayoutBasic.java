package org.astu.attendancetracker.view.layouts;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import org.astu.attendancetracker.view.MainView;
import org.astu.attendancetracker.view.pages.GroupList;

public class AppLayoutBasic extends AppLayout {

    public AppLayoutBasic() {
        SideNav nav = getSideNav();

        addToDrawer(nav);
    }

    public SideNav getSideNav() {
        SideNav nav = new SideNav();

        SideNavItem dashboardLink = new SideNavItem("Группы",
                GroupList.class, VaadinIcon.GROUP.create());
        SideNavItem inboxLink = new SideNavItem("Пользователи", MainView.class,
                VaadinIcon.USER.create());
//        SideNavItem calendarLink = new SideNavItem("Отчёты",
//                MainView.class, VaadinIcon.CALENDAR.create());
        SideNavItem vaadinLink = new SideNavItem("Vaadin website",
                "https://vaadin.com", VaadinIcon.VAADIN_H.create());

        nav.addItem(dashboardLink, inboxLink, vaadinLink);

        return nav;
    }
}
