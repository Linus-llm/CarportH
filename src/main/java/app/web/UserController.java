package app.web;

import app.db.Offer;
import app.db.OfferMapper;
import app.db.UserMapper;
import app.db.User;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class UserController {

    public static void addRoutes(Javalin app)
    {
        app.get(Path.Web.INDEX, UserController::serveIndexPage);
        app.get(Path.Web.LOGIN, UserController::serveLoginPage);
        app.post(Path.Web.LOGIN, UserController::handleLoginPost);
        app.post(Path.Web.REGISTER, UserController::handleRegisterPost);

        app.get(Path.Web.USER_OFFERS, UserController::serveUserOffersPage);

    }
    public static void serveLoginPage(Context ctx)
    {
        ctx.attribute("errmsg", ctx.sessionAttribute("errmsg"));
        ctx.render(Path.Template.LOGIN);
        ctx.sessionAttribute("errmsg", null);
    }

    public static void serveIndexPage(Context ctx)
    {
        ctx.attribute("user", ctx.sessionAttribute("user"));
        ctx.attribute("errmsg", ctx.sessionAttribute("errmsg"));
        ctx.render(Path.Template.INDEX);
        ctx.sessionAttribute("errmsg", null);
    }

    public static void handleLoginPost(Context ctx)
    {
        User user;
        String redirect = ctx.sessionAttribute("loginredirect");
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        if (email == null || password == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return;
        }
        if (email.isEmpty() || password.isEmpty()) {
            ctx.sessionAttribute("errmsg", "* Invalid email or password");
            ctx.redirect(Path.Web.LOGIN);
            return;
        }
        user = UserMapper.login(email, password);
        if (user == null) {
            ctx.sessionAttribute("errmsg", "* Invalid email or password");
            ctx.redirect(Path.Web.LOGIN);
            return;
        }
        ctx.sessionAttribute("user", user);
        if (redirect != null) {
            ctx.sessionAttribute("loginredirect", null);
            ctx.redirect(redirect);
            return;
        }
        ctx.redirect(Path.Web.INDEX);
    }

    public static void handleRegisterPost(Context ctx)
    {
        String name = ctx.formParam("name");
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        if (name == null || email == null || password == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return;
        }
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() ||
                !UserMapper.register(name, email, password)) {
            ctx.sessionAttribute("errmsg", "* Failed to register");
            ctx.redirect(Path.Web.LOGIN);
            return;
        }
        ctx.redirect(Path.Web.INDEX);
    }


    public static void serveUserOffersPage(Context ctx)
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

}
