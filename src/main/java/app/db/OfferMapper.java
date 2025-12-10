package app.db;

import app.exceptions.DBException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OfferMapper {

    private static Offer rsToOffer(ResultSet rs)
            throws SQLException
    {
        return new Offer(
                rs.getInt("id"),
                rs.getInt("customer_id"),
                rs.getInt("salesperson_id"),
                rs.getString("address"),
                rs.getInt("postalcode"),
                rs.getString("city"),
                rs.getInt("width"),
                rs.getInt("height"),
                rs.getInt("length"),
                rs.getInt("shed_width"),
                rs.getInt("shed_length"),
                rs.getDouble("price"),
                rs.getString("text"),
                OfferStatus.values()[rs.getInt("status")]);
    }

    public static List<Offer> getOffersWhere(ConnectionPool cp, String where, Object... args)
            throws DBException {
        ResultSet rs;
        String sql = "SELECT * FROM offers JOIN postalcodes ON offers.postalcode=postalcodes.postalcode "+where;
        List<Offer> offers = new ArrayList<>();
        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 0;
            for (Object arg : args)
                ps.setObject(++i, arg);
            rs = ps.executeQuery();
            while (rs.next())
                offers.add(rsToOffer(rs));
        } catch (SQLException e){
            throw new DBException("Failed to get offers: " + e.getMessage());
        }
        return offers;
    }

    public static List<Offer> getCustomerOffers(ConnectionPool cp, int customerId)
            throws DBException {
        return getOffersWhere(cp, "WHERE customer_id=?", customerId);
    }

    public static List<Offer> getSalespersonOffers(ConnectionPool cp, int salespersonId)
            throws  DBException {
        return getOffersWhere(cp, "WHERE salesperson_id=?", salespersonId);
    }

    public static Offer getOffer(ConnectionPool cp, int id)
            throws DBException {
        ResultSet rs;
        String sql = "SELECT * FROM offers JOIN postalcodes ON offers.postalcode=postalcodes.postalcode WHERE id=?";
        Offer offer = null;
        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next())
                offer = rsToOffer(rs);
        } catch (SQLException e){
            throw new DBException("Failed to get offer: " + e.getMessage());
        }
        return offer;
    }

    public static boolean addOffer(ConnectionPool cp, Offer offer)
            throws DBException {
        String sql = "INSERT INTO offers (customer_id, salesperson_id, address, postalcode, width, height, length, shed_width, shed_length, price, text, status) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 0;
            ps.setInt(++i, offer.customerId);
            ps.setInt(++i, offer.salespersonId);
            ps.setString(++i, offer.address);
            ps.setInt(++i, offer.postalcode);
            ps.setInt(++i, offer.width);
            ps.setInt(++i, offer.height);
            ps.setInt(++i, offer.length);
            ps.setInt(++i, offer.shedWidth);
            ps.setInt(++i, offer.shedLength);
            ps.setDouble(++i, offer.price);
            ps.setString(++i, offer.text);
            ps.setInt(++i, offer.status.ordinal());
            return ps.executeUpdate() == 1;
        } catch (SQLException e){
            throw new DBException("Failed to add offer: " + e.getMessage());
        }
    }

    public static boolean addQuery(ConnectionPool cp, Offer offer)
            throws DBException
    {
        String sql = "INSERT INTO offers (customer_id, address, postalcode, width, height, length, shed_width, shed_length, text, status) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            int i = 0;
            ps.setInt(++i, offer.customerId);
            ps.setString(++i, offer.address);
            ps.setInt(++i, offer.postalcode);
            ps.setInt(++i, offer.width);
            ps.setInt(++i, offer.height);
            ps.setInt(++i, offer.length);
            ps.setInt(++i, offer.shedWidth);
            ps.setInt(++i, offer.shedLength);
            ps.setString(++i, offer.text);
            ps.setInt(++i, offer.status.ordinal());
            if (ps.executeUpdate() != 1) return false;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) offer.id = keys.getInt(1);
            }
            return true;
        } catch (SQLException e){
            throw new DBException("Failed to add query: " + e.getMessage());
        }
    }

    public static boolean updateOffer(ConnectionPool cp, Offer offer)
            throws DBException
    {
        String sql = "UPDATE offers SET width=?, height=?, length=?, shed_width=?, shed_length=?, price=?, text=?, status=? WHERE id=?";
        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 0;
            ps.setInt(++i, offer.width);
            ps.setInt(++i, offer.height);
            ps.setInt(++i, offer.length);
            ps.setInt(++i, offer.shedWidth);
            ps.setInt(++i, offer.shedLength);
            ps.setDouble(++i, offer.price);
            ps.setString(++i, offer.text);
            ps.setInt(++i, offer.status.ordinal());
            ps.setInt(++i, offer.id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e){
            throw new DBException("Failed to update offer: " + e.getMessage());
        }
    }

    public static boolean setSalesperson(ConnectionPool cp, int id, int salespersonId)
            throws DBException
    {
        String sql = "UPDATE offers SET salesperson_id=? WHERE id=?";
        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, salespersonId);
            ps.setInt(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e){
            throw new DBException("Failed to set salesperson: " + e.getMessage());
        }
    }
    // Here we collect all unassigned offers/queries
    public static List<Offer> getUnassignedOffers(ConnectionPool cp)
            throws DBException
    {
        return getOffersWhere(cp,
                "WHERE salesperson_id IS NULL AND status=?",
                OfferStatus.SALESPERSON.ordinal());
    }

    // the seller assigns himself to an offer
    public static boolean assignSalesperson(ConnectionPool cp, int offerId, int salespersonId)
            throws DBException
    {
        String sql = "UPDATE offers SET salesperson_id=? WHERE id=?";
        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, salespersonId);
            ps.setInt(2, offerId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DBException("Failed to assign salesperson: " + e.getMessage());
        }
    }
// update to the price
    public static void updatePrice(ConnectionPool cp, int offerId, double price) throws DBException {
        String sql = "UPDATE offers SET price=? WHERE id=?";

        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, price);
            ps.setInt(2, offerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DBException("Failed to update offer price: " + e.getMessage());
        }
    }
}
