import app.db.*;
import app.exceptions.CarportCalculationException;
import app.exceptions.DBException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestCalculator {
    CarportCalculator c = new CarportCalculator();
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
    public void testCalcNumberOfPillars(){

        assertEquals(9, c.calcNumberOfPillars(5000, 5999));
    }
    @Test
    public void testCalcNumberOfRafters(){

        assertEquals(10, c.calcNumberOfRafters(5000));
    }
    @Test
    public void testCalcNumberOfBoardsForRemLength(){

        assertEquals(2, c.calcNumberOfBeamsForRemLength(5000, 5000));
    }

    @Test
    public void testCalcNumberOfBoardsCoverWidth(){

        assertEquals(4, c.calcNumberOfBeamsCoverWidth(5999, 3000));
    }
    @Test
    public void testCalcNumberOfPlanksForShed(){

        assertEquals(200, c.calcNumberOfPlanksForShed(1000, 1000, 100));
    }
    @Test
    public void testCalculateNeeds() throws DBException, CarportCalculationException {
        int length = 6000;
        int width = 4000;
        int height = 2215;
        List<WoodNeed> needs = c.calculateNeeds(cp, length, width, height);
        for (WoodNeed n : needs) {
            System.out.println(n.type + " | length≥" + n.requiredLengthMm + " | count=" + n.count);
        }
    }
    @Test
    public void testCalculateNeedsWithShed() throws DBException, CarportCalculationException {
        int length = 6000;
        int width = 4000;
        int height = 2215;
        int shedLength = 2000;
        int shedWidth = 4000;
        List<WoodNeed> needs = c.calculateNeedsWithShed(cp, length, width, height, shedWidth, shedLength);
        for (WoodNeed n : needs) {
            System.out.println(n.type + " | length≥" + n.requiredLengthMm + " | count=" + n.count);
        }
    }

}
