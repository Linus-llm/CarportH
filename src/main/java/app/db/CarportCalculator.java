package app.db;

import app.exceptions.CarportCalculationException;
import app.exceptions.DBException;
import java.util.ArrayList;
import java.util.List;

public class CarportCalculator {


    public static int calcNumberOfPillars(int lengthMm, int widthMm) {
        int perRow = (int) Math.floor((double) lengthMm / CarportRules.MAX_PILLAR_SPACING_MM) + 1;

        //width cannot exceed 6000mm, so max 3 rows of pillars
        int rows = (widthMm > CarportRules.MAX_PILLAR_SPACING_MM) ? 3 : 2;
        return Math.max((perRow * rows), CarportRules.MIN_PILLARS);
    }

    //return gaps +1 because there needs to be at least two rafters.
    public static int calcNumberOfRafters(int lengthMm) {
        int gaps = (int) Math.floor((double) lengthMm / CarportRules.RAFTER_SPACING_MM);
        return gaps + 1;
    }

    // at least 2 boards for the length of the rem because there is two length-sides
    public static int calcNumberOfBeamsForRemLength(int lengthMm, int boardLengthMm) {
        return Math.max(((int) Math.ceil((double) lengthMm / boardLengthMm)), CarportRules.MIN_BOARDS);
    }

    // at least 2 boards for the length of the rem because there is two width-sides
    public static int calcNumberOfBeamsCoverWidth(int widthMm, int boardWidthMm) {
        return Math.max(((int) Math.ceil((double) widthMm / boardWidthMm)), CarportRules.MIN_BOARDS);
    }
    public static int calcNumberOfPlanksForShed(int shedLengthMm, int shedWidthMm, int plankLengthMm) {
        int planksForLength = (int) Math.ceil((double) shedLengthMm / plankLengthMm) * 2;
        int planksForWidth = (int) Math.ceil((double) shedWidthMm / plankLengthMm) * 2;
        return Math.max(planksForLength + planksForWidth, CarportRules.MIN_PLANKS_IF_SHED);
    }

    // this uses the above methods to calculate the total list of wood needed for a simple carport based on the dimensions.
    public static List<WoodNeed> calculateNeeds(ConnectionPool cp, int lengthMm, int widthMm, int heightMm) throws CarportCalculationException, DBException {
        List<WoodNeed> needs = new ArrayList<>();

        // this adds the pieces of wood needed for the pillars
        int pillarCount = calcNumberOfPillars(lengthMm, widthMm);
        needs.add(new WoodNeed(WoodCategory.PILLAR, heightMm, pillarCount));

        // this adds the pieces of wood needed for the rafters
        int rafterCount = calcNumberOfRafters(lengthMm);
        needs.add(new WoodNeed(WoodCategory.RAFTER, widthMm, rafterCount));

        // this adds the pieces of wood needed for the beams
        //width
        Wood wood = null;

        wood = WoodMapper.getWood(cp, WoodCategory.BEAM, widthMm);

        if (wood == null) {
            throw new CarportCalculationException("No wood found for BEAM width " + widthMm);
        }
        int beamCountWidth = calcNumberOfBeamsCoverWidth(widthMm, wood.length);
        needs.add(new WoodNeed(WoodCategory.BEAM, widthMm, beamCountWidth));
        //length

        wood = WoodMapper.getWood(cp, WoodCategory.BEAM, lengthMm);

        if (wood == null) {
            throw new CarportCalculationException("No wood found for BEAM length " + lengthMm);
        }
        int beamCountLength = calcNumberOfBeamsForRemLength(widthMm, wood.length);
        needs.add(new WoodNeed(WoodCategory.BEAM, widthMm, beamCountLength));

        // returns the full list of wood needed
        return needs;


    }

    public static List<WoodNeed> calculateNeedsWithShed(ConnectionPool cp, int lengthMm, int widthMm, int heightMm, int shedWidthMm, int shedLengthMm) throws CarportCalculationException, DBException {
        List<WoodNeed> needs = calculateNeeds(cp, lengthMm, widthMm, heightMm);
        if (shedWidthMm <= 0 || shedLengthMm <= 0) {
            return needs;
        }
        Wood wood = null;
        // adding extra pillars for the shed and calculating planks needed
        if (shedWidthMm <= 3000) {
            needs.add(new WoodNeed(WoodCategory.PILLAR, heightMm, CarportRules.MIN_PILLARS_IF_SHED - CarportRules.MIN_PILLARS));
            wood = WoodMapper.getWood(cp, WoodCategory.BEAM, heightMm);
            if (wood == null) {
                throw new CarportCalculationException("No suitable BEAM found for shed planks");
            }
            int plankCount = calcNumberOfPlanksForShed(shedLengthMm, shedWidthMm, wood.height);
            needs.add(new WoodNeed(WoodCategory.PLANK, wood.height, plankCount));

        } else {
            needs.add(new WoodNeed(WoodCategory.PILLAR, heightMm, CarportRules.MIN_PILLARS_IF_SHED+1 - CarportRules.MIN_PILLARS));

            //the wished length here refers to the height of the plank needed for the shed walls thus same height as pillar
            wood = WoodMapper.getWood(cp, WoodCategory.BEAM, heightMm);

            if (wood == null) {
                throw new CarportCalculationException("No suitable BEAM found for shed planks");
            }
            //we take the height of the wood piece (NOT THE LENGTH) because planks are stood up vertically for the shed walls
            int plankCount = calcNumberOfPlanksForShed(shedLengthMm, shedWidthMm, wood.height);
            needs.add(new WoodNeed(WoodCategory.PLANK, wood.height, plankCount));
        }
        return needs;
    }

    public static void calculateOffer(ConnectionPool cp, Offer offer, int widthMm, int lengthMm, int heightMm, int shedWidthMm, int shedLengthMm) throws CarportCalculationException, DBException {
        offer.width = widthMm;
        offer.length = lengthMm;
        offer.height = heightMm;
        offer.shedWidth = shedWidthMm;
        offer.shedLength = shedLengthMm;
        List<WoodNeed> needs = calculateNeedsWithShed(
                cp,
                lengthMm,
                widthMm,
                heightMm,
                shedWidthMm,
                shedLengthMm
        );

        List<Bill> bills = new ArrayList<>();
        offer.price = 0;

            for (WoodNeed need : needs) {
                Wood wood;
                wood = WoodMapper.getWood(cp, need.type, need.requiredLengthMm);
                if (wood == null) {
                    throw new CarportCalculationException("No wood found for " + need.type + " length " + need.requiredLengthMm);
                }
                double pricePerMeter = 0;
                double lengthMeters = 0;
                Bill bill = null;
                pricePerMeter = wood.pricePerMeter;
                lengthMeters = wood.length / 1000.0;
                double linePrice = pricePerMeter * lengthMeters * need.count;
                bill = new Bill(
                        offer.id,
                        wood.id,
                        need.count,
                        "helptext.todo",
                        linePrice
                );

                BillMapper.insert(cp, bill);
                bills.add(bill);
                offer.price += bill.price;
            }
            OfferMapper.updateOffer(cp, offer);
            BillMapper.deleteOfferBills(cp, offer.id);
            BillMapper.addBills(cp, bills);
        }
    }


