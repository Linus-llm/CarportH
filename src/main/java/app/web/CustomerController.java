package app.web;

import app.db.*;
import app.exceptions.DBException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.List;

public class CustomerController{

    public static void addRoutes(Javalin app)
    {
        app.get(Path.Web.INDEX, CustomerController::serveIndexPage);
        app.post(Path.Web.SEND_REQUEST, CustomerController::handleFormPost);
        app.post("/offers/{id}/message", CustomerController::handleMessage);
        app.get(Path.Web.USER_OFFERS, CustomerController::serveOffersPage);

        app.get("/orderConfirmation", CustomerController::serveOrderConfirmation);
    }

    public static void serveIndexPage(Context ctx) throws DBException {
        User user = ctx.sessionAttribute("user");


        // TODO: get possible dimensions from DB
        List<Wood> beams = WoodMapper.getWoodsByCategory(Server.connectionPool, WoodCategory.RAFTER);

        int[] lengths = beams.stream().mapToInt(w -> w.length).distinct().sorted().toArray();
        int[] widths = beams.stream().mapToInt(w -> w.length).distinct().sorted().toArray();
        ctx.attribute("widths", widths);
        ctx.attribute("lengths", lengths);
        int[] shedLengths = {1000, 1400, 1800, 2200, 2600, 3000, 3400, 3800, 4200, 4600, 5000};
        ctx.attribute("shedLengths", shedLengths);
        int[] shedWidths = widths;
        ctx.attribute("shedWidths", shedWidths);
        ctx.attribute("user", user);
        ctx.attribute("errmsg", ctx.sessionAttribute("errmsg"));
        ctx.attribute("successTxt", ctx.sessionAttribute("successTxt"));
        ctx.render(Path.Template.INDEX);
        ctx.sessionAttribute("errmsg", null);
        ctx.sessionAttribute("successTxt", null);
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
            if (!OfferMapper.addQuery(Server.connectionPool, offer))
                throw new Exception("addQuery failed");

            ctx.sessionAttribute("successTxt", "* Din forespørgsel er sendt!");
            ctx.redirect(Path.Web.INDEX);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
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
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

    public static void handleMessage(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        String comment = ctx.formParam("comment");
        String radio = ctx.formParam("decision");

        try {
            if (comment == null || radio == null) {
                ctx.status(HttpStatus.BAD_REQUEST);
                return;
            }

            Offer offer = OfferMapper.getOffer(Server.connectionPool, id);
            if (offer == null) {
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

        } catch (Exception e) {
            System.out.println("ERROR (handleMessage): " + e.getMessage());
            ctx.status(500);
        }
    }
    public static void serveOrderConfirmation(Context ctx) {
        int offerId = Integer.parseInt(ctx.queryParam("offerId"));
        User user;

        try {
            user = ctx.sessionAttribute("user");
            // 1. hent offer
            Offer offer = OfferMapper.getOffer(Server.connectionPool, offerId);
            if (offer == null || offer.status != OfferStatus.ORDERED || offer.customerId != user.id) {
                ctx.status(404);
                return;
            }

            // 3. hent styklisten
            List<Bill> bills = BillMapper.getBillsByOfferId(Server.connectionPool, offerId);

            // 4. send data til kvitterings.html
            ctx.attribute("offer", offer);
            ctx.attribute("bills", bills);

            ctx.render("orderReceipt.html");

        } catch (Exception e) {
            System.out.println("ERROR (serveOrderConfirmation): " + e.getMessage());
            ctx.status(500);
        }
    }

}




