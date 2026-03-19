
package model;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * ENCAPSULATION: Employee model class with private fields and public getters/setters.
 * Uses JavaFX Properties for TableView binding.
 */
public class Employee {

    // ENCAPSULATION: private fields
    private final IntegerProperty employeeId   = new SimpleIntegerProperty();
    private final StringProperty  firstName    = new SimpleStringProperty();
    private final StringProperty  middleName   = new SimpleStringProperty();
    private final StringProperty  lastName     = new SimpleStringProperty();
    private ObjectProperty<LocalDate> dateOfBirth = new SimpleObjectProperty<>();
    private final StringProperty  contactNo    = new SimpleStringProperty();
    private final StringProperty  email        = new SimpleStringProperty();
    private final StringProperty  address      = new SimpleStringProperty();
    private final StringProperty  position     = new SimpleStringProperty();
    private final StringProperty  department   = new SimpleStringProperty();

    public Employee() {}

    public Employee(int employeeId, String firstName, String middleName, String lastName,
                    LocalDate dateOfBirth, String contactNo, String email,
                    String address, String position, String department) {
        setEmployeeId(employeeId);
        setFirstName(firstName);
        setMiddleName(middleName);
        setLastName(lastName);
        setDateOfBirth(dateOfBirth);
        setContactNo(contactNo);
        setEmail(email);
        setAddress(address);
        setPosition(position);
        setDepartment(department);
    }

    // Property accessors (for TableView binding)
    public IntegerProperty employeeIdProperty()        { return employeeId; }
    public StringProperty  firstNameProperty()         { return firstName; }
    public StringProperty  middleNameProperty()        { return middleName; }
    public StringProperty  lastNameProperty()          { return lastName; }
    public ObjectProperty<LocalDate> dateOfBirthProperty() { return dateOfBirth; }
    public StringProperty  contactNoProperty()         { return contactNo; }
    public StringProperty  emailProperty()             { return email; }
    public StringProperty  addressProperty()           { return address; }
    public StringProperty  positionProperty()          { return position; }
    public StringProperty  departmentProperty()        { return department; }

    // ENCAPSULATION: Getters
    public int       getEmployeeId()  { return employeeId.get(); }
    public String    getFirstName()   { return firstName.get(); }
    public String    getMiddleName()  { return middleName.get(); }
    public String    getLastName()    { return lastName.get(); }
    public LocalDate getDateOfBirth() { return dateOfBirth.get(); }
    public String    getContactNo()   { return contactNo.get(); }
    public String    getEmail()       { return email.get(); }
    public String    getAddress()     { return address.get(); }
    public String    getPosition()    { return position.get(); }
    public String    getDepartment()  { return department.get(); }

    /** Convenience: full name */
    public String getFullName() {
        String mid = (getMiddleName() != null && !getMiddleName().isBlank()) ? " " + getMiddleName() : "";
        return getFirstName() + mid + " " + getLastName();
    }

    // ENCAPSULATION: Setters
    public void setEmployeeId(int v)        { employeeId.set(v); }
    public void setFirstName(String v)      { firstName.set(v); }
    public void setMiddleName(String v)     { middleName.set(v); }
    public void setLastName(String v)       { lastName.set(v); }
    public void setDateOfBirth(LocalDate v) { dateOfBirth.set(v); }
    public void setContactNo(String v)      { contactNo.set(v); }
    public void setEmail(String v)          { email.set(v); }
    public void setAddress(String v)        { address.set(v); }
    public void setPosition(String v)       { position.set(v); }
    public void setDepartment(String v)     { department.set(v); }

    @Override
    public String toString() {
        return getFullName();
    }
}