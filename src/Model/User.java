package Model;

import Utils.DBController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.sql.SQLException;
import java.util.ListIterator;

public class User
{
    private Integer userId;
    private String userName;
    private String password;

    public User()
    {

    }

    public User(Integer userId, String userName, String password)
    {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
    }

    public static ObservableList<User> userSearch(String search) throws SQLException
    {
        ObservableList<User> allUsers = DBController.getAllUsers();
        ListIterator<User> userIterator = allUsers.listIterator();
        ObservableList<User> searchResult = FXCollections.observableArrayList();
        while(userIterator.hasNext())
        {
            User currUser = userIterator.next();
            if(currUser.getUserName().contains(search))
            {
                searchResult.add(currUser);
            }
        }

        if (searchResult.size() == 0)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Search Result");
            alert.setHeaderText("No Matches Found");
            alert.setContentText("Your search did not have any matches.");
            alert.showAndWait();
        }
        return searchResult;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
