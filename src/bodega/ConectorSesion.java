package bodega;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author luisGonzalez
 */
public class ConectorSesion {
    
    private static Connection conn;
    private static final String driver = "com.mysql.jdbc.Driver";
    private static final String user = "root";
    private static final String password = "root";
    private static final String url = "jdbc:mysql://localhost:3306/Code_n_Bugs";
    
    public ConectorSesion(){
        conn = null;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, password);
            if(conn != null){
            }
        } catch(ClassNotFoundException | SQLException e){
            System.out.println("Error al conectar "+e);
        }
    }
    
    public Connection getConnection(){
        return conn;
    }
    
    public void Desconectar(){
        conn = null;
        if(conn == null){
            System.out.println("----------------------");
        }
    }
}
