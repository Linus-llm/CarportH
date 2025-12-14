package app.web;

import app.db.UserMapper;
import app.db.User;
import app.exceptions.DBException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;


public class UserController {

    public static void addRoutes(Javalin app)
    {
        app.get(Path.Web.LOGIN, UserController::serveLoginPage);
        app.post(Path.Web.LOGIN, UserController::handleLoginPost);
        app.post(Path.Web.REGISTER, UserController::handleRegisterPost);
        app.post(Path.Web.LOGOUT, UserController::handleLogoutPost);

    }

    public static void serveLoginPage(Context ctx)
    {
        ctx.attribute("errmsg", ctx.sessionAttribute("errmsg"));
        ctx.render(Path.Template.LOGIN);
        ctx.sessionAttribute("errmsg", null);
    }

    public static void handleLoginPost(Context ctx) {
        User user;
        try {
            String redirect = ctx.sessionAttribute("loginredirect");
            String email = ctx.formParam("email");
            String password = ctx.formParam("password");
            if (email == null || password == null) {
                ctx.status(HttpStatus.BAD_REQUEST);
                return;
            }
            if (email.isEmpty() || password.isEmpty()) {
                ctx.sessionAttribute("errmsg", "* Ugyldig email eller password");
                ctx.redirect(Path.Web.LOGIN);
                return;
            }
            user = UserMapper.login(Server.connectionPool, email, password);
            if (user == null) {
                ctx.sessionAttribute("errmsg", "* Ugyldig email eller password");
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
        catch (DBException e) {
            System.out.println("ERROR: " + e.getMessage());
            ctx.sessionAttribute("errmsg", "Database fejl");
            ctx.redirect(Path.Web.LOGIN);
        }
    }

    public static void handleLogoutPost(Context ctx)
    {
        ctx.sessionAttribute("user", null);
        ctx.redirect(Path.Web.INDEX);
    }

    public static void handleRegisterPost(Context ctx) {
        String name = ctx.formParam("name");
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        try {
            if (name == null || email == null || password == null) {
                ctx.status(HttpStatus.BAD_REQUEST);
                return;
            }
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() ||
                    !UserMapper.register(Server.connectionPool, name, email, password)) {
                ctx.sessionAttribute("errmsg", "* Failed to register");
                ctx.redirect(Path.Web.LOGIN);
                return;
            }
            ctx.redirect(Path.Web.INDEX);
        } catch (DBException e){
            System.out.println("ERROR: " + e.getMessage());
            ctx.sessionAttribute("errmsg", "Database fejl");
            ctx.redirect(Path.Web.LOGIN);
        }
    }
}
