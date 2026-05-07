package com.ooad.calendar;

import com.ooad.calendar.config.Database;
import com.ooad.calendar.model.AppointmentModel;
import com.ooad.calendar.model.GroupMeetingModel;
import com.ooad.calendar.model.UserModel;
import com.ooad.calendar.presenter.AppointmentPresenter;
import com.ooad.calendar.view.CalendarFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class CalendarApplication {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                Database.initialize();

                AppointmentModel appointmentModel = new AppointmentModel();
                GroupMeetingModel groupMeetingModel = new GroupMeetingModel();
                UserModel userModel = new UserModel();
                CalendarFrame frame = new CalendarFrame(1, appointmentModel, groupMeetingModel, userModel);
                AppointmentPresenter presenter = new AppointmentPresenter(
                        appointmentModel,
                        groupMeetingModel,
                        frame::refreshCalendar
                );
                frame.setPresenter(presenter);
                frame.setVisible(true);
            } catch (Exception ex) {
                throw new IllegalStateException("Cannot start calendar application", ex);
            }
        });
    }
}
