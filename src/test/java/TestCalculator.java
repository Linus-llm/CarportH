import app.db.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestCalculator {
    CarportCalculator c = new CarportCalculator();
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

        assertEquals(2, c.calcNumberOfBoardsForRemLength(5000, 5000));
    }

    @Test
    public void testCalcNumberOfBoardsCoverWidth(){

        assertEquals(4, c.calcNumberOfBoardsCoverWidth(5999, 3000));
    }
}
