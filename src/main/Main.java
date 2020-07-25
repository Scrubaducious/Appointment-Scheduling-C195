package main;

import Utils.DBConnection;
import Utils.DBQuery;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.ResourceBundle;

public class Main extends Application
{
    Stage mainStage;
    Scene loginScene;

    public static void main(String[] args) throws SQLException
    {
        Connection conn = DBConnection.startConnection();
        DBQuery.setStatement(conn); //Create Statement Object
        Statement statement = DBQuery.getStatement();

        /*
        //Variable Insert
        String countryName = "Canada";
        String createDate = "2020-05-25 00:00:00";
        String createdBy = "admin";
        String lastUpdateBy = "admin";

        String insertStatement = "INSERT INTO country(country, createDate, createdBy, lastUpdateBy) " +
                "VALUES(" +
                "'" + countryName + "'," +
                "'" + createDate + "'," +
                "'" + createdBy + "'," +
                "'" + lastUpdateBy + "'" +
                ")";


        //Execute SQL Statement
        statement.execute(insertStatement);

        //Confirm rows affected
        if(statement.getUpdateCount() > 0)
            System.out.println(statement.getUpdateCount() + " row(s) affected");
        else
            System.out.println("No change");
         */

        launch(args);

        DBConnection.closeConnection();
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        primaryStage.setResizable(false);
        Parent loginRoot = FXMLLoader.load(getClass().getResource("LoginScreen.fxml"));
        loginScene = new Scene(loginRoot, 450,600);
        primaryStage.setTitle("Appointment Manager");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }
}
