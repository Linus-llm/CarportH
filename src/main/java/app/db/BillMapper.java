package app.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public static List<Bill> getBillsByOfferId(ConnectionPool cp, int offerId){
        String sql = "SELECT b.id AS bid, b.wood_id AS wid, b.count AS bcount, b.price AS bprice, " +
                "w.length AS wlength, wp.width AS wpwidth, wp.height AS wpheight, wp.category AS wpcategory " +
                "FROM bills b " +
                "JOIN woods w ON b.wood_id = w.id " +
                "JOIN wood_profiles wp ON w.profile_id = wp.id " +
                "WHERE b.offer_id = ?";

        List<Bill> result = new ArrayList<>();
        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, offerId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                int id = rs.getInt("bid");
                int woodId = rs.getInt("wid");
                int count = rs.getInt("bcount");
                double price = rs.getDouble("bprice");
                int length = rs.getInt("wlength");
                int width = rs.getInt("wpwidth");
                int height = rs.getInt("wpheight");
                WoodCategory category = WoodCategory.values()[rs.getInt("wpcategory")];
                result.add(new Bill(id,woodId,count,length,width,height,price, category));
            }


        } catch(SQLException e) {
            System.err.println(e);
        }
        return result;
    }

    public static boolean addBills(ConnectionPool cp, List<Bill> bills)
    {
        String sql = "INSERT INTO bills (offer_id, wood_id, count, price) VALUES (?, ?, ?, ?)";

        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (Bill b : bills) {
                ps.setInt(1, b.offerId);
                ps.setInt(2, b.woodId);
                ps.setInt(3, b.quantity);
                ps.setDouble(4, b.price);
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (Exception e) {
            System.err.println(e);
        }

        return false;
    }

    public static boolean deleteOfferBills(ConnectionPool cp, int offerId)
    {
        String sql = "DELETE FROM bills WHERE offer_id=?";

        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offerId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println(e);
        }

        return false;
    }
}
