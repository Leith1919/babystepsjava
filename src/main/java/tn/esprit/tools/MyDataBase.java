package tn.esprit.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    public final String URL = "jdbc:mysql://localhost:3306/pidev";
    public final String USER = "root";
    public final String PASSWORD = "";
    private Connection cnx;
    public static MyDataBase myDataBase;

    private MyDataBase() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to the database successfully");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }
    public static MyDataBase getInstance() {
        if(myDataBase==null)
            myDataBase = new MyDataBase();
        return myDataBase;
    }
    public Connection getCnx() {
        return cnx;
    }


}
