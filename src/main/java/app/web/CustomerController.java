package app.web;

import app.db.OfferMapper;
import app.db.Offer;
import app.db.OfferStatus;
import app.db.User;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.sql.SQLException;

public class CustomerController{

    public static void addRoutes(Javalin app)
    {
        app.get(Path.Web.INDEX, CustomerController::serveIndexPage);
        app.post(Path.Web.SEND_REQUEST, CustomerController::handleFormPost);
    }

    public static void serveIndexPage(Context ctx) {
        User user = ctx.sessionAttribute("user");

        ctx.attribute("user", user);
        ctx.attribute("errmsg", ctx.sessionAttribute("errmsg"));
        ctx.render(Path.Template.INDEX);
        ctx.sessionAttribute("errmsg", null);
    }

    public static void handleFormPost(Context ctx)
    {
        //TODO tilføj check af user ikke er null

            User user = ctx.sessionAttribute("user");
            Offer offer = null;
        if (user!=null) {
            int carportWidth = Integer.parseInt(ctx.formParam("carportWidth"));
            int carportLength = Integer.parseInt(ctx.formParam("carportLength"));
            int carportShedWidth = Integer.parseInt(ctx.formParam("carportShedWidth"));
            int carportShedLength = Integer.parseInt(ctx.formParam("carportShedLength"));
            String adress = ctx.formParam("adress");
            int postalcode = Integer.parseInt(ctx.formParam("postalcode"));
            String city = ctx.formParam("city");
            int height = 2215; // default height
            int customerId = user.id;


            offer = new Offer(0, customerId, adress, postalcode, city, carportWidth, height, carportLength, carportShedWidth, carportShedLength, OfferStatus.SALESPERSON);
            try {
                OfferMapper.addQuery(Server.connectionPool, offer);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        ctx.redirect(Path.Web.INDEX);
    }

}
