package main;

import Model.Appointment;
import Model.Customer;
import Utils.DBController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static Utils.DBController.zoneUTC;

public class MainScreenController implements Initializable
{
    @FXML public Button addCustomerBtn;
    @FXML public Button editCustomerBtn;
    @FXML public Button logoutBtn;
    @FXML public Button searchCustomerBtn;
    @FXML public Button addApptBtn;
    @FXML public Button editApptBtn;
    @FXML public Button delCustomerBtn;
    @FXML public Button typeReportBtn;
    @FXML public Button viewCustomerListBtn;
    @FXML public Button allUsersBtn;
    @FXML public RadioButton pastApptRadioBtn;
    @FXML public RadioButton allApptRadioBtn;
    @FXML public RadioButton upcomingApptRadioBtn;
    @FXML public RadioButton monthlyApptRadioBtn;
    @FXML public RadioButton weeklyApptRadioBtn;
    @FXML public Label appointmentsLabel;
    @FXML public TextField searchCustomerTxt;
    @FXML public DatePicker mainDatePicker;
    @FXML public TableView<Customer> mainCustomerTable;
    @FXML public TableColumn<Customer, Integer> mainCustomerIdCol;
    @FXML public TableColumn<Customer, String> mainCustomerNameCol;
    @FXML public TableView<Appointment> mainApptTable;
    @FXML public TableColumn<Appointment, Integer> mainApptIdCol;
    @FXML public TableColumn<Appointment, Integer> mainApptCustIdCol;
    @FXML public TableColumn<Appointment, String> mainApptTitleCol;
    @FXML public TableColumn<Appointment, String> mainApptLocationCol;
    @FXML public TableColumn<Appointment, String> mainApptTypeCol;
    @FXML public TableColumn<Appointment, LocalDate> mainApptDateCol;
    @FXML public TableColumn<Appointment, LocalTime> mainApptStartCol;
    @FXML public TableColumn<Appointment, LocalTime> mainApptEndCol;

    public static Customer addApptCustomer = null;
    public static Customer customerToEdit = null;
    public static Appointment apptToEdit = null;
    public ObservableList<Appointment> appointments = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        try {
            checkUpcomingAppts();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        //Listener with lambda, automatically updates Appointment Table when a different Date is selected
        mainDatePicker.valueProperty().addListener((v, oldValue, newValue) ->
        {
            if (allApptRadioBtn.isSelected()) {
                try {
                    setTableAll();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            else if (monthlyApptRadioBtn.isSelected()) {
                try {
                    setTableMonthly();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            else if (weeklyApptRadioBtn.isSelected()) {
                try {
                    setTableWeekly();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            else if (upcomingApptRadioBtn.isSelected()) {
                try {
                    setTableUpcoming();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            else if (pastApptRadioBtn.isSelected()) {
                try {
                    setTablePast();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            try {
                updateApptTable();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });

        try
        {
            appointments = DBController.getAllAppointments();
            mainCustomerTable.setItems(DBController.getAllCustomers());
            mainApptTable.setItems(appointments);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        //Initialize with all Upcoming Appts selected
        upcomingApptRadioBtn.setSelected(true);
        try {
            setTableUpcoming();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        mainCustomerIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        mainCustomerNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        mainApptIdCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        mainApptCustIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        mainApptTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        mainApptLocationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        mainApptTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        mainApptDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        mainApptStartCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        mainApptEndCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
    }

    public void generateTypeReport() throws SQLException
    {
        //ObservableList<String> uniqueApptTypes = FXCollections.observableArrayList();
        //ListIterator<String> uniqueApptTypesList = uniqueApptTypes.listIterator();
        Set<String> uniqueApptTypes = new HashSet<>();
        ObservableList<Appointment> appts = DBController.getAppointmentsMonthly(LocalDate.now(), LocalDate.now().plusMonths(1));
        ListIterator<Appointment> apptsList = appts.listIterator();
        while (apptsList.hasNext())
        {
            Appointment appt = apptsList.next();
            String apptType = appt.getType();
            uniqueApptTypes.add(apptType);
        }

        Integer apptTypes = uniqueApptTypes.size();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Appointment Types");
        alert.setHeaderText("Appointment Types");
        if (apptTypes == 1)
        {
            alert.setContentText("There is " + apptTypes + " Appointment Type this month.");
        }
        else
        {
            alert.setContentText("There are " + apptTypes + " different types of Appointments this Month.");
        }
        alert.setHeight(200);
        alert.setWidth(500);
        alert.show();
    }

    public void checkUpcomingAppts() throws SQLException
    {
        ObservableList<Appointment> upcomingAppts = FXCollections.observableArrayList();
        ObservableList<Appointment> apptsByUser = DBController.getAppointmentsByUser();
        ListIterator<Appointment> apptsList = apptsByUser.listIterator();
        LocalDateTime now = LocalDateTime.now(zoneUTC);

        while (apptsList.hasNext())
        {
            Appointment appt = apptsList.next();
            LocalDateTime startCheck = appt.getStart().minusMinutes(15);
            if ((now.isAfter(startCheck)) && (now.isBefore(appt.getStart())))
            {
                upcomingAppts.add(appt);
            }
        }

        if (!upcomingAppts.isEmpty())
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Upcoming Appointment");
            alert.setHeaderText("Upcoming Appointment");
            alert.setContentText("You have 1 or more Appointments Scheduled within the next 15 Minutes.");
            alert.setHeight(200);
            alert.setWidth(500);
            alert.show();
        }
    }

    public void updateCustomerTable()
    {
        try
        {
            mainCustomerTable.setItems(DBController.getAllCustomers());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        mainCustomerIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        mainCustomerNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
    }

    public void updateApptTable() throws SQLException
    {
        mainApptTable.setItems(appointments);

        mainApptIdCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        mainApptCustIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        mainApptTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        mainApptLocationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        mainApptTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        mainApptDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        mainApptStartCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        mainApptEndCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
    }

    public void searchCustomerBtnClick() throws SQLException
    {
        ObservableList<Customer> searchResults;
        String search = searchCustomerTxt.getText();
        searchResults = Customer.customerSearch(search);
        mainCustomerTable.setItems(searchResults);
    }

    public void addCustomerBtnClick() throws IOException
    {
        Stage addCustomerStage = new Stage();
        addCustomerStage.initModality(Modality.APPLICATION_MODAL);
        addCustomerStage.setTitle("Add/Modify Customer");
        Parent addCustomerRoot = FXMLLoader.load(getClass().getResource("AddCustomer.fxml"));
        Scene addCustomerScene = new Scene(addCustomerRoot, 450,600);
        addCustomerStage.setScene(addCustomerScene);
        addCustomerStage.setResizable(false);
        addCustomerStage.showAndWait();
        updateCustomerTable();
    }

    public void editCustomerBtnClick() throws IOException, SQLException
    {
        customerToEdit = mainCustomerTable.getSelectionModel().getSelectedItem();
        if (customerToEdit == null)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Edit Error");
            alert.setHeaderText("No Customer Selected");
            alert.setContentText("Please select a Customer from the Customer List to Edit.");
            alert.showAndWait();
        }
        else if (customerToEdit != null)
        {
            Integer customerToEditId = customerToEdit.getCustomerId();
            customerToEdit = DBController.getCustomerDetails(customerToEditId);
            Stage editCustomerStage = new Stage();
            editCustomerStage.initModality(Modality.APPLICATION_MODAL);
            editCustomerStage.setTitle("Modify Customer");
            Parent editCustomerRoot = FXMLLoader.load(getClass().getResource("EditCustomer.fxml"));
            Scene editCustomerScene = new Scene(editCustomerRoot, 450,600);
            editCustomerStage.setScene(editCustomerScene);
            editCustomerStage.setResizable(false);
            editCustomerStage.showAndWait();
            updateCustomerTable();
            customerToEdit = null;
        }
    }

    public void toCustomerList() throws IOException
    {
        Stage customerListStage = new Stage();
        customerListStage.initModality(Modality.APPLICATION_MODAL);
        customerListStage.setTitle("Detailed Customer List");
        Parent customerListRoot = FXMLLoader.load(getClass().getResource("CustomerList.fxml"));
        Scene customerListScene = new Scene(customerListRoot);
        customerListStage.setScene(customerListScene);
        customerListStage.setResizable(false);
        customerListStage.showAndWait();
        updateCustomerTable();
    }

    public void toAllUsers() throws IOException
    {
        Stage userApptsStage = new Stage();
        userApptsStage.initModality(Modality.APPLICATION_MODAL);
        userApptsStage.setTitle("All Users");
        Parent userApptsRoot = FXMLLoader.load(getClass().getResource("AllUsers.fxml"));
        Scene userApptsScene = new Scene(userApptsRoot);
        userApptsStage.setScene(userApptsScene);
        userApptsStage.setResizable(false);
        userApptsStage.showAndWait();
    }

    public void addAppt() throws IOException, SQLException
    {
        addApptCustomer = mainCustomerTable.getSelectionModel().getSelectedItem();
        if (addApptCustomer == null)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Add Error");
            alert.setHeaderText("No Customer Selected");
            alert.setContentText("Please select a Customer from the Customer List.");
            alert.showAndWait();
        }
        else if (addApptCustomer != null)
        {
            Stage addApptStage = new Stage();
            addApptStage.initModality(Modality.APPLICATION_MODAL);
            addApptStage.setTitle("Add Appointment");
            Parent addApptRoot = FXMLLoader.load(getClass().getResource("AddAppointment.fxml"));
            Scene addApptScene = new Scene(addApptRoot, 450, 600);
            addApptStage.setScene(addApptScene);
            addApptStage.setResizable(false);
            addApptStage.showAndWait();
            if (allApptRadioBtn.isSelected()) setTableAll();
            else if (monthlyApptRadioBtn.isSelected()) setTableMonthly();
            else if (weeklyApptRadioBtn.isSelected()) setTableWeekly();
            else if (pastApptRadioBtn.isSelected()) setTablePast();
            else setTableUpcoming();
            addApptCustomer = null;
        }
    }

    public void setTablePast() throws SQLException
    {
        appointmentsLabel.setText("Past Appointments");
        appointments = DBController.getPastAppointments();

        mainApptIdCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        mainApptCustIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        mainApptTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        mainApptLocationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        mainApptTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        mainApptDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        mainApptStartCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        mainApptEndCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        updateApptTable();
    }

    public void setTableAll() throws SQLException
    {
        appointmentsLabel.setText("All Appointments");
        appointments = DBController.getAllAppointments();

        mainApptIdCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        mainApptCustIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        mainApptTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        mainApptLocationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        mainApptTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        mainApptDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        mainApptStartCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        mainApptEndCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        updateApptTable();
    }

    public void setTableUpcoming() throws SQLException
    {
        appointmentsLabel.setText("All Upcoming Appointments");
        appointments = DBController.getUpcomingAppointments();

        mainApptIdCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        mainApptCustIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        mainApptTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        mainApptLocationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        mainApptTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        mainApptDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        mainApptStartCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        mainApptEndCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        updateApptTable();
    }

    public void setTableMonthly() throws SQLException
    {
        LocalDate startDate;
        if (mainDatePicker.getValue() == null)
        {
            startDate = LocalDate.now();
        }
        else
        {
            startDate = mainDatePicker.getValue();
        }

        LocalDate endDate = startDate.plusMonths(1);
        appointmentsLabel.setText("Appointments by Month: " + startDate + " - " + endDate);

        appointments = DBController.getAppointmentsMonthly(startDate, endDate);
        updateApptTable();
    }

    public void setTableWeekly() throws SQLException
    {
        LocalDate startDate;
        if (mainDatePicker.getValue() == null)
        {
            startDate = LocalDate.now();
        }
        else
        {
            startDate = mainDatePicker.getValue();
        }

        LocalDate endDate = startDate.plusWeeks(1);
        appointmentsLabel.setText("Appointments by Week: " + startDate + " - " + endDate);

        appointments = DBController.getAppointmentsWeekly(startDate, endDate);
        updateApptTable();
    }

    public void deleteCustomer() throws SQLException
    {
        Customer custToDel = mainCustomerTable.getSelectionModel().getSelectedItem();
        if (custToDel == null)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Delete Error");
            alert.setHeaderText("No Customer Selected");
            alert.setContentText("Please select a Customer from the Customer List to Delete.");
            alert.showAndWait();
        }
        else if (custToDel != null)
        {
            Integer custToDelId = custToDel.getCustomerId();
            DBController.deleteCustomer(custToDelId);
            updateCustomerTable();
            custToDel = null;
        }
        updateCustomerTable();
        setTableUpcoming();
        updateApptTable();
    }

    public void editAppt() throws IOException, SQLException
    {
        apptToEdit = mainApptTable.getSelectionModel().getSelectedItem();

        if (apptToEdit == null)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Edit Error");
            alert.setHeaderText("No Appointment Selected");
            alert.setContentText("Please select an Appointment from the Appointment List to Edit.");
            alert.showAndWait();
        }
        else if (apptToEdit != null)
        {
            Stage editApptStage = new Stage();
            editApptStage.initModality(Modality.APPLICATION_MODAL);
            editApptStage.setTitle("Edit Appointment");
            Parent editApptRoot = FXMLLoader.load(getClass().getResource("EditAppointment.fxml"));
            Scene editApptScene = new Scene(editApptRoot);
            editApptStage.setScene(editApptScene);
            editApptStage.setResizable(false);
            editApptStage.showAndWait();
            if (allApptRadioBtn.isSelected()) setTableAll();
            else if (monthlyApptRadioBtn.isSelected()) setTableMonthly();
            else if (weeklyApptRadioBtn.isSelected()) setTableWeekly();
            else if (pastApptRadioBtn.isSelected()) setTablePast();
            else setTableUpcoming();
            apptToEdit = null;
        }
    }

    public void deleteAppt() throws SQLException
    {
        Appointment apptToDel = mainApptTable.getSelectionModel().getSelectedItem();
        if (apptToDel == null)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Delete Error");
            alert.setHeaderText("No Appointment Selected");
            alert.setContentText("Please select an Appointment from the Appointment List to Delete.");
            alert.showAndWait();
        }
        else if (apptToDel != null)
        {
            Integer apptToDelId = apptToDel.getAppointmentId();
            DBController.deleteAppointment(apptToDelId);
            if (allApptRadioBtn.isSelected()) setTableAll();
            else if (monthlyApptRadioBtn.isSelected()) setTableMonthly();
            else if (weeklyApptRadioBtn.isSelected()) setTableWeekly();
            else if (pastApptRadioBtn.isSelected()) setTablePast();
            else setTableUpcoming();
            apptToDel = null;
        }
    }

    public void logoutBtnClick(ActionEvent actionEvent) throws IOException
    {
        Stage loginStage = new Stage();
        Stage currentStage = (Stage) logoutBtn.getScene().getWindow();
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.setTitle("Login");
        Parent loginRoot = FXMLLoader.load(getClass().getResource("LoginScreen.fxml"));
        Scene loginScene = new Scene(loginRoot, 450, 600);
        loginStage.setScene(loginScene);
        loginStage.setResizable(false);
        currentStage.close();
        loginStage.show();
    }
}
