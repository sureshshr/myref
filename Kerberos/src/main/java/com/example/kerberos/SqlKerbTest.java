import javax.security.auth.login.LoginContext;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class SqlKerbTest {
    public static void main(String[] args) throws Exception {
        String jaasProfile = (args.length > 0) ? args[0] : "KerberosUser";
        String url = "jdbc:sqlserver://dbhost.corp.example.com:1433;"
                + "databaseName=YourDb;"
                + "integratedSecurity=true;"
                + "authenticationScheme=JavaKerberos";

        LoginContext lc = new LoginContext(jaasProfile);
        lc.login();
        System.out.println("✅ JAAS login successful for JDBC");

        try (Connection conn = DriverManager.getConnection(url);
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT SYSTEM_USER, SUSER_SNAME()")) {
            while (rs.next()) {
                System.out.println("Connected as: " + rs.getString(1) + " / " + rs.getString(2));
            }
        }
    }
}
