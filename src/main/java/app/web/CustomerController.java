package app.web;

import app.db.*;
import app.exceptions.DBException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CustomerController{

    private static Logger logger = Logger.getLogger("web");

    public static void addRoutes(Javalin app)
    {
        app.get(Path.Web.INDEX, CustomerController::serveIndexPage);
        app.post(Path.Web.SEND_REQUEST, CustomerController::handleFormPost);
        app.post(Path.Web.HANDLE_MESSAGE, CustomerController::handleMessage);
        app.get(Path.Web.USER_OFFERS, CustomerController::serveOffersPage);

        app.get(Path.Web.ORDER_CONFIRM, CustomerController::serveOrderConfirmation);
    }

    public static void serveIndexPage(Context ctx) {
        try {
            User user = ctx.sessionAttribute("user");

            List<Wood> beams =
                    WoodMapper.getWoodsByCategory(Server.connectionPool, WoodCategory.RAFTER);

            int[] lengths = beams.stream().mapToInt(w -> w.length).distinct().sorted().toArray();
            int[] widths  = beams.stream().mapToInt(w -> w.length).distinct().sorted().toArray();
            int[] shedLengths = new int[]{1000, 2000, 3000, 4000};

            ctx.attribute("widths", widths);
            ctx.attribute("lengths", lengths);
            ctx.attribute("shedLengths", shedLengths);
            ctx.attribute("shedWidths", widths);
            ctx.attribute("user", user);
            ctx.attribute("successTxt", ctx.sessionAttribute("successTxt"));
            ctx.attribute("errmsg", ctx.sessionAttribute("errmsg"));
            ctx.sessionAttribute("errmsg", null);
            ctx.sessionAttribute("successTxt", null);
            ctx.render(Path.Template.INDEX);

        } catch (DBException e) {
            ctx.sessionAttribute("errmsg", "* Træ fra database kunne ikke hentes.");
            ctx.render(Path.Template.INDEX);
        }
    }

    public static void handleFormPost(Context ctx) {
        Offer offer;
        User user = ctx.sessionAttribute("user");
        if (user == null) {
            ctx.sessionAttribute("errmsg", "Du skal være logget ind for at sende en forespørgsel.");
            ctx.redirect(Path.Web.INDEX);
            return;
        }
        try {
            int carportWidth = Integer.parseInt(ctx.formParam("carportWidth"));
            int carportLength = Integer.parseInt(ctx.formParam("carportLength"));
            int carportShedWidth = Integer.parseInt(ctx.formParam("carportShedWidth"));
            int carportShedLength = Integer.parseInt(ctx.formParam("carportShedLength"));
            String address = ctx.formParam("address");
            int postalcode = Integer.parseInt(ctx.formParam("postalCodes"));
            int height = 2215; // default height
            String text = ctx.formParam("text");
            if (text == null)
                text = "t";
            if (text.length() >= 1024)
                text = text.substring(0, 1024);
            int customerId = user.id;


            //dummy data insert for testing customerPage
            int salespersonId = 1;
            String city = "København";
            double price = 0.0;

            offer = new Offer(0, customerId, salespersonId, address, postalcode, city, carportWidth, height, carportLength, carportShedWidth, carportShedLength, price, text, OfferStatus.SALESPERSON);
            OfferMapper.addQuery(Server.connectionPool, offer);

            ctx.sessionAttribute("successTxt", "* Din forespørgsel er sendt!");
            ctx.redirect(Path.Web.INDEX);
        } catch (DBException e) {
            logger.log(Level.SEVERE, e.getStackTrace()[0]+": "+e.getMessage());
            ctx.sessionAttribute("errmsg", "* Din forespørgsel kunne ikke sendes!");
            ctx.redirect(Path.Web.INDEX);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getStackTrace()[0]+": "+e.getMessage());
            ctx.sessionAttribute("errmsg", "* Din forespørgsel kunne ikke sendes!");
            ctx.redirect(Path.Web.INDEX);
        }
    }

    public static void serveOffersPage(Context ctx)
    {
        try {
            User user = ctx.sessionAttribute("user");
            if (user == null) {
                ctx.sessionAttribute("loginredirect", Path.Web.USER_OFFERS);
                ctx.redirect(Path.Web.LOGIN);
                return;
            }

            List<Offer> offers = OfferMapper.getCustomerOffers(Server.connectionPool, user.id);

            ctx.attribute("user", user);
            ctx.attribute("offers", offers);
            ctx.render(Path.Template.USER_OFFERS);
        } catch (DBException e) {
            logger.log(Level.SEVERE, e.getStackTrace()[0]+": "+e.getMessage());
            ctx.sessionAttribute("errmsg", "* Dine tilbud kunne ikke hentes.");
            ctx.render(Path.Template.USER_OFFERS);
        }
    }

    public static void handleMessage(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        String comment = ctx.formParam("comment");
        String radio = ctx.formParam("decision");
        User user = ctx.sessionAttribute("user");

        if (user == null) {
            ctx.status(HttpStatus.FORBIDDEN);
            return;
        }

        try {
            if (comment == null || radio == null) {
                ctx.status(HttpStatus.BAD_REQUEST);
                return;
            }

            Offer offer = OfferMapper.getOffer(Server.connectionPool, id);
            if (offer == null || offer.customerId != user.id) {
                ctx.status(404);
                return;
            }
            offer.text = comment;
            if (radio.equals("ACCEPTED")) {
                offer.status = OfferStatus.ACCEPTED;
                OfferMapper.updateOffer(Server.connectionPool, offer);
                ctx.attribute("offer", offer);
                ctx.render(Path.Template.PAYMENT);
                return;
            } else if (radio.equals("REJECTED")) {
                offer.status = OfferStatus.SALESPERSON;
                OfferMapper.updateOffer(Server.connectionPool, offer);
                ctx.redirect(Path.Web.USER_OFFERS);
                return;
            }
            ctx.status(HttpStatus.BAD_REQUEST);

        } catch (DBException e) {
            logger.log(Level.SEVERE, e.getStackTrace()[0]+": "+e.getMessage());
            ctx.status(500);
        }
    }

    public static void serveOrderConfirmation(Context ctx) {
        int offerId = Integer.parseInt(ctx.pathParam("id"));
        User user;

        try {
            user = ctx.sessionAttribute("user");
            if (user == null) {
                ctx.redirect(Path.Web.LOGIN);
                return;
            }
            // 1. find offer
            Offer offer = OfferMapper.getOffer(Server.connectionPool, offerId);
            if (offer == null || offer.status != OfferStatus.ACCEPTED || offer.customerId != user.id) {
                ctx.status(404);
                return;
            }

            // 2. update status to ORDERED
            offer.status = OfferStatus.ORDERED;
            OfferMapper.updateOffer(Server.connectionPool, offer);

            // 3. hent styklisten
            List<Bill> bills = BillMapper.getBillsByOfferId(Server.connectionPool, offerId);

            // 4. send data til kvitterings.html
            ctx.attribute("offer", offer);
            ctx.attribute("bills", bills);

            ctx.render(Path.Template.ORDER_RECEIPT);

        } catch (DBException e) {
            logger.log(Level.SEVERE, e.getStackTrace()[0]+": "+e.getMessage());
            ctx.status(500);
        }
    }

}




