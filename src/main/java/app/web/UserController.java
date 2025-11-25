package app.web;

import app.db.UserMapper;
import app.db.User;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.ArrayList;

public class UserController {

    public static void addRoutes(Javalin app)
    {
        app.get(Path.Web.INDEX, UserController::serveIndexPage);
        app.get(Path.Web.LOGIN, UserController::serveLoginPage);
        app.post(Path.Web.LOGIN, UserController::handleLoginPost);
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
}
