package app.db;

import java.util.ArrayList;
import java.util.List;

public class WoodNeed {
    public final WoodCategory type;
    public final int requiredLengthMm; // length this piece must be able to cover
    public final int count;
    CarportCalculator cc = new CarportCalculator();

    public WoodNeed(WoodCategory type, int requiredLengthMm, int count) {
        this.type = type;
        this.requiredLengthMm = requiredLengthMm;
        this.count = count;
    }


   /* public List<WoodNeed> calculateNeeds(int lengthMm, int widthMm, int heightMm) {
        List<WoodNeed> needs = new ArrayList<>();

        // 1) Stolper
        int pillarCount = cc.calcNumberOfPillars(lengthMm, widthMm);
        needs.add(new WoodNeed(WoodCategory.PILLAR, heightMm, pillarCount));

        // 2) Spær: go across the width
        int rafterCount = cc.calcNumberOfRafters(lengthMm);
        needs.add(new WoodNeed(WoodCategory.RAFTER, widthMm, rafterCount));

        // 3) Rem langs længden (2 remme)
        needs.add(new WoodNeed(WoodCategory.BOARD, lengthMm, 2));
        // 4) Rem langs bredden (2 remme)
        needs.add(new WoodNeed(WoodCategory.BOARD, widthMm, 2));

        return needs;
    } */

}
