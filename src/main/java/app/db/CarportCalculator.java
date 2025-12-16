package app.db;

import app.exceptions.CarportCalculationException;
import app.exceptions.DBException;

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
        if (boardLengthMm >= lengthMm) {
            return Math.max(((int) Math.ceil((double) lengthMm / boardLengthMm)), CarportRules.MIN_BOARDS);
        } else {
            return Math.max(((int) Math.ceil((double) lengthMm / boardLengthMm) * 2), CarportRules.MIN_BOARDS);
        }
    }

    // at least 2 boards for the length of the rem because there is two width-sides
    public static int calcNumberOfBeamsCoverWidth(int widthMm, int boardWidthMm) {
        if (boardWidthMm >= widthMm) {
            return Math.max(((int) Math.ceil((double) widthMm / boardWidthMm)), CarportRules.MIN_BOARDS);
        } else{
            return Math.max(((int) Math.ceil((double) widthMm / boardWidthMm) * 2), CarportRules.MIN_BOARDS);
        }
    }
    public static int calcNumberOfPlanksForShed(int shedLengthMm, int shedWidthMm, int plankLengthMm) {
        int planksForLength = (int) Math.ceil((double) shedLengthMm / plankLengthMm) * 2;
        int planksForWidth = (int) Math.ceil((double) shedWidthMm / plankLengthMm) * 2;
        return Math.max(planksForLength + planksForWidth, CarportRules.MIN_PLANKS_IF_SHED);
    }

    // calculate bills/material list from an offer
    public static List<Bill> calcBills(ConnectionPool cp, Offer offer)
            throws DBException, CarportCalculationException
    {
        int cnt;
        Wood wood;
        double price;
        List<Bill> bills = new ArrayList<>();

        // rafters
        cnt = calcNumberOfRafters(offer.length);
        wood = WoodMapper.getWood(cp, WoodCategory.RAFTER, offer.width);
        if (wood == null)
            throw new CarportCalculationException("rafters");
        price = (float)wood.pricePerMeter*(wood.length/1000.0);
        bills.add(new Bill(offer.id, wood.id, cnt, "helptext.rafters", cnt*price));

        // beams
        wood = WoodMapper.getWood(cp, WoodCategory.RAFTER, offer.length);

        if (wood == null)
            throw new CarportCalculationException("beams");
        cnt = calcNumberOfBeamsCoverWidth(offer.width,wood.length) + calcNumberOfBeamsForRemLength(offer.length,wood.length);
        price = (float)wood.pricePerMeter*(wood.length/1000.0);
        bills.add(new Bill(offer.id, wood.id, cnt, "helptext.beams", cnt*price));

        // pillars
        cnt = calcNumberOfPillars(offer.length, offer.shedLength);
        cnt += calcShedPillarCnt(offer.shedLength);
        int innerWidth = offer.width-CarportRules.BEAMS_OFFS*2;
        cnt += calcShedWidthPillarCnt(innerWidth, offer.shedWidth);
        cnt += calcShedLengthPillarCnt(innerWidth, offer.shedWidth, offer.shedLength);
        wood = WoodMapper.getWood(cp, WoodCategory.PILLAR, offer.height+CarportRules.PILLAR_EXTRA_MM);
        if (wood == null)
            throw new CarportCalculationException("pillars");
        price = (float)wood.pricePerMeter*(wood.length/1000.0);
        bills.add(new Bill(offer.id, wood.id, cnt, "helptext.pillars", cnt*price));

        // planks
        wood = WoodMapper.getWood(cp, WoodCategory.PLANK, 0);
        if (wood == null)
            throw new CarportCalculationException("planks");
        cnt = calcNumberOfPlanksForShed(offer.shedLength, offer.shedWidth, wood.length);
        price = (float)wood.pricePerMeter*(wood.length/1000.0);
        bills.add(new Bill(offer.id, wood.id, cnt, "helptext.shedplanks", cnt*price));

        return bills;
    }
    }


