package app.web;

import app.db.*;
import app.draw.CarportSVG;
import app.exceptions.CarportCalculationException;
import app.exceptions.DBException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class SalesController {

    private static Logger logger = Logger.getLogger("web");

    public static void addRoutes(Javalin app) {
        app.before(Path.Web.SALES + "*", SalesController::before);
        app.get(Path.Web.SALES, SalesController::serveMainPage);
        app.get(Path.Web.SALES_NEW_OFFER+"{id}", SalesController::serveNewOfferPage);
        app.post(Path.Web.SALES_CALC, SalesController::handleCalcPost);
        app.post(Path.Web.SALES_SEND_OFFER, SalesController::handleSendOfferPost);
        app.post(Path.Web.SALES_CLAIM_OFFER, SalesController::handleClaimOfferPost);
        app.post(Path.Web.SALES_SET_PRICE, SalesController::handleUpdatePrice);
    }

    public static void before(Context ctx) {
        User user = ctx.sessionAttribute("user");
        if (user == null || user.role != UserRole.SALESPERSON) {
            if (ctx.method() == HandlerType.GET) {
                ctx.sessionAttribute("loginredirect", ctx.path());
                ctx.redirect(Path.Web.LOGIN);
            } else {
                ctx.status(HttpStatus.FORBIDDEN);
            }
        }
    }

    public static void serveMainPage(Context ctx) {
        List<Offer> offers;
        User user;

        try {
            user = ctx.sessionAttribute("user");
            assert user != null;
            //har en sælger
            List<Offer> myOffers = OfferMapper.getSalespersonOffers(Server.connectionPool, user.id);
            //har ikk en sælger
            List<Offer> unassignedOffers = OfferMapper.getUnassignedOffers(Server.connectionPool);
            ctx.attribute("myOffers", myOffers);
            ctx.attribute("unassignedOffers", unassignedOffers);

            offers = OfferMapper.getSalespersonOffers(Server.connectionPool, user.id);
            ctx.attribute("offers", offers);
            ctx.attribute("user", user);

            ctx.render(Path.Template.SALES);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getStackTrace()[0]+": "+e.getMessage());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static void serveNewOfferPage(Context ctx) {
        try {
            int offerId = Integer.parseInt(ctx.pathParam("id"));
            Offer offer;
            offer = OfferMapper.getOffer(Server.connectionPool, offerId);
            if (offer == null) {
                ctx.status(404);
                return;
            }
            User seller = ctx.sessionAttribute("user");
            assert seller != null;
            if (offer.salespersonId != seller.id || offer.status != OfferStatus.SALESPERSON) {
                ctx.status(404);
                return;
            }

            // set as session attribute for form POST's
            ctx.sessionAttribute("offer", offer);

            List<Bill> bills = BillMapper.getBillsByOfferId(Server.connectionPool, offerId);
            ctx.attribute("bills", bills);
            double costprice = 0.0;
            for (Bill b : bills)
                costprice += b.price;

            User customer = UserMapper.getUser(Server.connectionPool, offer.customerId);
            ctx.attribute("customer", customer);

            ctx.attribute("offer", offer);
            ctx.attribute("costprice", costprice);

            String defaultTab = ctx.sessionAttribute("defaultTab");
            if (defaultTab == null)
                defaultTab = "tab-dimensions";
            ctx.attribute("defaultTab", defaultTab);
            ctx.sessionAttribute("defaultTab", "tab-dimensions");

            // create a simple svg sketch
            if (!bills.isEmpty()) {
                // NOTE: we don't know the real wood dimensions, because of DB design.
                CarportSVG svg = new CarportSVG(600, 600, offer.width, offer.length);
                svg.drawBeams(50, offer.length);
                svg.drawRafters(45, CarportCalculator.calcNumberOfRafters(offer.length));
                svg.drawPillars(100, 100, CarportCalculator.calcPillarsOffs(offer.length, offer.shedLength));
                int[] offsW = CarportCalculator.calcShedWidthPillarsOffs(
                        offer.width,
                        offer.shedWidth);
                int[] offsL = CarportCalculator.calcShedLengthPillarsOffs(offer.width, offer.shedWidth, offer.shedLength);
                svg.drawShedPillars(100, 100,
                        offer.shedWidth, offer.shedLength, offsW, offsL);
                svg.drawShedPlanks(offer.shedWidth, offer.shedLength);
                ctx.attribute("svg", svg.toString());
            }

            ctx.attribute("errmsg", ctx.sessionAttribute("errmsg"));
            ctx.render(Path.Template.SALES_NEW_OFFER);
            ctx.sessionAttribute("errmsg", null);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getStackTrace()[0]+": "+e.getMessage());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static void handleCalcPost(Context ctx) throws DBException, CarportCalculationException {
        Offer offer;


        offer = ctx.sessionAttribute("offer");
        if (offer == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return;
        }

        String w = ctx.formParam("width");
        String l = ctx.formParam("length");
        String h = ctx.formParam("height");
        String sw = ctx.formParam("shedWidth");
        String sl = ctx.formParam("shedLength");

        if (w == null || l == null || h == null || sw == null || sl == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return;
        }
        offer.width = (int) (Float.parseFloat(w) * 1000);
        offer.length = (int) (Float.parseFloat(l) * 1000);
        offer.height = (int) (Float.parseFloat(h) * 1000);
        offer.shedWidth = (int) (Float.parseFloat(sw) * 1000);
        offer.shedLength = (int) (Float.parseFloat(sl) * 1000);

        try {
            BillMapper.deleteOfferBills(Server.connectionPool, offer.id);
            List<Bill> bills = CarportCalculator.calcBills(Server.connectionPool, offer);
            if (bills == null)
                ctx.sessionAttribute("errmsg", "* kunne ikke beregne stykliste");
            else
                if (!BillMapper.addBills(Server.connectionPool, bills))
                    throw new SQLException("addBills returned false");

            if (!OfferMapper.updateOffer(Server.connectionPool, offer))
                throw new SQLException("updateOffer returned false");
        } catch (SQLException e) {
            System.out.println("ERROR: "+e.getStackTrace()[0]+": "+e.getMessage());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            return;
        }

        ctx.sessionAttribute("defaultTab", "tab-matlist");
        ctx.redirect(Path.Web.SALES_NEW_OFFER+offer.id);
    }

    public static void handleSendOfferPost(Context ctx) throws SQLException, DBException {
        Offer offer;

        offer = ctx.sessionAttribute("offer");
        if (offer == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return;
        }

        offer.text = ctx.formParam("text");
        String price = ctx.formParam("price");
        if(price != null && !price.isEmpty()){
            offer.price = (int) Double.parseDouble(price);
        }
        offer.status = OfferStatus.CUSTOMER;
        OfferMapper.updateOffer(Server.connectionPool, offer);

        ctx.redirect(Path.Web.SALES);
    }

    public static void handleClaimOfferPost(Context ctx) throws SQLException, DBException {
        User user = ctx.sessionAttribute("user");
        assert user != null;
        assert user.role == UserRole.SALESPERSON;

        int offerId = Integer.parseInt(ctx.pathParam("id"));

        OfferMapper.assignSalesperson(Server.connectionPool, offerId, user.id);
        ctx.redirect(Path.Web.SALES);

    }

    public static void handleUpdatePrice(Context ctx) throws SQLException, DBException {

        Offer offer = ctx.sessionAttribute("offer");
        String s = ctx.formParam("salesprice");
        if (offer == null || s == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return;
        }
        ctx.sessionAttribute("defaultTab", "tab-matlist");
        try {
            offer.price = Translator.parseCurrency(s);
            OfferMapper.updatePrice(Server.connectionPool, offer.id, offer.price);
        } catch (ParseException e) {
            ctx.sessionAttribute("errmsg", "* failed to set price");
            ctx.redirect(Path.Web.SALES_NEW_OFFER+offer.id);
            return;
        }
        ctx.redirect(Path.Web.SALES_NEW_OFFER+offer.id);
    }
}
