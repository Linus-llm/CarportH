import app.db.ConnectionPool;
import app.db.Offer;
import app.db.OfferMapper;
import app.db.OfferStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestDB {

    private static final String JDBC_USER = System.getenv("JDBC_USER");
    private static final String JDBC_PASSWORD = System.getenv("JDBC_PASSWORD");
    private static final String JDBC_URL = System.getenv("JDBC_URL");
    private static final String JDBC_DB = System.getenv("JDBC_DB");
    private static ConnectionPool cp;

    @BeforeAll
    public static void beforeAll()
    {
        try {
            cp = new ConnectionPool(JDBC_USER, JDBC_PASSWORD, JDBC_URL, JDBC_DB);
            ClassLoader classLoader = TestDB.class.getClassLoader();
            String initPath = classLoader.getResource("sql/init.sql").getPath();
            String initSQL = Files.readString(Paths.get(initPath));
            String dataPath = classLoader.getResource("sql/data.sql").getPath();
            String dataSQL = Files.readString(Paths.get(dataPath));
            Connection conn = cp.getConnection();
            Statement stmt = conn.createStatement();
            stmt.execute(initSQL);
            stmt.execute(dataSQL);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testOffer()
            throws SQLException
    {
        Offer offer = new Offer(0, 1, 0, "addr 9", 4242, "bobvile", 6000, 2000, 7800, 0, 0, 0.0, "Customer note", OfferStatus.SALESPERSON);
        assertTrue(OfferMapper.addOffer(cp, offer));
        List<Offer> offers = OfferMapper.getSalespersonOffers(cp, 0);
        assertNotNull(offers);
        assertFalse(offers.isEmpty());
        offer = offers.get(0);
        assertTrue(OfferMapper.setSalesperson(cp, offer.id, 2));
        offer.price = 5000.0;
        offer.text += ". salesperson note";
        assertTrue(OfferMapper.updateOffer(cp, offer));
        offers = OfferMapper.getCustomerOffers(cp, 1);
        assertNotNull(offers);
        assertFalse(offers.isEmpty());
    }

    @Test
    public void testGetWood(){
        try {
            Wood wood = WoodMapper.getWood(cp, WoodCategory.PILLAR, 2000);
            assertNotNull(wood);
            System.out.println(wood.id+", "+wood.category+", "+wood.length+", "+wood.pricePerMeter);
        } catch (SQLException e) {
            fail(e);
        }
    }

}
