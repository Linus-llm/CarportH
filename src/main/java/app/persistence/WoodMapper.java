package app.persistence;

import app.entities.Wood;
import app.entities.WoodCategory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WoodMapper {

    public static Wood getWood(ConnectionPool cp, WoodCategory category, int wishLength) throws SQLException {
        String sql =
                "SELECT w.length, wp.id AS profile_id, wp.category, wp.width, wp.height " +
                        "FROM woods w " +
                        "JOIN wood_profiles wp ON wp.id = w.profile_id " +
                        "WHERE wp.category = ? AND w.length >= ? " +
                        "ORDER BY w.length ASC " +
                        "LIMIT 1";

        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, category.ordinal());
            ps.setInt(2, wishLength);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Wood(
                            rs.getInt("profile_id"),
                            WoodCategory.values()[rs.getInt("category")],
                            rs.getInt("width"),
                            rs.getInt("height"),
                            rs.getInt("length")
                    );
                }
            }
        }
        return null;
    }

    public static Wood getWoodById(ConnectionPool cp, int woodId) throws SQLException {
        String sql =
                "SELECT w.length, wp.id AS profile_id, wp.category, wp.width, wp.height " +
                        "FROM woods w " +
                        "JOIN wood_profiles wp ON wp.id = w.profile_id " +
                        "WHERE w.id = ?";

        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, woodId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Wood(
                            rs.getInt("profile_id"),
                            WoodCategory.values()[rs.getInt("category")],
                            rs.getInt("width"),
                            rs.getInt("height"),
                            rs.getInt("length")
                    );
                }
            }
        }
        return null;
    }

}
