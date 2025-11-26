package app.web;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;

public class CustomerController{

    public static void addRoutes(Javalin app)
    {
        app.get(Path.Web.INDEX, CustomerController::serveIndexPage);
        app.post(Path.Web.SEND_REQUEST, CustomerController::handleFormPost);
    }

    public static void serveIndexPage(Context ctx)
    {
        ctx.attribute("user", ctx.sessionAttribute("user"));
        ctx.attribute("errmsg", ctx.sessionAttribute("errmsg"));
        ctx.render(Path.Template.INDEX);
        ctx.sessionAttribute("errmsg", null);
    }

    public static void handleFormPost(Context ctx)
    {

        int carportWidth = Integer.parseInt(ctx.formParam("carportWidth"));
        int carportLength = Integer.parseInt(ctx.formParam("carportLength"));
        String carportRoof = ctx.formParam("carportRoof");
        int carportShedWidth = Integer.parseInt(ctx.formParam("carportShedWidth"));
        int carportShedLength = Integer.parseInt(ctx.formParam("carportShedLength"));


        Map<String, Object> formData = new HashMap<>();
        formData.put("carportWidth", carportWidth);
        formData.put("carportLength", carportLength);
        formData.put("carportRoof", carportRoof);
        formData.put("carportShedWidth", carportShedWidth);
        formData.put("carportShedLength", carportShedLength);

        ctx.sessionAttribute("formData", formData);
        ctx.redirect(Path.Web.INDEX);
    }

}
