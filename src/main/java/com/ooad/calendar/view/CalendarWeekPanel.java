package com.ooad.calendar.view;

import com.ooad.calendar.entity.Appointment;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CalendarWeekPanel extends JLayeredPane {
    private static final int HEADER_HEIGHT = 86;
    private static final int TIME_WIDTH = 86;
    private static final int HOUR_HEIGHT = 72;
    private static final int FIRST_HOUR = 0;
    private static final int LAST_HOUR = 24;
    private static final Color GRID = new Color(232, 234, 237);
    private static final Color EVENT_BLUE = new Color(3, 169, 244);

    private final BiConsumer<LocalDateTime, LocalDateTime> timeRangeHandler;
    private final Consumer<Appointment> appointmentClickHandler;
    private LocalDate weekStart;
    private List<Appointment> appointments = new ArrayList<>();
    private Integer dragDay;
    private Integer dragStartMinute;
    private Integer dragCurrentMinute;

    public CalendarWeekPanel(BiConsumer<LocalDateTime, LocalDateTime> timeRangeHandler,
                             Consumer<Appointment> appointmentClickHandler) {
        this.timeRangeHandler = timeRangeHandler;
        this.appointmentClickHandler = appointmentClickHandler;
        setLayout(null);
        setOpaque(true);
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(1000, HEADER_HEIGHT + (LAST_HOUR - FIRST_HOUR) * HOUR_HEIGHT));
        DragSelectionHandler handler = new DragSelectionHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);
    }

    public void setWeek(LocalDate weekStart, List<Appointment> appointments) {
        this.weekStart = weekStart;
        this.appointments = appointments;
        rebuild();
    }

    private void rebuild() {
        removeAll();
        if (weekStart == null) {
            return;
        }
        int width = Math.max(920, getPreferredSize().width);
        int dayWidth = (width - TIME_WIDTH) / 7;
        String[] dayNames = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        for (int day = 0; day < 7; day++) {
            LocalDate date = weekStart.plusDays(day);
            JPanel header = new JPanel(new java.awt.BorderLayout());
            header.setOpaque(false);
            header.setBounds(TIME_WIDTH + day * dayWidth, 8, dayWidth, HEADER_HEIGHT - 16);
            JLabel name = new JLabel(dayNames[day], SwingConstants.CENTER);
            name.setFont(new Font("SansSerif", Font.BOLD, 12));
            JLabel number = new JLabel(String.valueOf(date.getDayOfMonth()), SwingConstants.CENTER);
            number.setFont(new Font("SansSerif", Font.PLAIN, 28));
            header.add(name, java.awt.BorderLayout.NORTH);
            header.add(number, java.awt.BorderLayout.CENTER);
            add(header, Integer.valueOf(2));

        }

        for (Appointment appointment : appointments) {
            addEventBlock(appointment, dayWidth);
        }
        revalidate();
        repaint();
    }

    private void addEventBlock(Appointment appointment, int dayWidth) {
        int day = (int) Duration.between(weekStart.atStartOfDay(), appointment.getStartsAt().toLocalDate().atStartOfDay()).toDays();
        if (day < 0 || day > 6) {
            return;
        }
        int startMinute = appointment.getStartsAt().getHour() * 60 + appointment.getStartsAt().getMinute();
        int endMinute = appointment.getEndsAt().getHour() * 60 + appointment.getEndsAt().getMinute();
        int visibleStart = Math.max(FIRST_HOUR * 60, startMinute);
        int visibleEnd = Math.min(LAST_HOUR * 60, endMinute);
        if (visibleEnd <= visibleStart) {
            return;
        }
        int y = HEADER_HEIGHT + (visibleStart - FIRST_HOUR * 60) * HOUR_HEIGHT / 60 + 4;
        int height = Math.max(34, (visibleEnd - visibleStart) * HOUR_HEIGHT / 60 - 8);
        int x = TIME_WIDTH + day * dayWidth + 8;
        EventBlock block = new EventBlock(appointment);
        block.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                appointmentClickHandler.accept(appointment);
                e.consume();
            }
        });
        block.setBounds(x, y, dayWidth - 16, height);
        add(block, Integer.valueOf(3));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(GRID);

        int width = getWidth() == 0 ? getPreferredSize().width : getWidth();
        int dayWidth = (width - TIME_WIDTH) / 7;
        g2.drawLine(0, HEADER_HEIGHT, width, HEADER_HEIGHT);
        for (int day = 0; day <= 7; day++) {
            int x = TIME_WIDTH + day * dayWidth;
            g2.drawLine(x, HEADER_HEIGHT, x, getHeight());
        }
        for (int hour = FIRST_HOUR; hour <= LAST_HOUR; hour++) {
            int y = HEADER_HEIGHT + (hour - FIRST_HOUR) * HOUR_HEIGHT;
            g2.drawLine(TIME_WIDTH, y, width, y);
            if (hour < LAST_HOUR) {
                String label = hour == 0 ? "12 AM" : hour == 12 ? "12 PM" : hour < 12 ? hour + " AM" : (hour - 12) + " PM";
                g2.setColor(new Color(95, 99, 104));
                g2.drawString(label, 22, y + 18);
                g2.setColor(GRID);
            }
        }
        g2.setColor(new Color(95, 99, 104));
        g2.drawString("GMT+07", 18, HEADER_HEIGHT - 12);
        paintDragSelection(g2, dayWidth);
        g2.dispose();
    }

    private void paintDragSelection(Graphics2D g2, int dayWidth) {
        if (dragDay == null || dragStartMinute == null || dragCurrentMinute == null) {
            return;
        }
        Rectangle bounds = selectionBounds(dayWidth);
        g2.setColor(new Color(26, 115, 232, 54));
        g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);
        g2.setColor(new Color(26, 115, 232, 170));
        g2.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);
    }

    private Rectangle selectionBounds(int dayWidth) {
        int start = Math.min(dragStartMinute, dragCurrentMinute);
        int end = Math.max(dragStartMinute, dragCurrentMinute);
        if (end == start) {
            end = Math.min(LAST_HOUR * 60, start + 30);
        }
        int y = HEADER_HEIGHT + (start - FIRST_HOUR * 60) * HOUR_HEIGHT / 60;
        int height = Math.max(18, (end - start) * HOUR_HEIGHT / 60);
        int x = TIME_WIDTH + dragDay * dayWidth + 6;
        return new Rectangle(x, y + 2, dayWidth - 12, height - 4);
    }

    private int dayAt(int x) {
        int width = getWidth() == 0 ? getPreferredSize().width : getWidth();
        int dayWidth = (width - TIME_WIDTH) / 7;
        int day = (x - TIME_WIDTH) / dayWidth;
        return day < 0 || day > 6 ? -1 : day;
    }

    private int minuteAt(int y) {
        int boundedY = Math.max(HEADER_HEIGHT, Math.min(HEADER_HEIGHT + (LAST_HOUR - FIRST_HOUR) * HOUR_HEIGHT, y));
        int minutes = FIRST_HOUR * 60 + (boundedY - HEADER_HEIGHT) * 60 / HOUR_HEIGHT;
        return roundToQuarterHour(minutes);
    }

    private int roundToQuarterHour(int minutes) {
        int rounded = Math.round(minutes / 15.0f) * 15;
        return Math.max(FIRST_HOUR * 60, Math.min(LAST_HOUR * 60, rounded));
    }

    private LocalDateTime dateTimeFor(int day, int minuteOfDay) {
        return weekStart.plusDays(day).atTime(minuteOfDay / 60, minuteOfDay % 60);
    }

    private class DragSelectionHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (weekStart == null || e.getY() < HEADER_HEIGHT || e.getX() < TIME_WIDTH) {
                return;
            }
            int day = dayAt(e.getX());
            if (day == -1) {
                return;
            }
            dragDay = day;
            dragStartMinute = minuteAt(e.getY());
            dragCurrentMinute = dragStartMinute;
            repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (dragDay == null) {
                return;
            }
            dragCurrentMinute = minuteAt(e.getY());
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (dragDay == null || dragStartMinute == null || dragCurrentMinute == null) {
                clearSelection();
                return;
            }
            int start = Math.min(dragStartMinute, dragCurrentMinute);
            int end = Math.max(dragStartMinute, dragCurrentMinute);
            if (end == start) {
                end = Math.min(LAST_HOUR * 60, start + 30);
            }
            int day = dragDay;
            clearSelection();
            timeRangeHandler.accept(dateTimeFor(day, start), dateTimeFor(day, end));
        }

        private void clearSelection() {
            dragDay = null;
            dragStartMinute = null;
            dragCurrentMinute = null;
            repaint();
        }
    }

    private static class EventBlock extends JPanel {
        private final Appointment appointment;

        EventBlock(Appointment appointment) {
            this.appointment = appointment;
            setOpaque(false);
            setLayout(new java.awt.BorderLayout());
            setBorder(new EmptyBorder(8, 10, 8, 10));
            JLabel label = new JLabel(eventText());
            label.setForeground(Color.WHITE);
            label.setFont(new Font("SansSerif", Font.BOLD, 13));
            add(label, java.awt.BorderLayout.NORTH);
            String location = appointment.getLocation() == null || appointment.getLocation().isBlank()
                    ? ""
                    : appointment.getLocation();
            JLabel sub = new JLabel(location);
            sub.setForeground(new Color(227, 242, 253));
            sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
            add(sub, java.awt.BorderLayout.CENTER);
            setToolTipText(appointment.getTitle());
        }

        private String eventText() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mma");
            return "<html>" + appointment.getTitle() + "<br>"
                    + appointment.getStartsAt().format(formatter).toLowerCase()
                    + " - " + appointment.getEndsAt().format(formatter).toLowerCase()
                    + "</html>";
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(EVENT_BLUE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(new Color(0, 137, 209));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
