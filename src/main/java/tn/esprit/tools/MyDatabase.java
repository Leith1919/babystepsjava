package tn.esprit.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class MyDatabase {
   public final  String  url ="jdbc:mysql://localhost:3306/leithpidev";
    public String user="root";
    public String password="";
    private Connection cnx;
    public static MyDatabase MyDatabase;
    private MyDatabase()  {
        try {
            cnx = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to database");
        }catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public static MyDatabase getInstance() {
        if(MyDatabase == null)
            MyDatabase = new MyDatabase();
        return MyDatabase;


    }

    public Connection getCnx() {
        return cnx;
    }
}
