package com.ooad.calendar.view;

import com.ooad.calendar.entity.Appointment;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
    private static final Color GOOGLE_BLUE = new Color(26, 115, 232);
    private static final Color TODAY_COLUMN = new Color(232, 240, 254);

    private final BiConsumer<LocalDateTime, LocalDateTime> timeRangeHandler;
    private final Consumer<Appointment> appointmentClickHandler;
    private final CalendarWeekHeaderPanel headerPanel = new CalendarWeekHeaderPanel();
    private LocalDate weekStart;
    private LocalDate today;
    private LocalDate selectedDate;
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
        setPreferredSize(new Dimension(1000, (LAST_HOUR - FIRST_HOUR) * HOUR_HEIGHT));
        DragSelectionHandler handler = new DragSelectionHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                rebuild();
            }
        });
    }

    public JComponent getHeaderComponent() {
        return headerPanel;
    }

    public int scrollYForMinute(int minuteOfDay) {
        int boundedMinute = Math.max(FIRST_HOUR * 60, Math.min(LAST_HOUR * 60, minuteOfDay));
        return (boundedMinute - FIRST_HOUR * 60) * HOUR_HEIGHT / 60;
    }

    public void setWeek(LocalDate weekStart, LocalDate today, LocalDate selectedDate, List<Appointment> appointments) {
        this.weekStart = weekStart;
        this.today = today;
        this.selectedDate = selectedDate;
        this.appointments = appointments;
        headerPanel.setDates(weekStart, today, selectedDate);
        rebuild();
    }

    private void rebuild() {
        removeAll();
        if (weekStart == null) {
            return;
        }
        int width = contentWidth();
        int dayWidth = (width - TIME_WIDTH) / 7;
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
        int y = (visibleStart - FIRST_HOUR * 60) * HOUR_HEIGHT / 60 + 4;
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

        int width = contentWidth();
        int dayWidth = (width - TIME_WIDTH) / 7;
        paintTodayColumn(g2, dayWidth);
        for (int day = 0; day <= 7; day++) {
            int x = TIME_WIDTH + day * dayWidth;
            g2.drawLine(x, 0, x, getHeight());
        }
        for (int hour = FIRST_HOUR; hour <= LAST_HOUR; hour++) {
            int y = (hour - FIRST_HOUR) * HOUR_HEIGHT;
            g2.drawLine(TIME_WIDTH, y, width, y);
            if (hour < LAST_HOUR) {
                String label = hour == 0 ? "12 AM" : hour == 12 ? "12 PM" : hour < 12 ? hour + " AM" : (hour - 12) + " PM";
                g2.setColor(new Color(95, 99, 104));
                g2.drawString(label, 22, y + 18);
                g2.setColor(GRID);
            }
        }
        paintDragSelection(g2, dayWidth);
        g2.dispose();
    }

    private void paintTodayColumn(Graphics2D g2, int dayWidth) {
        int todayOffset = todayOffset();
        if (todayOffset < 0) {
            return;
        }
        int x = TIME_WIDTH + todayOffset * dayWidth;
        g2.setColor(new Color(TODAY_COLUMN.getRed(), TODAY_COLUMN.getGreen(), TODAY_COLUMN.getBlue(), 72));
        g2.fillRect(x, 0, dayWidth, getHeight());
        g2.setColor(GRID);
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
        int y = (start - FIRST_HOUR * 60) * HOUR_HEIGHT / 60;
        int height = Math.max(18, (end - start) * HOUR_HEIGHT / 60);
        int x = TIME_WIDTH + dragDay * dayWidth + 6;
        return new Rectangle(x, y + 2, dayWidth - 12, height - 4);
    }

    private int dayAt(int x) {
        int width = contentWidth();
        int dayWidth = (width - TIME_WIDTH) / 7;
        int day = (x - TIME_WIDTH) / dayWidth;
        return day < 0 || day > 6 ? -1 : day;
    }

    private int minuteAt(int y) {
        int boundedY = Math.max(0, Math.min((LAST_HOUR - FIRST_HOUR) * HOUR_HEIGHT, y));
        int minutes = FIRST_HOUR * 60 + boundedY * 60 / HOUR_HEIGHT;
        return roundToQuarterHour(minutes);
    }

    private int roundToQuarterHour(int minutes) {
        int rounded = Math.round(minutes / 15.0f) * 15;
        return Math.max(FIRST_HOUR * 60, Math.min(LAST_HOUR * 60, rounded));
    }

    private LocalDateTime dateTimeFor(int day, int minuteOfDay) {
        return weekStart.plusDays(day).atTime(minuteOfDay / 60, minuteOfDay % 60);
    }

    private int contentWidth() {
        int width = getWidth() == 0 ? getPreferredSize().width : getWidth();
        return Math.max(920, width);
    }

    private class DragSelectionHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (weekStart == null || e.getX() < TIME_WIDTH) {
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

    private int todayOffset() {
        if (weekStart == null || today == null) {
            return -1;
        }
        int day = (int) Duration.between(weekStart.atStartOfDay(), today.atStartOfDay()).toDays();
        return day < 0 || day > 6 ? -1 : day;
    }

    private static class CalendarWeekHeaderPanel extends JPanel {
        private LocalDate weekStart;
        private LocalDate today;
        private LocalDate selectedDate;

        CalendarWeekHeaderPanel() {
            setOpaque(true);
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(1000, HEADER_HEIGHT));
        }

        void setDates(LocalDate weekStart, LocalDate today, LocalDate selectedDate) {
            this.weekStart = weekStart;
            this.today = today;
            this.selectedDate = selectedDate;
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int width = getWidth() == 0 ? getPreferredSize().width : getWidth();
            int dayWidth = (width - TIME_WIDTH) / 7;
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, width, getHeight());
            g2.setColor(new Color(95, 99, 104));
            g2.drawString("GMT+07", 18, HEADER_HEIGHT - 12);

            if (weekStart != null) {
                String[] dayNames = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
                for (int day = 0; day < 7; day++) {
                    LocalDate date = weekStart.plusDays(day);
                    int x = TIME_WIDTH + day * dayWidth;
                    paintDayHeader(g2, date, dayNames[day], x, dayWidth);
                }
            }

            g2.setColor(GRID);
            for (int day = 0; day <= 7; day++) {
                int x = TIME_WIDTH + day * dayWidth;
                g2.drawLine(x, 0, x, getHeight());
            }
            g2.drawLine(0, HEADER_HEIGHT - 1, width, HEADER_HEIGHT - 1);
            g2.dispose();
        }

        private void paintDayHeader(Graphics2D g2, LocalDate date, String dayName, int x, int dayWidth) {
            boolean isToday = date.equals(today);
            boolean isSelected = date.equals(selectedDate);
            if (isToday) {
                g2.setColor(new Color(TODAY_COLUMN.getRed(), TODAY_COLUMN.getGreen(), TODAY_COLUMN.getBlue(), 130));
                g2.fillRect(x, 0, dayWidth, getHeight());
            } else if (isSelected) {
                g2.setColor(new Color(241, 243, 244));
                g2.fillRect(x, 0, dayWidth, getHeight());
            }

            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(isToday ? GOOGLE_BLUE : new Color(95, 99, 104));
            drawCentered(g2, dayName, x, 24, dayWidth);

            String number = String.valueOf(date.getDayOfMonth());
            g2.setFont(new Font("SansSerif", Font.PLAIN, 28));
            Rectangle numberBounds = centeredTextBounds(g2, number, x, 62, dayWidth);
            if (isToday) {
                int circleSize = 40;
                int circleX = x + (dayWidth - circleSize) / 2;
                int circleY = 31;
                g2.setColor(GOOGLE_BLUE);
                g2.fillOval(circleX, circleY, circleSize, circleSize);
                g2.setColor(Color.WHITE);
            } else {
                g2.setColor(new Color(60, 64, 67));
            }
            g2.drawString(number, numberBounds.x, numberBounds.y);
        }

        private void drawCentered(Graphics2D g2, String text, int x, int baseline, int width) {
            Rectangle bounds = centeredTextBounds(g2, text, x, baseline, width);
            g2.drawString(text, bounds.x, bounds.y);
        }

        private Rectangle centeredTextBounds(Graphics2D g2, String text, int x, int baseline, int width) {
            int textWidth = g2.getFontMetrics().stringWidth(text);
            return new Rectangle(x + (width - textWidth) / 2, baseline, textWidth, g2.getFontMetrics().getHeight());
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
