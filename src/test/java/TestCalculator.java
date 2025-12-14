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

        assertEquals(6, c.calcNumberOfPillars(5000, 0));
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

        assertEquals(4, c.calcNumberOfPlanksForShed(100, 100, 100));
    }

    @Test
    public void testCalculatePillarOffs()
    {
        final int length = 6000; // 6 meters
        int i;
        int[] offs;

        offs = CarportCalculator.calcPillarsOffs(length, 0);
        assertNotNull(offs);
        assertTrue(offs.length>=2, "offs.length>=2");
        assertFalse(offs[0] > CarportRules.MAX_PILLAR_SPACING_MM/2, "offs[0] > MAX_PILLAR_SPACING_MM/2");
        for (i = 1; i < offs.length; i++)
            assertFalse(offs[i]-offs[i-1] > CarportRules.MAX_PILLAR_SPACING_MM, "offs["+i+"]-offs["+(i-1)+"] > MAX_PILLAR_SPACING_MM");
        assertFalse(length-offs[offs.length-1] > CarportRules.MAX_PILLAR_SPACING_MM/2, "length-offs[offs.length-1] > MAX_PILLAR_SPACING_MM/2");
    }

}
