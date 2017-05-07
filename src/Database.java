
import com.mysql.jdbc.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Ilkin
 */
public class Database {

    String myDriver = "com.mysql.jdbc.Driver";
    String db = "jdbc:mysql://localhost/osgb";

    Connection con = null;
    Statement st = null;

    public boolean connection() {

        try {
            Class.forName(myDriver);
            con = (Connection) DriverManager.getConnection(db);

            System.out.println("Connection: " + con);
            return true;

        } catch (Exception e) {
            System.out.println("Some error: " + e);
            return false;
        }

    }

    public java.sql.ResultSet query(String sorgu) {

        try {
            st = (Statement) con.createStatement();
            java.sql.ResultSet rs = st.executeQuery(sorgu);
            System.out.println(sorgu + " - Query executed");
            return rs;

        } catch (Exception e) {
            System.out.println("Some error occured during executing query: "+e);
        }

        return null;
    }
    
    
    public void query_udi(String sorgu){
                try {
            st = (Statement) con.createStatement();
            
            // create, update, insert, delete gibi data manipulation query`ler executeUpdate ile calistirilir
            st.executeUpdate(sorgu);
            System.out.println("Update/delete/insert query executed");

        } catch (Exception e) {
                    System.err.println("Some error occured during execution of insert/delete/update: "+ e);
            
        }

    }

    public void destroy() {

        System.out.println(db);
        try {
            System.out.println("Destroy called");
            System.out.println(con);
            st.close();
            con.close();

        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
