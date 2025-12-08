package app.web;

import app.db.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.sql.SQLException;
import java.util.List;

public class CustomerController{

    public static void addRoutes(Javalin app)
    {
        app.get(Path.Web.INDEX, CustomerController::serveIndexPage);
        app.post(Path.Web.SEND_REQUEST, CustomerController::handleFormPost);

//      app.get(Path.Web.USER_OFFERS, CustomerController::serveUserOffersPage);
        app.post(Path.Web.ACCEPT_OFFER, CustomerController::handleAccept);
        app.post("/offers/{id}/reject", CustomerController::handleReject);
        app.post("/offers/{id}/message", CustomerController::handleMessage);
        app.get(Path.Web.USER_OFFERS, CustomerController::serveOffersPage);

        app.get("/orderConfirmation", ctx -> ctx.render("orderConfirmation.html"));
    }

    public static void serveIndexPage(Context ctx) throws SQLException {
        User user = ctx.sessionAttribute("user");


        // TODO: get possible dimensions from DB
        List<Wood> beams = WoodMapper.getWoodsByCategory(Server.connectionPool, WoodCategory.BEAM);

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
        //TODO tilføj check af user ikke er null

        Offer offer;
        User user = ctx.sessionAttribute("user");
        if (user == null) { // FIXME: for debugging
            user = new User(1, "ole", "ole@customer.dk", UserRole.SALESPERSON);
            ctx.sessionAttribute("user", user);
        }
        try {
            int carportWidth = Integer.parseInt(ctx.formParam("carportWidth"));
            int carportLength = Integer.parseInt(ctx.formParam("carportLength"));
            int carportShedWidth = Integer.parseInt(ctx.formParam("carportShedWidth"));
            int carportShedLength = Integer.parseInt(ctx.formParam("carportShedLength"));
            String adress = "address";
            int postalcode = 4242;
            int height = 2215; // default height
            String text = ctx.formParam("text");
            if (text == null)
                text = "";
            if (text.length() >= 1024)
                text = text.substring(0, 1024);
            int customerId = user.id;


            //dummy data insert for testing customerPage
            int salespersonId = 1;
            String city = "København";
            double price = 0.0;

            offer = new Offer(0, customerId, salespersonId, adress, postalcode, city, carportWidth, height, carportLength, carportShedWidth, carportShedLength, price, text, OfferStatus.SALESPERSON);
            if (!OfferMapper.addQuery(Server.connectionPool, offer))
                throw new Exception("addQuery failed");
            //tester
            ctx.sessionAttribute("successTxt", "* Din forespørgsel er sendt!");
            ctx.redirect(Path.Web.INDEX);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            ctx.sessionAttribute("errmsg", "* Din forespørgsel kunne ikke sendes!");
            ctx.redirect(Path.Web.INDEX);
        }
    }

//    public static void serveUserOffersPage(Context ctx)
//    {
//        try {
//            User user = ctx.sessionAttribute("user");
//            if (user == null) {
//                ctx.sessionAttribute("loginredirect", Path.Web.USER_OFFERS);
//                ctx.redirect(Path.Web.LOGIN);
//                return;
//            }
//
//            List<Offer> offers = OfferMapper.getCustomerOffers(Server.connectionPool, user.id);
//
//            ctx.attribute("user", user);
//            ctx.attribute("offers", offers);
//            ctx.render(Path.Template.USER_OFFERS);
//        } catch (Exception e) {
//            System.out.println("ERROR: " + e.getMessage());
//            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
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
                System.out.println("User id: " + user.id + ", offers.size() = " + offers.size());

                ctx.attribute("user", user);
                ctx.attribute("offers", offers);
                ctx.render(Path.Template.USER_OFFERS);
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

    public static void handleAccept(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));

        try {
            Offer offer = OfferMapper.getOffer(Server.connectionPool, id);
            if (offer == null) {
                ctx.status(404);
                return;
            }

            offer.status = OfferStatus.ACCEPTED;

            OfferMapper.updateOffer(Server.connectionPool, offer);

            servePaymentPage(ctx);

        } catch (Exception e) {
            System.out.println("ERROR (handleAccept): " + e.getMessage());
            ctx.status(500);
        }
    }

    public static void servePaymentPage(Context ctx) {
        ctx.attribute("errmsg", ctx.sessionAttribute("errmsg"));
        ctx.render(Path.Template.PAYMENT);
        ctx.sessionAttribute("errmsg", null);
    }

    public static void handleReject(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));

        try {
            Offer offer = OfferMapper.getOffer(Server.connectionPool, id);
            if (offer == null) {
                ctx.status(404);
                return;
            }

            offer.status = OfferStatus.SALESPERSON;

            OfferMapper.updateOffer(Server.connectionPool, offer);

            ctx.redirect(Path.Web.USER_OFFERS);

        } catch (Exception e) {
            System.out.println("ERROR (handleReject): " + e.getMessage());
            ctx.status(500);
        }
    }

    public static void handleMessage(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        String comment = ctx.formParam("comment");

        try {
            Offer offer = OfferMapper.getOffer(Server.connectionPool, id);
            if (offer == null) {
                ctx.status(404);
                return;
            }

            // 1) Læg kommentaren et sted i Offer-objektet
            // LIGE NU bruger vi "text"-feltet, da det allerede findes i DB
            offer.text = comment;

            // 2) Gem i databasen
            OfferMapper.updateOffer(Server.connectionPool, offer);

            // 3) Redirect tilbage til oversigten
            ctx.redirect(Path.Web.USER_OFFERS);

        } catch (Exception e) {
            System.out.println("ERROR (handleMessage): " + e.getMessage());
            ctx.status(500);
        }
    }

}




