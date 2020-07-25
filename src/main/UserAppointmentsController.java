package main;

import Model.Appointment;
import Model.User;
import Utils.DBController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class UserAppointmentsController implements Initializable
{
    @FXML public Button closeBtn;
    @FXML public Label titleLabel;
    @FXML public TableView<Appointment> apptsTable;
    @FXML public TableColumn<Appointment, Integer> apptIdColumn;
    @FXML public TableColumn<Appointment, Integer> customerIdColumn;
    @FXML public TableColumn<Appointment, String> titleColumn;
    @FXML public TableColumn<Appointment, String> locationColumn;
    @FXML public TableColumn<Appointment, String> typeColumn;
    @FXML public TableColumn<Appointment, LocalDate> dateColumn;
    @FXML public TableColumn<Appointment, LocalTime> startColumn;
    @FXML public TableColumn<Appointment, LocalTime> endColumn;

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        titleLabel.setText("Appointments For: " + AllUsersController.user.getUserName());
        ObservableList<Appointment> userAppts = AllUsersController.userAppts;
        apptsTable.setItems(userAppts);

        apptIdColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
    }

    public void closeWindow()
    {
        Stage currentStage = (Stage) closeBtn.getScene().getWindow();
        currentStage.close();
    }
}
