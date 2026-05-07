# Google Calendar Swing MVP

Ứng dụng Java Swing mô phỏng giao diện Google Calendar cho bài toán thêm lịch hẹn theo mô hình MVP, lưu dữ liệu bằng H2.

## Chức năng chính

- Hiển thị lịch tuần kiểu Google Calendar, có sidebar tháng nhỏ và nút `Create`.
- Kéo chọn một vùng trong cột ngày để mở form `Add appointment` với giờ bắt đầu/kết thúc tương ứng.
- Nếu chỉ bấm một vị trí trên lịch, ứng dụng tự chọn khoảng 30 phút từ vị trí đó.
- Bấm vào một cuộc hẹn đã tạo để chỉnh sửa thông tin hoặc xóa cuộc hẹn.
- Chọn user trong vùng `My calendars` để chuyển sang lịch của user đó.
- Có thể thêm user mới hoặc xóa user đang chọn; khi xóa user, các lịch hẹn của user đó cũng được xóa.
- Vùng tab giữa sidebar cho phép xem `Cuộc họp`, `Cuộc hẹn`, `Remind`.
- Trong tab `Cuộc họp` có thể xem người tham gia bằng double-click hoặc nút `Người tham gia`, và có thể rời cuộc họp nhóm; trong tab `Remind` có thể xóa từng reminder.
- Validate ở View: không cho lưu khi tên trống hoặc thời lượng âm/bằng 0.
- Presenter kiểm tra trùng lịch theo khoảng thời gian.
- Khi trùng lịch, hiển thị cảnh báo và hỏi có thay thế lịch cũ không.
- Lưu lịch hẹn và reminder vào H2.
- Reminder có cả số phút nhắc trước và message; có thể thêm nhiều reminder tùy ý bằng nút nhanh 5p/10p/30p/1h/1 ngày, nhập số phút trực tiếp, hoặc chọn ngày-giờ nhắc cụ thể trước cuộc hẹn.
- Nếu lịch mới trùng tên và thời lượng với cuộc họp nhóm có sẵn, hỏi người dùng có tham gia nhóm không.
- Có thể tạo cuộc họp nhóm mới từ checkbox trong form.

## Cách chạy nhanh

```powershell
.\run.ps1
```

Database H2 được tạo tại:

```text
data/calendar.mv.db
```

## Chạy bằng Maven nếu máy có Maven

```powershell
mvn clean compile exec:java
```

## Cấu trúc theo class diagram

- `view`: `CalendarFrame`, `CalendarWeekPanel`, `AddAppointmentDialog`, `IAppointmentView`
- `presenter`: `AppointmentPresenter`
- `model`: `AppointmentModel`, `GroupMeetingModel`
- `entity`: `Appointment`, `Reminder`, `GroupMeeting`
- `dto`: `AppointmentDTO`
- `config`: `Database`
