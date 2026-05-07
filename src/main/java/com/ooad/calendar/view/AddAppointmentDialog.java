package com.ooad.calendar.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.ooad.calendar.dto.AppointmentDTO;
import com.ooad.calendar.dto.ReminderDTO;
import com.ooad.calendar.entity.Appointment;
import com.ooad.calendar.entity.Reminder;
import com.ooad.calendar.presenter.AppointmentPresenter;

public class AddAppointmentDialog extends JDialog implements IAppointmentView {
    private static final Color GOOGLE_BLUE = new Color(26, 115, 232);
    private final int ownerId;
    private final AppointmentPresenter presenter;
    private final Integer appointmentId;
    private final Integer groupMeetingId;
    private final JTextField titleField = new JTextField();
    private final JTextField locationField = new JTextField();
    private final JSpinner startSpinner;
    private final JSpinner endSpinner;
    private final JSpinner reminderMinutesSpinner = new JSpinner(new SpinnerNumberModel(30, 0, Integer.MAX_VALUE, 5));
    private final JSpinner reminderDateTimeSpinner;
    private final JTextField reminderMessageField = new JTextField("Nhắc lịch hẹn");
    private final DefaultListModel<ReminderDTO> reminderListModel = new DefaultListModel<>();
    private final JList<ReminderDTO> reminderList = new JList<>(reminderListModel);
    private final JCheckBox groupMeeting = new JCheckBox("Tạo cuộc họp nhóm");
    private final JButton saveButton = new JButton("Save");
    private final JLabel validationLabel = new JLabel(" ");

    public AddAppointmentDialog(CalendarFrame owner, int ownerId, LocalDateTime startsAt, LocalDateTime endsAt,
                                AppointmentPresenter presenter) {
        super(owner, "Add appointment", true);
        this.ownerId = ownerId;
        this.presenter = presenter;
        this.appointmentId = null;
        this.groupMeetingId = null;
        this.startSpinner = dateTimeSpinner(startsAt);
        this.endSpinner = dateTimeSpinner(endsAt);
        this.reminderDateTimeSpinner = dateTimeSpinner(startsAt.minusMinutes(30));
        reminderListModel.addElement(new ReminderDTO(30, "Nhắc lịch hẹn"));
        buildUi();
        bindValidation();
        UpdateValidationState(isInputValid());
        setSize(680, 570);
        setLocationRelativeTo(owner);
    }

    public AddAppointmentDialog(CalendarFrame owner, Appointment appointment, AppointmentPresenter presenter) {
        super(owner, "Edit appointment", true);
        this.ownerId = appointment.getOwnerId();
        this.presenter = presenter;
        this.appointmentId = appointment.getId();
        this.groupMeetingId = appointment.getGroupMeetingId();
        this.startSpinner = dateTimeSpinner(appointment.getStartsAt());
        this.endSpinner = dateTimeSpinner(appointment.getEndsAt());
        this.reminderDateTimeSpinner = dateTimeSpinner(appointment.getStartsAt().minusMinutes(30));
        buildUi();
        titleField.setText(appointment.getTitle());
        locationField.setText(appointment.getLocation() == null ? "" : appointment.getLocation());
        groupMeeting.setSelected(appointment.getGroupMeetingId() != null);
        applyReminderSelection(appointment);
        bindValidation();
        UpdateValidationState(isInputValid());
        setSize(680, 570);
        setLocationRelativeTo(owner);
    }

    @Override
    public void ShowAddWindow(LocalDateTime defaultDate) {
        startSpinner.setValue(Date.from(defaultDate.atZone(ZoneId.systemDefault()).toInstant()));
        endSpinner.setValue(Date.from(defaultDate.plusHours(1).atZone(ZoneId.systemDefault()).toInstant()));
        setVisible(true);
    }

    @Override
    public AppointmentDTO GetInputData() {
        return new AppointmentDTO(
                appointmentId,
                groupMeetingId,
                ownerId,
                titleField.getText(),
                locationField.getText(),
                toLocalDateTime((Date) startSpinner.getValue()),
                toLocalDateTime((Date) endSpinner.getValue()),
                selectedReminders(),
                groupMeeting.isSelected()
        );
    }

    @Override
    public void ShowErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Dữ liệu chưa hợp lệ", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void ShowConflictWarning(Appointment conflict) {
        JOptionPane.showMessageDialog(
                this,
                "Khoảng thời gian này đã có lịch: " + conflict.getTitle(),
                "Trùng lịch",
                JOptionPane.WARNING_MESSAGE
        );
    }

    @Override
    public void ShowMultipleConflictsWarning(List<Appointment> conflicts) {
        StringBuilder message = new StringBuilder("Khoảng thời gian này trùng với nhiều lịch hẹn:\n");
        int limit = Math.min(conflicts.size(), 5);
        for (int i = 0; i < limit; i++) {
            Appointment conflict = conflicts.get(i);
            message.append("- ")
                    .append(conflict.getTitle())
                    .append(" lúc ")
                    .append(conflict.getStartsAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .append('\n');
        }
        if (conflicts.size() > limit) {
            message.append("... và ").append(conflicts.size() - limit).append(" lịch khác.\n");
        }
        message.append("Vui lòng chọn khoảng thời gian khác.");
        JOptionPane.showMessageDialog(
                this,
                message.toString(),
                "Trùng nhiều lịch",
                JOptionPane.WARNING_MESSAGE
        );
    }

    @Override
    public boolean RequestAppointmentReplacement(Appointment conflict) {
        String time = conflict.getStartsAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Bạn muốn thay thế lịch \"" + conflict.getTitle() + "\" lúc " + time + " không?",
                "Thay thế lịch hẹn",
                JOptionPane.YES_NO_OPTION
        );
        return choice == JOptionPane.YES_OPTION;
    }

    @Override
    public boolean RequestJoinGroupMeeting(String groupName) {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Có cuộc họp nhóm \"" + groupName + "\" cùng tên và thời lượng. Bạn muốn tham gia không?",
                "Tham gia nhóm",
                JOptionPane.YES_NO_OPTION
        );
        return choice == JOptionPane.YES_OPTION;
    }

    @Override
    public void UpdateValidationState(boolean valid) {
        if (valid) {
            EnableSaveButton();
        } else {
            DisableSaveButton();
        }
        validationLabel.setText(valid ? " " : "Tên không được trống và thời gian kết thúc phải sau bắt đầu.");
    }

    public void DisableSaveButton() {
        saveButton.setEnabled(false);
    }

    public void EnableSaveButton() {
        saveButton.setEnabled(true);
    }

    @Override
    public void RefreshCalendarView() {
        repaint();
    }

    @Override
    public void CloseWindow() {
        dispose();
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(248, 250, 253));
        root.setBorder(new EmptyBorder(16, 22, 16, 22));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        root.add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(12, 50, 8, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;

        titleField.setFont(new Font("SansSerif", Font.PLAIN, 26));
        titleField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, GOOGLE_BLUE));
        titleField.setText("");
        titleField.putClientProperty("JTextField.placeholderText", "Add title");
        form.add(titleField, gbc);

        gbc.gridy = 1;
        form.add(demoPresetPanel(), gbc);

        gbc.gridy = 2;
        form.add(row("◷", "Thời gian", spinnerPanel()), gbc);
        gbc.gridy = 3;
        form.add(row("◉", "Địa điểm", locationField), gbc);
        gbc.gridy = 4;
        form.add(row("≡", "Lời nhắc", reminderPanel()), gbc);
        gbc.gridy = 5;
        form.add(row("●", "Nhóm", groupMeeting), gbc);
        gbc.gridy = 6;
        validationLabel.setForeground(new Color(188, 32, 27));
        form.add(validationLabel, gbc);

        root.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        if (appointmentId != null) {
            JButton delete = new JButton("Delete");
            delete.setForeground(new Color(188, 32, 27));
            delete.addActionListener(e -> deleteAppointment());
            actions.add(delete);
        }
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        saveButton.setBackground(GOOGLE_BLUE);
        saveButton.setForeground(Color.WHITE);
        saveButton.setText("Save");
        saveButton.setOpaque(true);
        saveButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(24, 90, 188)),
                new EmptyBorder(5, 18, 5, 18)
        ));
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> {
            if (!isInputValid()) {
                ShowErrorMessage("Vui lòng nhập tên lịch hẹn và chọn thời gian hợp lệ.");
                UpdateValidationState(false);
                return;
            }
            presenter.SaveAppointment(this);
        });
        actions.add(cancel);
        actions.add(saveButton);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel demoPresetPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);

        JButton normal = new JButton("Demo thường");
        JButton conflict = new JButton("Demo trùng lịch");
        JButton joinGroup = new JButton("Demo tham gia nhóm");
        JButton reminder = new JButton("Demo nhắc hẹn");

        normal.addActionListener(e -> applyDemoPreset(
                "Bình thường: đọc yêu cầu", "Thư viện",
                7, 45, 8, 15, 30, "Nhắc lịch bình thường"
        ));
        conflict.addActionListener(e -> applyDemoPreset(
                "Demo trùng lịch", "Phòng B2",
                9, 0, 10, 0, 30, "Nhắc lịch trùng"
        ));
        joinGroup.addActionListener(e -> applyDemoPreset(
                "Demo tham gia nhóm", "Google Meet",
                9, 45, 10, 45, 10, "Nhắc tham gia nhóm"
        ));
        reminder.addActionListener(e -> applyReminderDemoPreset());

        panel.add(normal);
        panel.add(conflict);
        panel.add(joinGroup);
        panel.add(reminder);
        return panel;
    }

    private void applyDemoPreset(String title, String location, int startHour, int startMinute,
                                 int endHour, int endMinute, int reminderMinutes, String reminderMessage) {
        LocalDate date = toLocalDateTime((Date) startSpinner.getValue()).toLocalDate();
        LocalDateTime startsAt = date.atTime(startHour, startMinute);
        LocalDateTime endsAt = date.atTime(endHour, endMinute);

        titleField.setText(title);
        locationField.setText(location);
        setSpinnerDateTime(startSpinner, startsAt);
        setSpinnerDateTime(endSpinner, endsAt);
        setSpinnerDateTime(reminderDateTimeSpinner, startsAt.minusMinutes(reminderMinutes));
        groupMeeting.setSelected(false);

        reminderMinutesSpinner.setValue(reminderMinutes);
        reminderMessageField.setText(reminderMessage);
        reminderListModel.clear();
        reminderListModel.addElement(new ReminderDTO(reminderMinutes, reminderMessage));
        UpdateValidationState(isInputValid());
    }

    private void applyReminderDemoPreset() {
        int reminderMinutes = 5;
        LocalDateTime reminderAt = LocalDateTime.now().plusMinutes(5).withSecond(0).withNano(0);
        LocalDateTime startsAt = reminderAt.plusMinutes(reminderMinutes);
        LocalDateTime endsAt = startsAt.plusMinutes(45);
        String reminderMessage = "Nhắc hẹn demo sau 5 phút";

        titleField.setText("Demo có nhắc hẹn");
        locationField.setText("Lab 3");
        setSpinnerDateTime(startSpinner, startsAt);
        setSpinnerDateTime(endSpinner, endsAt);
        setSpinnerDateTime(reminderDateTimeSpinner, reminderAt);
        groupMeeting.setSelected(false);

        reminderMinutesSpinner.setValue(reminderMinutes);
        reminderMessageField.setText(reminderMessage);
        reminderListModel.clear();
        reminderListModel.addElement(new ReminderDTO(reminderMinutes, reminderMessage));
        UpdateValidationState(isInputValid());
    }

    private void deleteAppointment() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Bạn chắc chắn muốn xóa cuộc hẹn này?",
                "Xóa cuộc hẹn",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            presenter.DeleteAppointment(this);
        }
    }

    private JPanel spinnerPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        panel.add(startSpinner);
        panel.add(new JLabel("–"));
        panel.add(endSpinner);
        return panel;
    }

    private JPanel reminderPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JPanel input = new JPanel(new BorderLayout(8, 0));
        input.setOpaque(false);
        JPanel minutes = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        minutes.setOpaque(false);
        minutes.add(new JLabel("Trước:"));
        minutes.add(reminderMinutesSpinner);
        minutes.add(new JLabel("phút"));
        minutes.add(quickButton("5p", 5));
        minutes.add(quickButton("10p", 10));
        minutes.add(quickButton("30p", 30));
        minutes.add(quickButton("1h", 60));
        minutes.add(quickButton("1 ngày", 1440));

        JPanel exactDate = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        exactDate.setOpaque(false);
        exactDate.add(new JLabel("Ngày giờ nhắc:"));
        exactDate.add(reminderDateTimeSpinner);
        JButton addByDate = new JButton("Add date");
        addByDate.addActionListener(e -> addReminderByDateTime());
        exactDate.add(addByDate);

        JPanel message = new JPanel(new BorderLayout(8, 0));
        message.setOpaque(false);
        reminderMessageField.setColumns(16);
        message.add(new JLabel("Message:"), BorderLayout.WEST);
        message.add(reminderMessageField, BorderLayout.CENTER);

        JPanel stackedInput = new JPanel(new GridLayout(0, 1, 0, 4));
        stackedInput.setOpaque(false);
        stackedInput.add(minutes);
        stackedInput.add(exactDate);
        input.add(stackedInput, BorderLayout.NORTH);
        input.add(message, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actions.setOpaque(false);
        JButton add = new JButton("Add");
        JButton remove = new JButton("Remove");
        add.addActionListener(e -> addReminder());
        remove.addActionListener(e -> removeSelectedReminder());
        actions.add(add);
        actions.add(remove);

        reminderList.setVisibleRowCount(3);
        reminderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reminderList.setFixedCellHeight(24);

        panel.add(input, BorderLayout.NORTH);
        panel.add(actions, BorderLayout.CENTER);
        panel.add(reminderList, BorderLayout.SOUTH);
        return panel;
    }

    private JButton quickButton(String text, int minutes) {
        JButton button = new JButton(text);
        button.addActionListener(e -> reminderMinutesSpinner.setValue(minutes));
        return button;
    }

    private JPanel row(String icon, String label, java.awt.Component component) {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setOpaque(false);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setForeground(new Color(95, 99, 104));
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));
        JLabel textLabel = new JLabel(label);
        textLabel.setForeground(new Color(60, 64, 67));
        JPanel left = new JPanel(new BorderLayout(12, 0));
        left.setOpaque(false);
        left.add(iconLabel, BorderLayout.WEST);
        left.add(textLabel, BorderLayout.CENTER);
        panel.add(left, BorderLayout.WEST);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private JButton chip(String text, boolean selected) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 12, 8, 12));
        button.setBackground(selected ? new Color(210, 227, 252) : new Color(248, 250, 253));
        return button;
    }

    private JSpinner dateTimeSpinner(LocalDateTime value) {
        Date date = Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
        JSpinner spinner = new JSpinner(new SpinnerDateModel(date, null, null, java.util.Calendar.MINUTE));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy HH:mm"));
        return spinner;
    }

    private void setSpinnerDateTime(JSpinner spinner, LocalDateTime value) {
        spinner.setValue(Date.from(value.atZone(ZoneId.systemDefault()).toInstant()));
    }

    private void bindValidation() {
        titleField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                UpdateValidationState(isInputValid());
            }

            public void removeUpdate(DocumentEvent e) {
                UpdateValidationState(isInputValid());
            }

            public void changedUpdate(DocumentEvent e) {
                UpdateValidationState(isInputValid());
            }
        });
        startSpinner.addChangeListener(e -> UpdateValidationState(isInputValid()));
        endSpinner.addChangeListener(e -> UpdateValidationState(isInputValid()));
    }

    private boolean isInputValid() {
        return !titleField.getText().trim().isEmpty()
                && toLocalDateTime((Date) endSpinner.getValue()).isAfter(toLocalDateTime((Date) startSpinner.getValue()));
    }

    private void addReminder() {
        int minutes = (Integer) reminderMinutesSpinner.getValue();
        String message = reminderMessageField.getText().trim();
        if (message.isEmpty()) {
            message = "Nhắc lịch hẹn";
        }
        reminderListModel.addElement(new ReminderDTO(minutes, message));
    }

    private void addReminderByDateTime() {
        LocalDateTime startsAt = toLocalDateTime((Date) startSpinner.getValue());
        LocalDateTime reminderAt = toLocalDateTime((Date) reminderDateTimeSpinner.getValue());
        long minutes = Duration.between(reminderAt, startsAt).toMinutes();
        if (minutes < 0) {
            ShowErrorMessage("Thời điểm nhắc phải trước thời gian bắt đầu cuộc hẹn.");
            return;
        }
        if (minutes > Integer.MAX_VALUE) {
            ShowErrorMessage("Khoảng thời gian nhắc quá lớn.");
            return;
        }
        reminderMinutesSpinner.setValue((int) minutes);
        addReminder();
    }

    private void removeSelectedReminder() {
        int index = reminderList.getSelectedIndex();
        if (index >= 0) {
            reminderListModel.remove(index);
        }
    }

    private List<ReminderDTO> selectedReminders() {
        List<ReminderDTO> reminders = new ArrayList<>();
        for (int i = 0; i < reminderListModel.size(); i++) {
            reminders.add(reminderListModel.get(i));
        }
        return reminders;
    }

    private void applyReminderSelection(Appointment appointment) {
        reminderListModel.clear();
        boolean firstReminder = true;
        for (Reminder reminder : appointment.getReminders()) {
            if (reminder.message() != null && !reminder.message().isBlank()) {
                reminderMessageField.setText(reminder.message());
            }
            if (firstReminder) {
                reminderMinutesSpinner.setValue(reminder.minutesBefore());
                setSpinnerDateTime(reminderDateTimeSpinner, appointment.getStartsAt().minusMinutes(reminder.minutesBefore()));
                firstReminder = false;
            }
            reminderListModel.addElement(new ReminderDTO(reminder.minutesBefore(), reminder.message()));
        }
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}
