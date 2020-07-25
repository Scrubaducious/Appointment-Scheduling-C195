package main;

import Model.Appointment;
import Model.Customer;
import Utils.DBController;
import javafx.collections.ObservableList;
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
import java.util.ResourceBundle;

public class CustomerListController implements Initializable
{
    @FXML public Button customerSearchBtn;
    @FXML public Button customerAddBtn;
    @FXML public Button customerEditBtn;
    @FXML public Button customerDeleteBtn;
    @FXML public Button reportBtn;
    @FXML public Button customerCancelBtn;
    @FXML public TextField customerSearchTxt;
    @FXML public TableView<Customer> customerTable;
    @FXML public TableColumn<Customer, Integer> customerTableID;
    @FXML public TableColumn<Customer, String> customerTableName;
    @FXML public TableColumn<Customer, String> customerTableAddress;
    @FXML public TableColumn<Customer, String> customerTableAddress2;
    @FXML public TableColumn<Customer, String> customerTableCity;
    @FXML public TableColumn<Customer, String> customerTablePhone;
    @FXML public TableColumn<Customer, String> customerTableDate;
    @FXML public TableColumn<Customer, String> customerTableActive;
    @FXML public TableColumn<Customer, String> customerTableCreated;

    public static Customer customerToEdit = null;
    public static Customer customerToDelete = null;

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        try
        {
            customerTable.setItems(DBController.getAllCustomers());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        customerTableID.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        customerTableName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerTableAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        customerTableAddress2.setCellValueFactory(new PropertyValueFactory<>("address2"));
        customerTableCity.setCellValueFactory(new PropertyValueFactory<>("city"));
        customerTablePhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        //customerTableCreated.setCellValueFactory(new PropertyValueFactory<>("createdBy"));
        //customerTableDate.setCellValueFactory(new PropertyValueFactory<>("createDate"));
        //customerTableActive.setCellValueFactory(new PropertyValueFactory<>("active"));
    }

    public void updateTable()
    {
        try
        {
            customerTable.setItems(DBController.getAllCustomers());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        customerTableID.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        customerTableName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerTableAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        customerTableAddress2.setCellValueFactory(new PropertyValueFactory<>("address2"));
        customerTableCity.setCellValueFactory(new PropertyValueFactory<>("city"));
        customerTablePhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
    }

    public void customerSearchBtnClick() throws SQLException
    {
        ObservableList<Customer> searchResults;
        String search = customerSearchTxt.getText();
        searchResults = Customer.customerSearch(search);
        customerTable.setItems(searchResults);
    }

    public void customerCancelBtnClick()
    {
        Stage currentStage = (Stage) customerCancelBtn.getScene().getWindow();
        currentStage.close();
    }

    public void customerAddBtnClick() throws IOException
    {
        Stage currentStage = (Stage) customerCancelBtn.getScene().getWindow();
        currentStage.close();

        Stage addCustomerStage = new Stage();
        addCustomerStage.initModality(Modality.APPLICATION_MODAL);
        addCustomerStage.setTitle("Add/Modify Customer");
        Parent addCustomerRoot = FXMLLoader.load(getClass().getResource("AddCustomer.fxml"));
        Scene addCustomerScene = new Scene(addCustomerRoot, 450,600);
        addCustomerStage.setScene(addCustomerScene);
        addCustomerStage.setResizable(false);
        addCustomerStage.showAndWait();
        updateTable();
    }

    public void customerEditBtnClick() throws IOException, SQLException
    {
        customerToEdit = customerTable.getSelectionModel().getSelectedItem();
        if (customerToEdit == null)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Edit Error");
            alert.setHeaderText("No Customer Selected");
            alert.setContentText("Please select a Customer from the Customer List to Edit.");
            alert.showAndWait();
        }
        else
        {
            Integer customerToEditId = customerToEdit.getCustomerId();
            customerToEdit = DBController.getCustomerDetails(customerToEditId);
            Stage editCustomerStage = new Stage();
            editCustomerStage.initModality(Modality.APPLICATION_MODAL);
            editCustomerStage.setTitle("Add/Modify Customer");
            Parent editCustomerRoot = FXMLLoader.load(getClass().getResource("EditCustomer.fxml"));
            Scene editCustomerScene = new Scene(editCustomerRoot, 450,600);
            editCustomerStage.setScene(editCustomerScene);
            editCustomerStage.setResizable(false);
            editCustomerStage.showAndWait();
            updateTable();
            customerToEdit = null;
        }
    }

    public void generateCustomerReport() throws SQLException
    {
        Customer customer = customerTable.getSelectionModel().getSelectedItem();
        if (customer == null)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No Customer Selected");
            alert.setContentText("Please select a Customer from the Customer List Generate a Report for.");
            alert.showAndWait();
        }
        else
        {
            ObservableList<Appointment> customerAppts = DBController.getCustomerAppts(customer);
            Integer numOfAppts = customerAppts.size();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Customer Appointments");
            alert.setHeaderText("Customer Appointments");
            alert.setContentText("Customer " + customer.getCustomerName() + " has " + numOfAppts + " upcoming Appointment(s).");
            alert.showAndWait();
        }
    }

    public void customerDeleteBtnClick() throws SQLException
    {
        customerToDelete = customerTable.getSelectionModel().getSelectedItem();
        Integer customerToDeleteId = customerToDelete.getCustomerId();
        DBController.deleteCustomer(customerToDeleteId);
        updateTable();
    }
}
