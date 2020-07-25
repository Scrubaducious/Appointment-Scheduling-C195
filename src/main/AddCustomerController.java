package main;

import Model.Customer;
import Utils.DBController;
import Utils.DBQuery;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

public class AddCustomerController implements Initializable
{
    @FXML public Button cancelBtn;
    @FXML public Button saveBtn;
    @FXML public TextField customerNameTxt;
    @FXML public TextField customerAddressTxt;
    @FXML public TextField customerAddress2Txt;
    @FXML public TextField customerCountryTxt;
    @FXML public TextField customerCityTxt;
    @FXML public TextField customerZipTxt;
    @FXML public TextField customerPhoneTxt;

    Statement statement = DBQuery.getStatement();

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {

    }

    public void saveBtnClick() throws SQLException
    {
        Boolean allFields = true;
        String customerName = customerNameTxt.getText();
        String customerAddress = customerAddressTxt.getText();
        String customerAddress2 = customerAddress2Txt.getText();
        String customerCountry = customerCountryTxt.getText();
        String customerCity = customerCityTxt.getText();
        String customerZip = customerZipTxt.getText();
        String customerPhone = customerPhoneTxt.getText();
        String[] allCustomerData = { customerName, customerAddress, customerCountry, customerCity, customerZip, customerPhone };
        for (int i = 0; i < allCustomerData.length; i++)
        {
            if (allCustomerData[i].equals(""))
            {
                allFields = false;
                break;
            }
            else
            {
                allFields = true;
            }
        }
        if (!allFields)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Customer Error");
            alert.setHeaderText("Fields Incomplete");
            alert.setContentText("One or more Required Fields has been left Blank.");
            alert.showAndWait();
        }
        else if (allFields)
        {
            Customer newCustomer = new Customer(customerName, customerAddress, customerAddress2, customerCountry, customerCity, customerZip, customerPhone);
            DBController.addCustomer(newCustomer);
            Stage currentStage = (Stage) saveBtn.getScene().getWindow();
            currentStage.close();
        }
    }

    public void cancelBtnClick(ActionEvent actionEvent)
    {
        Stage currentStage = (Stage) cancelBtn.getScene().getWindow();
        currentStage.close();
    }
}
