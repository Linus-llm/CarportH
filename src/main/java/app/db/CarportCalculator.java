package app.db;

import java.util.ArrayList;
import java.util.List;

public class CarportCalculator {


        public int calcNumberOfPillars(int lengthMm, int widthMm) {
            int perRow = (int) Math.floor((double) lengthMm / CarportRules.MAX_PILLAR_SPACING_MM) + 1;

            //width cannot exceed 6000mm, so max 3 rows of pillars
            int rows = (widthMm > CarportRules.MAX_PILLAR_SPACING_MM) ? 3 : 2;
            return Math.max((perRow * rows), CarportRules.MIN_PILLARS);
        }
        //return gaps +1 because there needs to be at least two rafters.
        public int calcNumberOfRafters(int lengthMm) {
            int gaps = (int) Math.floor((double) lengthMm / CarportRules.RAFTER_SPACING_MM);
            return gaps + 1;
        }
        // at least 2 boards for the length of the rem because there is two length-sides
        public int calcNumberOfBoardsForRemLength(int lengthMm, int boardLengthMm) {
            return Math.max(((int) Math.ceil((double) lengthMm / boardLengthMm)), CarportRules.MIN_BOARDS);
        }
        // at least 2 boards for the length of the rem because there is two width-sides
        public int calcNumberOfBoardsCoverWidth(int widthMm, int boardWidthMm) {
            return Math.max(((int) Math.ceil((double) widthMm / boardWidthMm)), CarportRules.MIN_BOARDS);
        }
        // this uses the above methods to calculate the total list of wood needed for a simple carport based on the dimensions.
        public List<WoodNeed> calculateNeeds(int lengthMm, int widthMm, int heightMm) {
            List<WoodNeed> needs = new ArrayList<>();

            // this adds the pieces of wood needed for the pillars
            int pillarCount = calcNumberOfPillars(lengthMm, widthMm);
            needs.add(new WoodNeed(WoodCategory.PILLAR, heightMm, pillarCount));

            // this adds the pieces of wood neded or the rafters/spær
            int rafterCount = calcNumberOfRafters(lengthMm);
            needs.add(new WoodNeed(WoodCategory.RAFTER, widthMm, rafterCount));

            // this adds the pieces of wood needed for the remme/boards
            needs.add(new WoodNeed(WoodCategory.BOARD, lengthMm, 2));
            needs.add(new WoodNeed(WoodCategory.BOARD, widthMm, 2));

            // returns the full list of wood needed
            return needs;


        }
}
