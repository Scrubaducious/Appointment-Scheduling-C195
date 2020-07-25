module Scott.Woods.Appointment.Manager
{
    requires javafx.fxml;
    requires javafx.controls;
    requires mysql.connector.java;
    requires java.sql;

    opens main;
    opens Model;
}
