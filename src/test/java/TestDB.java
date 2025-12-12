import app.db.*;
import app.exceptions.DBException;
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

    private static ConnectionPool cp;


    @BeforeAll
    public static void setupDatabase() throws Exception {

        cp = new ConnectionPool("postgres", "postgres", "jdbc:postgresql://localhost:5432/carport", "carport");

        // Run test init + data to prepare `test` schema
        try (Connection conn = cp.getConnection();
             Statement stmt = conn.createStatement()) {

            String initSql = Files.readString(Paths.get("src/test/resources/sql/init.sql"));
            String dataSql = Files.readString(Paths.get("src/test/resources/sql/data.sql"));

            stmt.execute(initSql);
            stmt.execute(dataSql);
        }
    }

    @Test
    public void testOffer()
            throws SQLException, DBException {
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
    public void testGetWood() throws DBException {

        Wood wood = WoodMapper.getWood(cp, WoodCategory.PILLAR, 2000);
        assertNotNull(wood);
        assertEquals(WoodCategory.PILLAR, wood.category);
        assertTrue(wood.length >= 2000, "Wood lenght should be at least the wished length");
    }
    @Test
    void testLoginValidUserAndreturnsUser() throws DBException {
        // arrange
        String email = "ole@customer.dk";
        // password is irrelevant because your hashing is not used in test;
        // use whatever your login expects for the seeded user
        String password = "password";

        // act
        User user = UserMapper.login(cp, email, password);

        // assert
        assertNotNull(user, "User should not be null for valid credentials");
        assertEquals(email, user.email);
    }

    @Test
    void testInsertAndGetBillsByOfferId() throws Exception {

        Offer offer = new Offer(
                0,
                1,
                1,
                "Testvej 1",
                4242,
                "Bobville",
                3000,
                2200,
                4800,
                0,
                0,
                0.0,
                "test",
                OfferStatus.SALESPERSON
        );
        assertTrue(OfferMapper.addOffer(cp, offer), "Offer insert should succeed");

        List<Offer> offers = OfferMapper.getCustomerOffers(cp, offer.customerId);
        assertFalse(offers.isEmpty(), "Customer should have at least one offer");
        Offer dbOffer = offers.get(offers.size() - 1);

        Wood wood = WoodMapper.getWood(cp, WoodCategory.RAFTER, 3000);
        assertNotNull(wood, "Expected a BEAM wood of 3000mm in test data");

        Bill bill = new Bill(
                dbOffer.id,
                wood.id,
                2,
                "helptext.todo",
                123.45
        );
        assertTrue(BillMapper.insert(cp, bill), "Bill insert should succeed");
        List<Bill> bills = BillMapper.getBillsByOfferId(cp, dbOffer.id);
        assertEquals(1, bills.size(), "Exactly one bill expected for this offer");
        Bill b = bills.get(0);
        assertEquals(wood.id, b.woodId);
        assertEquals(2, b.quantity);
        assertEquals(3000, b.length);
        assertEquals(WoodCategory.RAFTER, b.category);
        assertEquals("helptext.todo", b.helptext);
    }


}
