package main;

import Model.User;
import Utils.DBQuery;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

public class LoginScreenController implements Initializable
{
    @FXML public TextField loginUsernameTxt;
    @FXML public TextField loginPasswordVisibleTxt;
    @FXML public Label loginUsernameLabel;
    @FXML public Label loginPasswordLabel;
    @FXML public Label loginTitle;
    @FXML public Label loginLocationLabel;
    @FXML public Label loginCurrLocation;
    @FXML public PasswordField loginPasswordHiddenTxt;
    @FXML public CheckBox loginPasswordToggle;
    @FXML public Button loginExitButton;
    @FXML public Button loginButton;

    //Supported Languages: English, French, German
    ResourceBundle defaultRB = ResourceBundle.getBundle("Bundles/Appointment", Locale.getDefault());

    Statement statement = DBQuery.getStatement();
    public static String currentUsername;
    public static User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        setAllText();

        //makes hidden and unhidden password fields share the same text properties
        loginPasswordVisibleTxt.textProperty().bindBidirectional(loginPasswordHiddenTxt.textProperty());
        loginPasswordToggle.setSelected(true);
        loginPasswordVisibleTxt.setVisible(false);
        loginPasswordHiddenTxt.setVisible(true);
    }

    public void setAllText()
    {
        loginTitle.setText(defaultRB.getString("loginTitle"));
        loginUsernameLabel.setText(defaultRB.getString("loginUsername"));
        loginPasswordLabel.setText(defaultRB.getString("loginPassword"));
        loginPasswordToggle.setText(defaultRB.getString("loginPasswordToggle"));
        loginButton.setText(defaultRB.getString("loginLogin"));
        loginExitButton.setText(defaultRB.getString("exit"));
        loginLocationLabel.setText(defaultRB.getString("location"));
        loginCurrLocation.setText(Locale.getDefault().toString());
    }

    public void loginButtonClick(ActionEvent actionEvent) throws SQLException, IOException
    {
        boolean userFound = false;
        int userId;
        String userSelect = "SELECT * FROM user";
        statement.execute(userSelect);
        ResultSet allUsers = statement.getResultSet();

        String usernameInput = loginUsernameTxt.getText();
        String passwordInput = loginPasswordHiddenTxt.getText();

        if ((usernameInput == null) || (passwordInput == null))
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(defaultRB.getString("loginErrorTitle"));
            alert.setHeaderText(defaultRB.getString("loginErrorHeader"));
            alert.setContentText(defaultRB.getString("loginNullMessage"));
            alert.showAndWait();
        }
        else
        {
            while (allUsers.next())
            {
                String userName = allUsers.getString("userName");
                String password = allUsers.getString("password");
                userId = allUsers.getInt("userId");
                int active = allUsers.getInt("active");

                if ((usernameInput.equals(userName)) && (password.equals(passwordInput)))
                {
                    Path path = Paths.get("logins.txt");
                    Files.write(path, Collections.singletonList("User: " + userName + " Logged in at: " + Date.from(Instant.now()).toString()), StandardCharsets.UTF_8, Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
                    userFound = true;
                    currentUser = new User(userId, userName, password);
                    currentUsername = userName;

                    Parent mainRoot = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));
                    Scene mainScreenScene = new Scene(mainRoot, 1280, 720);
                    Stage mainScreenStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                    mainScreenStage.setResizable(true);
                    mainScreenStage.setScene(mainScreenScene);
                    mainScreenStage.show();
                    mainScreenStage.setMaximized(true);

                    System.out.println("Login Successful");
                    String updateUser = "UPDATE user SET active = 1 WHERE userId = " + userId;
                    statement.execute(updateUser);
                    System.out.println(active);
                    break;
                }
            }

            if (!userFound)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(defaultRB.getString("loginErrorTitle"));
                alert.setHeaderText(defaultRB.getString("loginErrorHeader"));
                alert.setContentText(defaultRB.getString("loginErrorMessage"));
                alert.showAndWait();
            }
        }
    }

    public static String getCurrentUser()
    {
        return currentUsername;
    }

    public void togglePassword(ActionEvent actionEvent)
    {
        if (loginPasswordHiddenTxt.isVisible() == true)
        {
            loginPasswordHiddenTxt.setVisible(false);
            loginPasswordVisibleTxt.setVisible(true);
        }
        else
        {
            loginPasswordVisibleTxt.setVisible(false);
            loginPasswordHiddenTxt.setVisible(true);
        }
    }

    public void exitButtonClick(ActionEvent actionEvent)
    {
        System.exit(0);
    }
}
