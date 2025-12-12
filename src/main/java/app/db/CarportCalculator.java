package app.db;

import app.exceptions.CarportCalculationException;
import app.exceptions.DBException;
import java.util.ArrayList;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.List;

public class CarportCalculator {


    // returns non-shed sides pillar count
    public static int calcNumberOfPillars(int length, int shedLength) {
        int cnt;

        if (shedLength > 0) {
            if (length-shedLength < CarportRules.MAX_PILLAR_SPACING_MM/2)
                return 0;
            if (length-shedLength <= CarportRules.MAX_PILLAR_SPACING_MM)
                return 2;
        } else {
            // adjust for end pillar
            length += CarportRules.MAX_PILLAR_SPACING_MM/2;
        }

        cnt = (length / CarportRules.MAX_PILLAR_SPACING_MM + 1) * 2;
        if (cnt < CarportRules.MIN_PILLARS)
            cnt = CarportRules.MIN_PILLARS;

        return cnt;
    }

    // returns shed sides pillar count
    public static int calcShedPillarCnt(int shedLength)
    {
        if (shedLength > 0)
            return (int)Math.ceil((double)shedLength/CarportRules.MAX_PILLAR_SPACING_MM+1)*2;
        return 0;
    }

    // returns side pillars positions
    public static int[] calcPillarsOffs(int length, int shedLength)
    {
        int[] offs;
        int i, dist;
        int cnt, shedCnt;
        int idx = 0;

        shedCnt = calcShedPillarCnt(shedLength)/2;
        cnt = calcNumberOfPillars(length, shedLength)/2;
        offs = new int[cnt+shedCnt];
        offs[0] = CarportRules.PILLARS_OFFS;
        if (shedCnt > 1) {
            dist = (shedLength - CarportRules.PILLARS_OFFS) / (shedCnt-1);
            if (dist > CarportRules.MAX_PILLAR_SPACING_MM)
                dist = CarportRules.MAX_PILLAR_SPACING_MM;
            for (i = 1; i < shedCnt; i++)
                offs[i] = offs[0]+(i * dist);
            offs[shedCnt-1] = shedLength;
            idx = shedCnt-1;
        }
        if (cnt > 0) {
            dist = (length - shedLength) / (shedLength > 0 ? cnt : cnt-1);
            if (dist > CarportRules.MAX_PILLAR_SPACING_MM)
                dist = CarportRules.MAX_PILLAR_SPACING_MM;
            for (i = 1; i < offs.length - idx; i++)
                offs[i + idx] = (shedLength)+(i * dist);
        }

        if (offs[offs.length-1] > length-CarportRules.PILLARS_OFFS)
            offs[offs.length-1] = length-CarportRules.PILLARS_OFFS;

        // TODO: move this to a test
        if (offs[0] > CarportRules.MAX_PILLAR_SPACING_MM/2)
            System.out.println("ERROR: length-offs[0] > MAX_PILLAR_SPACING_MM/2");
        for (i = 1; i < offs.length; i++) {
            if (offs[i]-offs[i-1] > CarportRules.MAX_PILLAR_SPACING_MM) {
                System.out.println("ERROR: offs[" + i + "]-offs[" + (i - 1) + "] > MAX_PILLAR_SPACING_MM");
            }
        }
        if (length-offs[offs.length-1] > CarportRules.MAX_PILLAR_SPACING_MM/2)
            System.out.println("ERROR: length-offs[cnt-1] > MAX_PILLAR_SPACING_MM/2");

        return offs;
    }

    // returns middle pillar count along width
    public static int calcShedWidthPillarCnt(int width, int shedWidth)
    {
        int cnt;

        if (shedWidth <= 0)
            return 0;

        if (shedWidth < width)
            shedWidth -= CarportRules.BEAMS_OFFS;
        else
            shedWidth = width;
        cnt = (shedWidth/CarportRules.MAX_PILLAR_SPACING_MM);
        if (shedWidth < width) {
            // include corner
            cnt += 1;
        }
        return cnt*2;
    }

    // returns middle pillars along width
    public static int[] calcShedWidthPillarsOffs(int width, int shedWidth)
    {
        int[] offs;
        int i, dist, cnt;

        cnt = calcShedWidthPillarCnt(width-CarportRules.BEAMS_OFFS*2, shedWidth)/2;
        offs = new int[cnt];

        dist = shedWidth/(cnt+1);
        for (i = 0; i < cnt; i++)
            offs[i] = CarportRules.BEAMS_OFFS+(i+1) * dist;
        if (cnt >= 1 && shedWidth < width)
            offs[cnt-1] = shedWidth;

        return offs;
    }

    // returns middle pillar count along length
    // NOTE: only one row
    public static int calcShedLengthPillarCnt(int width, int shedWidth, int shedLength)
    {
        if (shedLength <= 0)
            return 0;

        shedLength -= CarportRules.PILLARS_OFFS;
        if (shedWidth < width)
            return (shedLength/CarportRules.MAX_PILLAR_SPACING_MM);
        return 0;
    }

    // returns middle pillars along length
    public static int[] calcShedLengthPillarsOffs(int width, int shedWidth, int shedLength)
    {
        int[] offs;
        int i, dist, cnt;

        width -= CarportRules.BEAMS_OFFS*2;
        cnt = calcShedLengthPillarCnt(width, shedWidth, shedLength);
        offs = new int[cnt];

        dist = shedLength/(cnt+1);
        for (i = 0; i < cnt; i++)
            offs[i] = CarportRules.PILLARS_OFFS+(i+1) * dist;

        return offs;
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

    // calculate bills/material list from an offer
    public static List<Bill> calcBills(ConnectionPool cp, Offer offer)
            throws SQLException
    {
        int cnt;
        Wood wood;
        double price;
        List<Bill> bills = new ArrayList<>();

        // rafters
        cnt = calcNumberOfRafters(offer.length);
        wood = WoodMapper.getWood(cp, WoodCategory.RAFTER, offer.width);
        if (wood == null)
            return null;
        price = (float)wood.pricePerMeter*(wood.length/1000.0);
        bills.add(new Bill(offer.id, wood.id, cnt, "helptext.rafters", cnt*price));

        // beams
        cnt = 2;
        wood = WoodMapper.getWood(cp, WoodCategory.RAFTER, offer.length);
        if (wood == null)
            return null;
        price = (float)wood.pricePerMeter*(wood.length/1000.0);
        bills.add(new Bill(offer.id, wood.id, cnt, "helptext.beams", cnt*price));

        // pillars
        cnt = calcNumberOfPillars(offer.length, offer.shedLength);
        cnt += calcShedPillarCnt(offer.shedLength);
        int innerWidth = offer.width-CarportRules.BEAMS_OFFS*2;
        cnt += calcShedWidthPillarCnt(innerWidth, offer.shedWidth);
        cnt += calcShedLengthPillarCnt(innerWidth, offer.shedWidth, offer.shedLength);
        wood = WoodMapper.getWood(cp, WoodCategory.PILLAR, offer.height);
        if (wood == null)
            return null;
        price = (float)wood.pricePerMeter*(wood.length/1000.0);
        bills.add(new Bill(offer.id, wood.id, cnt, "helptext.pillars", cnt*price));

        // planks
        wood = WoodMapper.getWood(cp, WoodCategory.PLANK, 0);
        if (wood == null)
            return null;
        cnt = calcNumberOfPlanksForShed(offer.shedLength, offer.shedWidth, wood.length);
        price = (float)wood.pricePerMeter*(wood.length/1000.0);
        bills.add(new Bill(offer.id, wood.id, cnt, "helptext.shedplanks", cnt*price));

        return bills;
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
        needs.add(new WoodNeed(WoodCategory.RAFTER, widthMm, beamCountWidth));
        //length

        wood = WoodMapper.getWood(cp, WoodCategory.BEAM, lengthMm);

        if (wood == null) {
            throw new CarportCalculationException("No wood found for BEAM length " + lengthMm);
        }
        int beamCountLength = calcNumberOfBeamsForRemLength(widthMm, wood.length);
        needs.add(new WoodNeed(WoodCategory.RAFTER, widthMm, beamCountLength));

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


