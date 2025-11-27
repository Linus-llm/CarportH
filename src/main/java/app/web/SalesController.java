package app.web;

import app.db.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;

import java.sql.SQLException;
import java.util.List;

public class SalesController {

    public static void addRoutes(Javalin app) {


        app.before(Path.Web.SALES + "*", SalesController::before);
        app.get(Path.Web.SALES, SalesController::serveMainPage);
        app.get(Path.Web.SALES_NEW_OFFER, SalesController::serveNewOfferPage);
        app.post(Path.Web.SALES_CALC, SalesController::handleCalcPost);
        app.post(Path.Web.SALES_SEND_OFFER, SalesController::handleSendOfferPost);


        app.get("/bestil-carport", SalesController::serveCarportForm);
        app.post("/send-request", SalesController::handleRequestPost);

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
            offers = OfferMapper.getSalespersonOffers(Server.connectionPool, user.id);
            ctx.attribute("offers", offers);
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
                if (offer == null || offer.status != OfferStatus.SALESPERSON) {
                    ctx.status(404);
                    return;
                }
                ctx.sessionAttribute("offer", offer);
            }
            ctx.attribute("offer", offer);
            String defaultTab = ctx.sessionAttribute("defaultTab");
            if (defaultTab == null)
                defaultTab = "tab-dimensions";
            ctx.attribute("defaultTab", defaultTab);
            ctx.sessionAttribute("defaultTab", "tab-dimensions");
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
        if (w == null || l == null || h == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return;
        }
        offer.width = Integer.parseInt(w);
        offer.length = Integer.parseInt(l);
        offer.height = Integer.parseInt(h);

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
            offer.status = OfferStatus.CUSTOMER;
            OfferMapper.updateOffer(Server.connectionPool, offer);
        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            return;
        }
        ctx.redirect(Path.Web.SALES);
    }


    //         INDSAT FRA RequestController


    public static void serveCarportForm(Context ctx) {
        ctx.html(
                "<h1>Bestil Carport</h1>" +
                        "<form action='/send-request' method='post'>" +
                        "Bredde: <input type='text' name='carportWidth'><br>" +
                        "Længde: <input type='text' name='carportLength'><br>" +
                        "Tag (0=ingen, 1=plast): <input type='text' name='carportRoof'><br>" +
                        "Skur bredde: <input type='text' name='carportShedWidth'><br>" +
                        "Skur længde: <input type='text' name='carportShedLength'><br>" +
                        "<button type='submit'>Send forespørgsel</button>" +
                        "</form>"
        );
    }

    public static void handleRequestPost(Context ctx) {
        String width = ctx.formParam("carportWidth");
        String length = ctx.formParam("carportLength");
        String roof = ctx.formParam("carportRoof");
        String shedWidth = ctx.formParam("carportShedWidth");
        String shedLength = ctx.formParam("carportShedLength");

        System.out.println("Ny carport forespørgsel modtaget:");
        System.out.println("Bredde: " + width);
        System.out.println("Længde: " + length);
        System.out.println("Tag: " + roof);
        System.out.println("Skur bredde: " + shedWidth);
        System.out.println("Skur længde: " + shedLength);

        ctx.html("<h2>Tak for din forespørgsel!</h2><p>Vi vender tilbage snarest.</p>");
    }
}