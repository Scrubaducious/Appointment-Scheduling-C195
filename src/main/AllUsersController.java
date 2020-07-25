package main;

import Model.Appointment;
import Model.Customer;
import Model.User;
import Utils.DBController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AllUsersController implements Initializable
{
    @FXML public Button userSearchBtn;
    @FXML public Button reportBtn;
    @FXML public Button cancelBtn;
    @FXML public TextField userSearchTxt;
    @FXML public TableView<User> userTable;
    @FXML public TableColumn<User, Integer> userIdColumn;
    @FXML public TableColumn<User, String> userNameColumn;

    public static User user = null;
    public static ObservableList<Appointment> userAppts = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        try
        {
            userTable.setItems(DBController.getAllUsers());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
    }

    public void userSearch() throws SQLException
    {
        ObservableList<User> searchResults;
        String search = userSearchTxt.getText();
        searchResults = User.userSearch(search);
        userTable.setItems(searchResults);
    }

    public void generateReport() throws SQLException, IOException
    {
        user = userTable.getSelectionModel().getSelectedItem();
        userAppts = DBController.getAppointmentsByUser(user);
        Stage userApptsStage = new Stage();
        userApptsStage.initModality(Modality.APPLICATION_MODAL);
        userApptsStage.setTitle("Appointments by User");
        Parent userApptsRoot = FXMLLoader.load(getClass().getResource("UserAppointments.fxml"));
        Scene userApptsScene = new Scene(userApptsRoot, 900, 600);
        userApptsStage.setScene(userApptsScene);
        userApptsStage.setResizable(false);
        userApptsStage.show();
    }

    public void cancelBtnClick()
    {
        Stage currentStage = (Stage) cancelBtn.getScene().getWindow();
        currentStage.close();
    }
}
