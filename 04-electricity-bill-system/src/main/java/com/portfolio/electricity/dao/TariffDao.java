package com.portfolio.electricity.dao;

import com.portfolio.electricity.model.TariffSlab;
import com.portfolio.electricity.util.DatabaseManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for progressive tariff slabs.
 */
public class TariffDao {

    public List<TariffSlab> findAllSlabs() throws SQLException {
        List<TariffSlab> slabs = new ArrayList<>();
        String sql = "SELECT * FROM tariff_slabs ORDER BY slab_order ASC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                slabs.add(new TariffSlab(
                        rs.getInt("slab_order"),
                        rs.getLong("min_units"),
                        rs.getLong("max_units"),
                        rs.getDouble("rate_per_unit")
                ));
            }
        }
        return slabs;
    }
}
