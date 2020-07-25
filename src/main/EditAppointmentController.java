package main;

import Model.Appointment;
import Model.Customer;
import Utils.DBController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.time.*;
import java.util.ListIterator;
import java.util.ResourceBundle;
import java.util.TimeZone;

public class EditAppointmentController implements Initializable
{
    @FXML public Button apptSaveBtn;
    @FXML public Button apptCancelBtn;
    @FXML public TextField apptTitleTxt;
    @FXML public TextField apptLocationTxt;
    @FXML public TextField apptTypeTxt;
    @FXML public DatePicker apptStartDateBox;
    @FXML public ComboBox<LocalTime> apptStartTimeBox;
    @FXML public ComboBox<Integer> apptLengthBox;
    @FXML public Label apptCustomerLabel;
    @FXML public Label apptCustomerIdLabel;

    Customer customer = null;
    Appointment appt = null;
    ObservableList<LocalTime> allTimes = FXCollections.observableArrayList();
    ZoneId defaultZoneId = ZoneId.of(TimeZone.getDefault().getID());
    ZoneId zoneUTC = ZoneId.of("UTC");

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        //Lambda to disable certain Dates on the Datepicker
        apptStartDateBox.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();

                setDisable(empty || date.compareTo(today) < 0 );
                if(date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY)
                    setDisable(true);
            }
        });

        appt = MainScreenController.apptToEdit;
        Integer customerId = appt.getCustomerId();

        try {
            customer = DBController.getCustomerDetails(customerId);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        apptCustomerLabel.setText("Customer: " + customer.getCustomerName());
        apptCustomerIdLabel.setText("ID: " + customer.getCustomerId());

        for (int i = 10; i < 17; i++) //Populate Time combobox with times between 10 and 4 (local times)
        {
            LocalTime time = LocalTime.of(i, 0, 0);
            allTimes.add(time);
        }
        apptStartTimeBox.setItems(allTimes);

        apptLengthBox.getItems().addAll(15, 30, 45);

        Integer apptLength = (appt.getEndTime().getMinute()) - (appt.getStartTime().getMinute());

        apptTitleTxt.setText(appt.getTitle());
        apptLocationTxt.setText(appt.getLocation());
        apptStartDateBox.setValue(appt.getDate());
        apptStartTimeBox.setValue(appt.getStartTime());
        apptLengthBox.setValue(apptLength);
        apptTypeTxt.setText(appt.getType());
    }

    public void apptSaveBtnClick() throws SQLException
    {
        Integer apptId = MainScreenController.apptToEdit.getAppointmentId();
        String apptTitle = apptTitleTxt.getText();
        String apptLocation = apptLocationTxt.getText();
        String apptType = apptTypeTxt.getText();
        Boolean overlapAppts = false;

        LocalDate apptStartDate = apptStartDateBox.getValue();
        LocalTime apptStartTime = apptStartTimeBox.getValue();
        LocalDateTime apptStartDatetime = apptStartTime.atDate(apptStartDate);
        ZonedDateTime apptStartZoned = apptStartDatetime.atZone(defaultZoneId);
        ZonedDateTime apptStartUTC = apptStartZoned.withZoneSameInstant(zoneUTC);
        LocalDateTime apptStartUTCLocal = apptStartUTC.toLocalDateTime();
        int apptLength = apptLengthBox.getValue();
        LocalTime apptEndTime = apptStartTime.plusMinutes(apptLength);
        LocalDateTime apptEndDatetime = apptEndTime.atDate(apptStartDate);
        ZonedDateTime apptEndZoned = apptEndDatetime.atZone(defaultZoneId);
        ZonedDateTime apptEndUTC = apptEndZoned.withZoneSameInstant(zoneUTC);

        ObservableList<Appointment> apptsByUser = DBController.getAppointmentsByUser();
        ListIterator<Appointment> apptIterator = apptsByUser.listIterator();
        while (apptIterator.hasNext())
        {
            Appointment appt = apptIterator.next();
            if ((appt.getStart().equals(apptStartUTCLocal)) && (!appt.getAppointmentId().equals(apptId)))
            {
                overlapAppts = true;
            }
        }

        if (overlapAppts)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Appointment Error");
            alert.setHeaderText("Invalid Date/Time");
            alert.setContentText("This Time overlaps with another of this User's Appointments.");
            alert.showAndWait();
        }

        if (apptStartDatetime.isBefore(LocalDateTime.now()))
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Appointment Error");
            alert.setHeaderText("Invalid Date/Time");
            alert.setContentText("Appointment cannot be scheduled for a time in the past.");
            alert.showAndWait();
        }

        else if (!overlapAppts)
        {
            DBController.updateAppointment(appt.getAppointmentId(), apptTitle, apptLocation, apptType, apptStartZoned, apptEndZoned);
            apptCancelBtnClick();
        }
    }

    public void apptCancelBtnClick()
    {
        Stage currentStage = (Stage) apptCancelBtn.getScene().getWindow();
        currentStage.close();
    }
}

