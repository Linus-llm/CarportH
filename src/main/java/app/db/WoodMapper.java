package app.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WoodMapper {

    //This method retrieves a wood piece from the database that matches the specified category of wood and has a length greater than or equal to the desired length.
    //The sql query is very specific about using the best matched length and not just any piece that is longer than the required length.
    public static Wood getWood(ConnectionPool cp, WoodCategory category, int wishLength) throws SQLException {
        String sql =
                "SELECT w.id AS wood_id, w.length, " +
                        "       wp.id AS profile_id, wp.category, wp.width, wp.height, wp.price " +
                        "FROM woods w " +
                        "JOIN wood_profiles wp ON wp.id = w.profile_id " +
                        "WHERE wp.category = ? AND w.length >= ? " +
                        "ORDER BY (w.length - ?) ASC " +
                        "LIMIT 1";

        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, category.ordinal());
            ps.setInt(2, wishLength);
            ps.setInt(3, wishLength);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Wood(
                            rs.getInt("wood_id"),
                            rs.getInt("profile_id"),
                            WoodCategory.values()[rs.getInt("category")],
                            rs.getInt("width"),
                            rs.getInt("height"),
                            rs.getInt("length"),
                            rs.getDouble("price")
                    );
                }
            }
        }
        return null;
    }
    public static List<Wood> getWoodsByCategory(ConnectionPool cp, WoodCategory category) throws SQLException {
        String sql = "SELECT w.id, w.profile_id, w.length, wp.price, wp.category, wp.width, wp.height " +
                "FROM woods w " +
                "JOIN wood_profiles wp ON w.profile_id = wp.id " +
                "WHERE wp.category = ? " +
                "ORDER BY w.length";
        List<Wood> result = new ArrayList<>();
        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, category.ordinal());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Wood(
                            rs.getInt("id"),
                            rs.getInt("profile_id"),
                            WoodCategory.values()[rs.getInt("category")],
                            rs.getInt("width"),
                            rs.getInt("height"),
                            rs.getInt("length"),
                            rs.getDouble("price")
                    ));
                }
            }
        }
        return result;
    }

}
