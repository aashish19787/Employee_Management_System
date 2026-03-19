package model;

import javafx.beans.property.*;

/**
 * ENCAPSULATION: Task model with private fields and public getters/setters.
 */
public class Task {

    // ENCAPSULATION: private fields
    private final IntegerProperty taskId        = new SimpleIntegerProperty();
    private final StringProperty  taskName      = new SimpleStringProperty();
    private final StringProperty  description   = new SimpleStringProperty();
    private final StringProperty  status        = new SimpleStringProperty();
    private final IntegerProperty employeeId    = new SimpleIntegerProperty();
    private final StringProperty  employeeName  = new SimpleStringProperty(); // joined from employees table

    public Task() {}

    public Task(int taskId, String taskName, String description, String status,
                int employeeId, String employeeName) {
        setTaskId(taskId);
        setTaskName(taskName);
        setDescription(description);
        setStatus(status);
        setEmployeeId(employeeId);
        setEmployeeName(employeeName);
    }

    // Property accessors
    public IntegerProperty taskIdProperty()       { return taskId; }
    public StringProperty  taskNameProperty()     { return taskName; }
    public StringProperty  descriptionProperty()  { return description; }
    public StringProperty  statusProperty()       { return status; }
    public IntegerProperty employeeIdProperty()   { return employeeId; }
    public StringProperty  employeeNameProperty() { return employeeName; }

    // ENCAPSULATION: Getters
    public int    getTaskId()       { return taskId.get(); }
    public String getTaskName()     { return taskName.get(); }
    public String getDescription()  { return description.get(); }
    public String getStatus()       { return status.get(); }
    public int    getEmployeeId()   { return employeeId.get(); }
    public String getEmployeeName() { return employeeName.get(); }

    // ENCAPSULATION: Setters
    public void setTaskId(int v)           { taskId.set(v); }
    public void setTaskName(String v)      { taskName.set(v); }
    public void setDescription(String v)   { description.set(v); }
    public void setStatus(String v)        { status.set(v); }
    public void setEmployeeId(int v)       { employeeId.set(v); }
    public void setEmployeeName(String v)  { employeeName.set(v); }
}