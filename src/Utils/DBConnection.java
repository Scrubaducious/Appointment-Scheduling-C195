package Utils;

import com.mysql.jdbc.Connection;

import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection
{
    //JDBC URL parts
    private static final String protocol = "jdbc";
    private static final String vendorName = ":mysql:";
    private static final String ipAddress = "//3.227.166.251/U06SYK";

    //Combined JDBC URL
    private static final String jdbcURL = protocol + vendorName + ipAddress;

    //Connection and Driver Interface reference
    private static final String mysqlJdbcDriver = "com.mysql.jdbc.Driver";
    private static Connection conn = null;

    private static final String username = "U06SYK"; //username
    private static final String password = "53688856222"; //password

    public static Connection startConnection()
    {
        try {
            Class.forName(mysqlJdbcDriver);
            conn = (Connection)DriverManager.getConnection(jdbcURL, username, password);
            System.out.println("Connection was Successful");
        }
        catch(ClassNotFoundException e)
        {
            System.out.println(e.getMessage());
        }
        catch(SQLException e)
        {
            System.out.println(e.getMessage());
        }

        return conn;
    }

    public static void closeConnection()
    {
        try {
            conn.close();
            System.out.println("Connection closed.");
        }
        catch(SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }
}
