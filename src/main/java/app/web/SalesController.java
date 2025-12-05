package app.web;

import app.db.*;
import app.draw.CarportSVG;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalesController {

    public static void addRoutes(Javalin app) {
        app.before(Path.Web.SALES + "*", SalesController::before);
        app.get(Path.Web.SALES, SalesController::serveMainPage);
        app.get(Path.Web.SALES_NEW_OFFER, SalesController::serveNewOfferPage);
        app.post(Path.Web.SALES_CALC, SalesController::handleCalcPost);
        app.post(Path.Web.SALES_SEND_OFFER, SalesController::handleSendOfferPost);
        app.post("/sales/claim-offer/{id}", SalesController::handleClaimOffer);
    }

    public static void before(Context ctx) {
        User user = ctx.sessionAttribute("user");
        if (user == null) { // FIXME: for debugging
            user = new User(2, "bob", "bob@salesperson.dk", UserRole.SALESPERSON);
            ctx.sessionAttribute("user", user);
            return;
        }
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
            System.out.println("ERROR: " + e.getMessage());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static void serveNewOfferPage(Context ctx) {
        try {
            int offerId = Integer.parseInt(ctx.pathParam("id"));
            Offer offer = ctx.sessionAttribute("offer");
            if (offer == null || offer.id != offerId) {
                offer = OfferMapper.getOffer(Server.connectionPool, offerId);
                ctx.sessionAttribute("offer", offer);
            }
            if (offer == null) {
                ctx.status(404);
                return;
            }

            List<Bill> bills = BillMapper.getBillsByOfferId(Server.connectionPool, offerId);
            ctx.attribute("bills", bills);

            User customer = UserMapper.getUser(Server.connectionPool, offer.customerId);
            ctx.attribute("customer", customer);

            ctx.attribute("offer", offer);

            String defaultTab = ctx.sessionAttribute("defaultTab");
            if (defaultTab == null)
                defaultTab = "tab-dimensions";
            ctx.attribute("defaultTab", defaultTab);
            ctx.sessionAttribute("defaultTab", "tab-dimensions");

            // FIXME: use values from carport calculator
            CarportSVG svg = new CarportSVG(600, 600, offer.width, offer.length);
            svg.drawStraps(45, offer.length);
            svg.drawRafters(45, 15, offer.length/14);
            svg.drawPillars(97, 97, new int[]{1000, offer.length/2, offer.length-300});
            ctx.attribute("svg", svg.toString());

            ctx.render(Path.Template.SALES_NEW_OFFER);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static void handleCalcPost(Context ctx) {
        String path;
        int idx;
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

        // we calculate the needed wood pieces based on carport dimensions
        CarportCalculator calculator = new CarportCalculator();
        List<WoodNeed> needs = calculator.calculateNeedsWithShed(Server.connectionPool, offer.length, offer.width, offer.height, offer.shedWidth, offer.shedLength );

        // we create bills list
        List<Bill> bills = new ArrayList<>();

        try {
            // we loop through each needed wood piece
            for (WoodNeed need : needs) {

                Wood wood = null;
                // thus we find a suitable wood piece from the DB with getWood
                try {
                    wood = WoodMapper.getWood(Server.connectionPool, need.type, need.requiredLengthMm);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                if (wood == null) {
                    // TODO: no suitable wood found, handle this case
                    continue;
                }

                // now we have found our wood piece
                // now we need to calculate the price for this line item
                double pricePerMeter = wood.pricePerMeter;
                //to get the length in meters we divide by 1000
                double lengthMeters = wood.length / 1000.0;
                double linePrice = pricePerMeter * lengthMeters * need.count;
                // we then create a bill object for this line item
                System.out.println("Creating bill for offerId = " + offer.id);
                Bill bill = new Bill(
                        offer.id,
                        wood.id,
                        need.count,
                        linePrice
                );
                System.out.println("Bill.offerId = " + bill.offerId);
                //we call the insert to save it to the DB
                BillMapper.insert(Server.connectionPool, bill);

                // then we add the line to the created bills list
                bills.add(bill);
            }
            // we calculate the total price of the bill by summing up each line price
            double total = bills.stream().mapToDouble(b -> b.price).sum();
            //we set the offer price to the total of the bill list
            offer.price = total;
            //then we update the offer in the DB
            OfferMapper.updatePrice(Server.connectionPool, offer.id, total);

        } catch(Exception e){
                System.out.println("ERROR: " + e.getMessage());
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            }


            path = Path.Web.SALES_NEW_OFFER;
            idx = path.indexOf('{');
            path = path.substring(0, idx);
            path += offer.id;
            ctx.sessionAttribute("defaultTab", "tab-matlist");
            ctx.redirect(path);

    }

    public static void handleSendOfferPost(Context ctx) {
        Offer offer;

        offer = ctx.sessionAttribute("offer");
        if (offer == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return;
        }
        try {
            offer.text = ctx.formParam("text");
            offer.status = OfferStatus.CUSTOMER;
            OfferMapper.updateOffer(Server.connectionPool, offer);
        } catch (SQLException e) {
            System.out.println("ERROR: "+e.getMessage());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            return;
        }
        ctx.redirect(Path.Web.SALES);
    }
    public static void handleClaimOffer(Context ctx) {
        User user = ctx.sessionAttribute("user");
        if (user == null || user.role != UserRole.SALESPERSON) {
            ctx.status(403);
            return;
        }

        int offerId = Integer.parseInt(ctx.pathParam("id"));
        try {
            OfferMapper.assignSalesperson(Server.connectionPool, offerId, user.id);
            ctx.redirect(Path.Web.SALES);
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500);
        }
    }





}