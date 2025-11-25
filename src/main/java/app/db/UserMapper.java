package app.db;
import app.web.Server;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;


public class UserMapper {

    private static byte[] hashPassword(String password, byte[] salt)
            throws Exception
    {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return factory.generateSecret(spec).getEncoded();
    }

    public static User login(String email, String password)
    {
        User user = null;

        PreparedStatement ps;
        ResultSet rs;
        String sql = "SELECT users.id, users.name, users.email, users.password, users.role FROM users WHERE users.email=?";

        try (Connection conn = Server.connectionPool.getConnection()){
            ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            rs = ps.executeQuery();
            if (rs.next()) {
                byte[] inHash = rs.getBytes("password");
                byte[] inSalt = rs.getBytes("salt");
                byte[] hash = hashPassword(password, inSalt);
                if (Arrays.equals(hash, inHash)) {
                    user = new User(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getInt("role") == 1 ? UserRole.SALESPERSON : UserRole.CUSTOMER );

                }
            }
            assert !rs.next();

        } catch (Exception e) {
            System.err.println(e);
        }

        return user;
    }
}
