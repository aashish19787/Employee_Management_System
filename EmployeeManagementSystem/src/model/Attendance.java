package model;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * ENCAPSULATION: Attendance model with private fields and public getters/setters.
 */
public class Attendance {

    // ENCAPSULATION: private fields
    private final IntegerProperty attendanceId   = new SimpleIntegerProperty();
    private final IntegerProperty employeeId     = new SimpleIntegerProperty();
    private final StringProperty  employeeName   = new SimpleStringProperty();
    private ObjectProperty<LocalDate> attendanceDate = new SimpleObjectProperty<>();
    private final StringProperty  status         = new SimpleStringProperty();
    private final StringProperty  remarks        = new SimpleStringProperty();

    public Attendance() {}

    public Attendance(int attendanceId, int employeeId, String employeeName,
                      LocalDate attendanceDate, String status, String remarks) {
        setAttendanceId(attendanceId);
        setEmployeeId(employeeId);
        setEmployeeName(employeeName);
        setAttendanceDate(attendanceDate);
        setStatus(status);
        setRemarks(remarks);
    }

    // Property accessors
    public IntegerProperty attendanceIdProperty()           { return attendanceId; }
    public IntegerProperty employeeIdProperty()             { return employeeId; }
    public StringProperty  employeeNameProperty()           { return employeeName; }
    public ObjectProperty<LocalDate> attendanceDateProperty(){ return attendanceDate; }
    public StringProperty  statusProperty()                 { return status; }
    public StringProperty  remarksProperty()                { return remarks; }

    // ENCAPSULATION: Getters
    public int       getAttendanceId()   { return attendanceId.get(); }
    public int       getEmployeeId()     { return employeeId.get(); }
    public String    getEmployeeName()   { return employeeName.get(); }
    public LocalDate getAttendanceDate() { return attendanceDate.get(); }
    public String    getStatus()         { return status.get(); }
    public String    getRemarks()        { return remarks.get(); }

    // ENCAPSULATION: Setters
    public void setAttendanceId(int v)          { attendanceId.set(v); }
    public void setEmployeeId(int v)            { employeeId.set(v); }
    public void setEmployeeName(String v)       { employeeName.set(v); }
    public void setAttendanceDate(LocalDate v)  { attendanceDate.set(v); }
    public void setStatus(String v)             { status.set(v); }
    public void setRemarks(String v)            { remarks.set(v); }
}