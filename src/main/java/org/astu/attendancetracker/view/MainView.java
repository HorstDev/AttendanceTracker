package org.astu.attendancetracker.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.astu.attendancetracker.view.layouts.AppLayoutBasic;

@Route(value = "", layout = AppLayoutBasic.class)
public class MainView extends VerticalLayout {

    public MainView() {
        TextField tf = new TextField("Привет");
        Button helloButton = new Button("Привет!", e -> {
            Notification.show("Ты нажал на кнопку привет");
        });

        add(tf, helloButton);




    }
}
