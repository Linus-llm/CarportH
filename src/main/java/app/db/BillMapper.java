package app.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BillMapper {
    public static boolean insert(ConnectionPool cp, Bill bill) {
        String sql = "INSERT INTO bills (offer_id, wood_id, count, price) VALUES (?, ?, ?, ?)";

        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bill.offerId);
            ps.setInt(2, bill.woodId);
            ps.setInt(3, bill.quantity);
            ps.setDouble(4, bill.price);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println(e);
        }

        return false;
    }
}
