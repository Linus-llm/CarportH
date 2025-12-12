package app.db;

import app.exceptions.DBException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BillMapper {
    public static boolean insert(ConnectionPool cp, Bill bill) throws DBException {
        String sql = "INSERT INTO bills (offer_id, wood_id, count, helptext, price) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bill.offerId);
            ps.setInt(2, bill.woodId);
            ps.setInt(3, bill.quantity);
            ps.setString(4, bill.helptext);
            ps.setDouble(5, bill.price);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DBException("Failed to insert bill" + e.getMessage());
        }
    }

    public static List<Bill> getBillsByOfferId(ConnectionPool cp, int offerId) throws DBException {
        String sql = "SELECT b.id AS bid, b.wood_id AS wid, b.count AS bcount, helptext, b.price AS bprice, " +
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
                String helptext = rs.getString("helptext");
                WoodCategory category = WoodCategory.values()[rs.getInt("wpcategory")];
                result.add(new Bill(id,woodId,count,length,width,height,helptext, price, category));
            }
        } catch(SQLException e) {
            throw new DBException("Failed to get bills by offer ID" + e.getMessage());
        }
        return result;
    }

    public static boolean addBills(ConnectionPool cp, List<Bill> bills) throws DBException
    {
        String sql = "INSERT INTO bills (offer_id, wood_id, count, helptext, price) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false); //this makes sure that either all inserts are done, or none
            for (Bill b : bills) {
                ps.setInt(1, b.offerId);
                ps.setInt(2, b.woodId);
                ps.setInt(3, b.quantity);
                ps.setString(4, b.helptext);
                ps.setDouble(5, b.price);
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            throw new DBException("Failed to add bills" + e.getMessage());
        }
    }

    public static boolean deleteOfferBills(ConnectionPool cp, int offerId) throws DBException
    {
        String sql = "DELETE FROM bills WHERE offer_id=?";

        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DBException("Failed to delete offer bills" + e.getMessage());
        }
    }
}
