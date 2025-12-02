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

        public int calcNumberOfRafters(int lengthMm) {
            int gaps = (int) Math.floor((double) lengthMm / CarportRules.RAFTER_SPACING_MM);
            return gaps + 1;
        }

        public int calcNumberOfBoardsForRemLength(int lengthMm, int boardLengthMm) {
            return Math.max(((int) Math.ceil((double) lengthMm / boardLengthMm)), CarportRules.MIN_BOARDS);
        }

        public int calcNumberOfBoardsCoverWidth(int widthMm, int boardWidthMm) {
            return Math.max(((int) Math.ceil((double) widthMm / boardWidthMm)), CarportRules.MIN_BOARDS);
        }

        public List<WoodNeed> calculateNeeds(int lengthMm, int widthMm, int heightMm) {
            List<WoodNeed> needs = new ArrayList<>();

            // 1) Stolper
            int pillarCount = calcNumberOfPillars(lengthMm, widthMm);
            needs.add(new WoodNeed(WoodCategory.PILLAR, heightMm, pillarCount));

            // 2) Spær: go across the width
            int rafterCount = calcNumberOfRafters(lengthMm);
            needs.add(new WoodNeed(WoodCategory.RAFTER, widthMm, rafterCount));

            // 3) Rem langs længden (2 remme)
            needs.add(new WoodNeed(WoodCategory.BOARD, lengthMm, 2));
            // 4) Rem langs bredden (2 remme)
            needs.add(new WoodNeed(WoodCategory.BOARD, widthMm, 2));

            return needs;


        }
}
