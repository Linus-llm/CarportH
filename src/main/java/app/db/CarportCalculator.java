package app.db;

import java.sql.Connection;
import java.util.ArrayList;
import app.web.Server;
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
    public int calcNumberOfBeamsForRemLength(int lengthMm, int boardLengthMm) {
        return Math.max(((int) Math.ceil((double) lengthMm / boardLengthMm)), CarportRules.MIN_BOARDS);
    }

    // at least 2 boards for the length of the rem because there is two width-sides
    public int calcNumberOfBeamsCoverWidth(int widthMm, int boardWidthMm) {
        return Math.max(((int) Math.ceil((double) widthMm / boardWidthMm)), CarportRules.MIN_BOARDS);
    }
    public int calcNumberOfPlanksForShed(int shedLengthMm, int shedWidthMm, int plankLengthMm) {
        int planksForLength = (int) Math.ceil((double) shedLengthMm / plankLengthMm) * 2;
        int planksForWidth = (int) Math.ceil((double) shedWidthMm / plankLengthMm) * 2;
        return Math.max(planksForLength + planksForWidth, CarportRules.MIN_PLANKS_IF_SHED);
    }

    // this uses the above methods to calculate the total list of wood needed for a simple carport based on the dimensions.
    public List<WoodNeed> calculateNeeds(ConnectionPool cp, int lengthMm, int widthMm, int heightMm) {
        List<WoodNeed> needs = new ArrayList<>();

        // this adds the pieces of wood needed for the pillars
        int pillarCount = calcNumberOfPillars(lengthMm, widthMm);
        needs.add(new WoodNeed(WoodCategory.PILLAR, heightMm, pillarCount));

        // this adds the pieces of wood neded or the rafters/spær
        int rafterCount = calcNumberOfRafters(lengthMm);
        needs.add(new WoodNeed(WoodCategory.RAFTER, widthMm, rafterCount));

        // this adds the pieces of wood needed for the remme/beams
        //width
        Wood wood = null;
        try{
            wood = WoodMapper.getWood(cp, WoodCategory.BEAM, widthMm);}
        catch(Exception e){
            System.out.println("ERROR: " + e.getMessage());
        }
        int beamCountWidth = calcNumberOfBeamsCoverWidth(widthMm, wood.length);
        needs.add(new WoodNeed(WoodCategory.BEAM, widthMm, beamCountWidth));
        //length
        try{
            wood = WoodMapper.getWood(cp, WoodCategory.BEAM, lengthMm);}
        catch(Exception e){
            System.out.println("ERROR: " + e.getMessage());
        }
        int beamCountLength = calcNumberOfBeamsForRemLength(widthMm, wood.length);
        needs.add(new WoodNeed(WoodCategory.BEAM, widthMm, beamCountLength));

        // returns the full list of wood needed
        return needs;


    }

    public List<WoodNeed> calculateNeedsWithShed(ConnectionPool cp, int lengthMm, int widthMm, int heightMm, int shedWidthMm, int shedLengthMm) {
        List<WoodNeed> needs = calculateNeeds(cp, lengthMm, widthMm, heightMm);
        if (shedWidthMm <= 0 || shedLengthMm <= 0) {
            return needs;
        }
        // adding extra pillars for the shed and calculating planks needed
        if (shedWidthMm <= 3000) {
            needs.add(new WoodNeed(WoodCategory.PILLAR, heightMm, CarportRules.MIN_PILLARS_IF_SHED - CarportRules.MIN_PILLARS));
            Wood wood = null;
            try{
                wood = WoodMapper.getWood(cp, WoodCategory.BEAM, 0);}
            catch(Exception e){
                System.out.println("ERROR: " + e.getMessage());
            }
            int plankCount = calcNumberOfPlanksForShed(shedLengthMm, shedWidthMm, wood.height);
            needs.add(new WoodNeed(WoodCategory.PLANK, wood.height, plankCount));
        } else {
            needs.add(new WoodNeed(WoodCategory.PILLAR, heightMm, CarportRules.MIN_PILLARS_IF_SHED+1 - CarportRules.MIN_PILLARS));
            Wood wood = null;
            try{
                wood = WoodMapper.getWood(cp, WoodCategory.BEAM, 0);}
            catch(Exception e){
                System.out.println("ERROR: " + e.getMessage());
            }
            int plankCount = calcNumberOfPlanksForShed(shedLengthMm, shedWidthMm, wood.height);
            needs.add(new WoodNeed(WoodCategory.PLANK, wood.height, plankCount));
        }
        return needs;
    }
}
