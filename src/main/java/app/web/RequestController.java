package app.web;

import io.javalin.Javalin;
import io.javalin.http.Context;

public class RequestController {

    public static void addRoutes(Javalin app) {
        app.get("/bestil-carport", RequestController::serveCarportForm);
        app.post("/send-request", RequestController::handleRequestPost);
    }

    // Vis formular
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

    // Modtag formular
    public static void handleRequestPost(Context ctx) {
        String width = ctx.formParam("carportWidth");
        String length = ctx.formParam("carportLength");
        String roof = ctx.formParam("carportRoof");
        String shedWidth = ctx.formParam("carportShedWidth");
        String shedLength = ctx.formParam("carportShedLength");

        // Print til konsol
        System.out.println("Ny carport forespørgsel modtaget:");
        System.out.println("Bredde: " + width);
        System.out.println("Længde: " + length);
        System.out.println("Tag: " + roof);
        System.out.println("Skur bredde: " + shedWidth);
        System.out.println("Skur længde: " + shedLength);

        // Svar til bruger
        ctx.html("<h2>Tak for din forespørgsel!</h2><p>Vi vender tilbage snarest.</p>");
    }
}
