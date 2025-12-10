package app.db;
import app.exceptions.DBException;
import app.web.Server;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.security.SecureRandom;
import java.util.List;


public class UserMapper {

    private static byte[] genSalt()
    {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    private static byte[] hashPassword(String password, byte[] salt)
            throws Exception
    {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return factory.generateSecret(spec).getEncoded();
    }

    public static User login(ConnectionPool cp, String email, String password) throws DBException
    {
        User user = null;

        PreparedStatement ps;
        ResultSet rs;
        String sql = "SELECT users.id, users.name, users.email, users.password, users.salt, users.role FROM users WHERE users.email=?";

        try (Connection conn = cp.getConnection()){
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
            throw new DBException("Failed to login: " + e.getMessage());
        }

        return user;
    }

    public static boolean register(ConnectionPool cp, String name, String email, String password) throws DBException
    {
        PreparedStatement ps;
        ResultSet rs;
        String sqlQuery = "SELECT users.id FROM users WHERE users.email=?";
        String sqlUpdate = "INSERT INTO users (name, email, password, salt, role) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = cp.getConnection()){
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, email);
            rs = ps.executeQuery();
            if (!rs.next()) {
                byte[] salt = genSalt();
                byte[] hash = hashPassword(password, salt);
                ps = conn.prepareStatement(sqlUpdate);
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setBytes(3, hash);
                ps.setBytes(4, salt);
                ps.setInt(5, 0);
                return (ps.executeUpdate() > 0);
            }
        } catch (Exception e) {
            throw new DBException("Failed to register: " + e.getMessage());
        }

        return false;
    }

    public static User getUser(ConnectionPool cp, int id)
            throws SQLException
    {
        PreparedStatement ps;
        ResultSet rs;
        String sql = "SELECT users.id, users.name, users.email, users.password, users.salt, users.role FROM users WHERE users.id=?";

        try (Connection conn = cp.getConnection()) {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        UserRole.values()[rs.getInt("role")]);
            }
        }
        return null;
    }

}
