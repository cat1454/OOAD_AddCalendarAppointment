package com.ooad.calendar.view;

import com.ooad.calendar.entity.Appointment;
import com.ooad.calendar.entity.GroupMeeting;
import com.ooad.calendar.entity.ReminderInfo;
import com.ooad.calendar.entity.User;
import com.ooad.calendar.model.AppointmentModel;
import com.ooad.calendar.model.DemoDataModel;
import com.ooad.calendar.model.GroupMeetingModel;
import com.ooad.calendar.model.UserModel;
import com.ooad.calendar.presenter.AppointmentPresenter;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class CalendarFrame extends JFrame {
    private static final Color GOOGLE_BLUE = new Color(26, 115, 232);
    private int currentUserId;
    private final AppointmentModel appointmentModel;
    private final GroupMeetingModel groupMeetingModel;
    private final UserModel userModel;
    private final DemoDataModel demoDataModel;
    private final CalendarWeekPanel weekPanel;
    private final JLabel titleLabel = new JLabel();
    private final JPanel miniMonthPanel = new JPanel();
    private final DefaultListModel<User> userListModel = new DefaultListModel<>();
    private final JList<User> userList = new JList<>(userListModel);
    private final DefaultListModel<GroupMeeting> meetingListModel = new DefaultListModel<>();
    private final JList<GroupMeeting> meetingList = new JList<>(meetingListModel);
    private final DefaultListModel<Appointment> appointmentListModel = new DefaultListModel<>();
    private final JList<Appointment> appointmentList = new JList<>(appointmentListModel);
    private final DefaultListModel<ReminderInfo> reminderListModel = new DefaultListModel<>();
    private final JList<ReminderInfo> reminderList = new JList<>(reminderListModel);
    private JScrollPane calendarScrollPane;
    private LocalDate today = systemToday();
    private LocalDate selectedDate = today;
    private LocalDate visibleWeekStart;
    private AppointmentPresenter presenter;

    public CalendarFrame(int currentUserId, AppointmentModel appointmentModel, GroupMeetingModel groupMeetingModel,
                         UserModel userModel, DemoDataModel demoDataModel) {
        super("Calendar");
        this.currentUserId = currentUserId;
        this.appointmentModel = appointmentModel;
        this.groupMeetingModel = groupMeetingModel;
        this.userModel = userModel;
        this.demoDataModel = demoDataModel;
        this.weekPanel = new CalendarWeekPanel(this::openAddDialog, this::openEditDialog);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 760));
        setLocationByPlatform(true);
        buildUi();
        refreshUsers();
        refreshCalendar(true);
    }

    public void setPresenter(AppointmentPresenter presenter) {
        this.presenter = presenter;
    }

    public void refreshCalendar() {
        refreshCalendar(false);
    }

    private void refreshCalendar(boolean scrollToCurrentTime) {
        today = systemToday();
        visibleWeekStart = startOfWeek(selectedDate);
        List<Appointment> appointments = appointmentModel.findByWeek(currentUserId, visibleWeekStart);
        titleLabel.setText(selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        weekPanel.setWeek(visibleWeekStart, today, selectedDate, appointments);
        rebuildMiniMonth();
        refreshSidebarDetails();
        if (scrollToCurrentTime) {
            scrollToCurrentTime();
        }
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);

        calendarScrollPane = new JScrollPane(weekPanel);
        calendarScrollPane.setColumnHeaderView(weekPanel.getHeaderComponent());
        calendarScrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(232, 234, 237)));
        calendarScrollPane.getVerticalScrollBar().setUnitIncrement(18);
        root.add(calendarScrollPane, BorderLayout.CENTER);
        setContentPane(root);
        pack();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(14, 18, 12, 18));
        header.setBackground(Color.WHITE);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);
        JLabel menu = new JLabel("☰");
        menu.setFont(new Font("SansSerif", Font.PLAIN, 24));
        JLabel logo = new JLabel("▣");
        logo.setForeground(GOOGLE_BLUE);
        logo.setFont(new Font("SansSerif", Font.BOLD, 28));
        JLabel appName = new JLabel("Calendar");
        appName.setFont(new Font("SansSerif", Font.PLAIN, 28));
        left.add(menu);
        left.add(logo);
        left.add(appName);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        center.setOpaque(false);
        JButton today = roundedButton("Today");
        JButton prev = flatButton("‹");
        JButton next = flatButton("›");
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 26));
        today.addActionListener(e -> goToToday());
        prev.addActionListener(e -> {
            selectedDate = selectedDate.minusWeeks(1);
            refreshCalendar();
        });
        next.addActionListener(e -> {
            selectedDate = selectedDate.plusWeeks(1);
            refreshCalendar();
        });
        center.add(today);
        center.add(prev);
        center.add(next);
        center.add(titleLabel);

        header.add(left, BorderLayout.WEST);
        header.add(center, BorderLayout.CENTER);
        return header;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(250, 10));
        sidebar.setBorder(new EmptyBorder(16, 18, 16, 18));
        sidebar.setBackground(Color.WHITE);

        JPanel top = new JPanel(new BorderLayout(0, 18));
        top.setOpaque(false);
        JButton create = roundedButton("+  Create");
        create.setFont(new Font("SansSerif", Font.PLAIN, 16));
        create.setPreferredSize(new Dimension(134, 52));
        create.addActionListener(e -> openAddDialog(selectedDate.atTime(9, 0), selectedDate.atTime(10, 0)));
        JPanel createActions = new JPanel(new BorderLayout(0, 8));
        createActions.setOpaque(false);
        createActions.add(create, BorderLayout.NORTH);

        JPanel demoActions = new JPanel(new java.awt.GridLayout(0, 1, 0, 6));
        demoActions.setOpaque(false);
        JButton seedDemo = roundedButton("Tạo dữ liệu demo");
        JButton clearData = flatButton("Xóa dữ liệu");
        seedDemo.addActionListener(e -> seedDemoData());
        clearData.addActionListener(e -> clearData());
        demoActions.add(seedDemo);
        demoActions.add(clearData);
        createActions.add(demoActions, BorderLayout.CENTER);

        top.add(createActions, BorderLayout.NORTH);
        miniMonthPanel.setOpaque(false);
        top.add(miniMonthPanel, BorderLayout.CENTER);

        JPanel centerStack = new JPanel(new BorderLayout(0, 12));
        centerStack.setOpaque(false);
        centerStack.add(buildDetailTabs(), BorderLayout.CENTER);

        JPanel calendars = new JPanel(new BorderLayout(0, 8));
        calendars.setOpaque(false);
        JLabel calendarsTitle = new JLabel("My calendars");
        calendarsTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        calendars.add(calendarsTitle, BorderLayout.NORTH);

        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setFixedCellHeight(34);
        userList.setBorder(new EmptyBorder(4, 0, 4, 0));
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && userList.getSelectedValue() != null) {
                currentUserId = userList.getSelectedValue().getId();
                refreshCalendar();
            }
        });
        JScrollPane usersScrollPane = new JScrollPane(userList);
        usersScrollPane.setBorder(BorderFactory.createLineBorder(new Color(232, 234, 237)));
        calendars.add(usersScrollPane, BorderLayout.CENTER);

        JPanel userActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        userActions.setOpaque(false);
        JButton addUser = flatButton("+ User");
        JButton deleteUser = flatButton("Delete");
        addUser.addActionListener(e -> addUser());
        deleteUser.addActionListener(e -> deleteSelectedUser());
        userActions.add(addUser);
        userActions.add(deleteUser);
        calendars.add(userActions, BorderLayout.SOUTH);
        centerStack.add(calendars, BorderLayout.SOUTH);
        sidebar.add(top, BorderLayout.NORTH);
        sidebar.add(centerStack, BorderLayout.CENTER);
        return sidebar;
    }

    private JTabbedPane buildDetailTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setPreferredSize(new Dimension(214, 230));
        tabs.addTab("Cuộc họp", meetingPanel());
        tabs.addTab("Cuộc hẹn", listPanel(appointmentList, "Mở", this::openSelectedAppointment));
        tabs.addTab("Remind", listPanel(reminderList, "Xóa", this::deleteSelectedReminder));
        return tabs;
    }

    private JPanel meetingPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(Color.WHITE);
        meetingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        meetingList.setFixedCellHeight(30);
        meetingList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showSelectedMeetingParticipants();
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(meetingList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(232, 234, 237)));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        JButton participants = flatButton("Người tham gia");
        JButton leave = flatButton("Rời");
        participants.addActionListener(e -> showSelectedMeetingParticipants());
        leave.addActionListener(e -> leaveSelectedMeeting());
        actions.add(participants);
        actions.add(leave);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel listPanel(JList<?> list, String actionText, Runnable action) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(Color.WHITE);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(30);
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(232, 234, 237)));
        JButton actionButton = flatButton(actionText);
        actionButton.addActionListener(e -> action.run());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.add(actionButton);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshSidebarDetails() {
        meetingListModel.clear();
        for (GroupMeeting meeting : groupMeetingModel.findByUser(currentUserId)) {
            meetingListModel.addElement(meeting);
        }
        appointmentListModel.clear();
        for (Appointment appointment : appointmentModel.findAllByUser(currentUserId)) {
            appointmentListModel.addElement(appointment);
        }
        reminderListModel.clear();
        for (ReminderInfo reminder : appointmentModel.findReminderInfos(currentUserId)) {
            reminderListModel.addElement(reminder);
        }
    }

    private void leaveSelectedMeeting() {
        GroupMeeting selected = meetingList.getSelectedValue();
        if (selected == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Rời cuộc họp \"" + selected.title() + "\"?",
                "Rời cuộc họp",
                JOptionPane.YES_NO_OPTION
        );
        if (choice == JOptionPane.YES_OPTION) {
            groupMeetingModel.leaveGroup(selected.id(), currentUserId);
            refreshCalendar();
        }
    }

    private void showSelectedMeetingParticipants() {
        GroupMeeting selected = meetingList.getSelectedValue();
        if (selected == null) {
            return;
        }
        List<User> participants = groupMeetingModel.findParticipants(selected.id());
        StringBuilder message = new StringBuilder();
        if (participants.isEmpty()) {
            message.append("Cuộc họp này chưa có người tham gia.");
        } else {
            for (User user : participants) {
                message.append("- ")
                        .append(user.getFullName())
                        .append(" (")
                        .append(user.getUsername())
                        .append(")")
                        .append(System.lineSeparator());
            }
        }
        JOptionPane.showMessageDialog(
                this,
                message.toString(),
                "Người tham gia: " + selected.title(),
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void openSelectedAppointment() {
        Appointment selected = appointmentList.getSelectedValue();
        if (selected != null) {
            openEditDialog(selected);
        }
    }

    private void deleteSelectedReminder() {
        ReminderInfo selected = reminderList.getSelectedValue();
        if (selected == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Xóa reminder \"" + selected + "\"?",
                "Xóa remind",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            appointmentModel.deleteReminder(selected.getId());
            refreshCalendar();
        }
    }

    private void seedDemoData() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Xóa toàn bộ dữ liệu và tạo dữ liệu demo cho ngày " + selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "?",
                "Tạo dữ liệu demo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        demoDataModel.seedDemoData(selectedDate);
        currentUserId = 1;
        refreshUsers();
        refreshCalendar();
        JOptionPane.showMessageDialog(
                this,
                """
                        Đã tạo dữ liệu demo sau 07:30.

                        Trùng lịch: tạo lịch đè lên 08:30-09:30.
                        Tham gia nhóm: tạo lịch "Demo tham gia nhóm" lúc 09:45-10:45.
                        Bình thường: dùng các lịch không trùng đang hiển thị.
                        """,
                "Dữ liệu demo đã sẵn sàng",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void clearData() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Xóa toàn bộ người dùng, lịch hẹn, nhắc hẹn và cuộc họp nhóm?",
                "Xóa dữ liệu",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        demoDataModel.clearData();
        refreshUsers();
        refreshCalendar();
    }

    private void refreshUsers() {
        userListModel.clear();
        List<User> users = userModel.findAll();
        for (User user : users) {
            userListModel.addElement(user);
        }
        if (users.isEmpty()) {
            User created = userModel.create("Người dùng mới");
            userListModel.addElement(created);
            currentUserId = created.getId();
        }
        selectCurrentUser();
    }

    private void selectCurrentUser() {
        for (int i = 0; i < userListModel.size(); i++) {
            if (userListModel.get(i).getId() == currentUserId) {
                userList.setSelectedIndex(i);
                return;
            }
        }
        if (!userListModel.isEmpty()) {
            userList.setSelectedIndex(0);
            currentUserId = userListModel.get(0).getId();
        }
    }

    private void addUser() {
        String fullName = JOptionPane.showInputDialog(this, "Nhập tên user mới:", "Thêm user", JOptionPane.PLAIN_MESSAGE);
        if (fullName == null || fullName.trim().isEmpty()) {
            return;
        }
        try {
            User created = userModel.create(fullName);
            currentUserId = created.getId();
            refreshUsers();
            refreshCalendar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể thêm user", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedUser() {
        User selected = userList.getSelectedValue();
        if (selected == null) {
            return;
        }
        if (userListModel.size() <= 1) {
            JOptionPane.showMessageDialog(this, "Cần giữ lại ít nhất một user.", "Không thể xóa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Xóa user \"" + selected.getFullName() + "\" và toàn bộ lịch hẹn của user này?",
                "Xóa user",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        userModel.delete(selected.getId());
        currentUserId = userListModel.get(0).getId() == selected.getId()
                ? userListModel.get(1).getId()
                : userListModel.get(0).getId();
        refreshUsers();
        refreshCalendar();
    }

    private void rebuildMiniMonth() {
        miniMonthPanel.removeAll();
        miniMonthPanel.setLayout(new BorderLayout(0, 8));
        JLabel monthTitle = new JLabel(selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        monthTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        miniMonthPanel.add(monthTitle, BorderLayout.NORTH);

        JPanel grid = new JPanel(new java.awt.GridLayout(0, 7, 2, 6));
        grid.setOpaque(false);
        String[] days = {"S", "M", "T", "W", "T", "F", "S"};
        for (String day : days) {
            JLabel label = new JLabel(day, SwingConstants.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 11));
            grid.add(label);
        }
        YearMonth ym = YearMonth.from(selectedDate);
        LocalDate first = ym.atDay(1);
        int blanks = first.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < blanks; i++) {
            grid.add(new JLabel(""));
        }
        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            LocalDate date = ym.atDay(day);
            JButton button = miniMonthButton(String.valueOf(day));
            button.setFont(new Font("SansSerif", Font.PLAIN, 12));
            boolean isToday = date.equals(today);
            boolean isSelected = date.equals(selectedDate);
            if (isToday) {
                button.setForeground(Color.WHITE);
                button.setBackground(GOOGLE_BLUE);
                button.setOpaque(true);
                button.setContentAreaFilled(true);
            } else if (isSelected) {
                button.setForeground(GOOGLE_BLUE);
                button.setBackground(new Color(232, 240, 254));
                button.setOpaque(true);
                button.setContentAreaFilled(true);
            }
            button.addActionListener(e -> {
                selectedDate = date;
                refreshCalendar();
            });
            grid.add(button);
        }
        miniMonthPanel.add(grid, BorderLayout.CENTER);
        miniMonthPanel.revalidate();
        miniMonthPanel.repaint();
    }

    private void goToToday() {
        today = systemToday();
        selectedDate = today;
        refreshCalendar(true);
    }

    private void scrollToCurrentTime() {
        if (calendarScrollPane == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            LocalTime now = LocalTime.now(ZoneId.systemDefault());
            int contextMinutes = 60;
            int targetMinute = Math.max(0, now.getHour() * 60 + now.getMinute() - contextMinutes);
            int targetY = weekPanel.scrollYForMinute(targetMinute);
            JScrollBar verticalBar = calendarScrollPane.getVerticalScrollBar();
            int maxValue = Math.max(0, verticalBar.getMaximum() - verticalBar.getVisibleAmount());
            verticalBar.setValue(Math.min(targetY, maxValue));
        });
    }

    private void openAddDialog(LocalDateTime startsAt, LocalDateTime endsAt) {
        if (presenter == null) {
            return;
        }
        AddAppointmentDialog dialog = new AddAppointmentDialog(this, currentUserId, startsAt, endsAt, presenter);
        dialog.setVisible(true);
    }

    private void openEditDialog(Appointment appointment) {
        if (presenter == null) {
            return;
        }
        AddAppointmentDialog dialog = new AddAppointmentDialog(this, appointment, presenter);
        dialog.setVisible(true);
    }

    private LocalDate startOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    }

    private static LocalDate systemToday() {
        return LocalDate.now(ZoneId.systemDefault());
    }

    private JButton roundedButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 220, 224), 1, true),
                new EmptyBorder(9, 18, 9, 18)
        ));
        return button;
    }

    private JButton flatButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(4, 9, 4, 9));
        return button;
    }

    private JButton miniMonthButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(2, 2, 2, 2));
        button.setMargin(new java.awt.Insets(0, 0, 0, 0));
        button.setPreferredSize(new Dimension(26, 24));
        button.setMinimumSize(new Dimension(22, 22));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        return button;
    }
}
