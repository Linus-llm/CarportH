package app.web;

import app.db.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalesController {

    public static void addRoutes(Javalin app) {
        app.before(Path.Web.SALES + "*", SalesController::before);
        app.get(Path.Web.SALES, SalesController::serveMainPage);
        app.get(Path.Web.SALES_NEW_OFFER, SalesController::serveNewOfferPage);
        app.post(Path.Web.SALES_CALC, SalesController::handleCalcPost);
        app.post(Path.Web.SALES_SEND_OFFER, SalesController::handleSendOfferPost);
        app.post("/sales/claim-offer/{id}", SalesController::handleClaimOffer);
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
            //har en sælger
            List<Offer> myOffers = OfferMapper.getSalespersonOffers(Server.connectionPool, user.id);
            //har ikk en sælger
            List<Offer> unassignedOffers = OfferMapper.getUnassignedOffers(Server.connectionPool);
            ctx.attribute("myOffers", myOffers);
            ctx.attribute("unassignedOffers", unassignedOffers);

            offers = OfferMapper.getSalespersonOffers(Server.connectionPool, user.id);
            ctx.attribute("offers", offers);
            ctx.attribute("user", user);
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
                ctx.sessionAttribute("offer", offer);
            }
            if (offer == null) {
                ctx.status(404);
                return;
            }
            // TODO: List<Bill> bills = BillMapper.getBills(Server.connectionPool, offerId);
            // ctx.attribute("bills", bills);
            User customer = UserMapper.getUser(Server.connectionPool, offer.customerId);
            ctx.attribute("customer", customer);
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
        offer.width = (int) (Float.parseFloat(w) * 1000);
        offer.length = (int) (Float.parseFloat(l) * 1000);
        offer.height = (int) (Float.parseFloat(h) * 1000);

        // TODO: calculate mat list
        CarportCalculator calculator = new CarportCalculator();
        List<WoodNeed> needs = calculator.calculateNeeds(offer.length, offer.width, offer.height);

        List<Bill> bills = new ArrayList<>();

        try {
            for (WoodNeed need : needs) {

                WoodCategory category = switch (need.type) {
                    case PILLAR -> WoodCategory.PILLAR;
                    case RAFTER -> WoodCategory.RAFTER;
                    case BOARD -> WoodCategory.BOARD;
                };
                // 4. Use WoodMapper to get a stock wood piece with sufficient length
                Wood wood = null;
                try {
                    wood = WoodMapper.getWood(Server.connectionPool, category, need.requiredLengthMm);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                if (wood == null) {
                    // handle "no suitable wood found" – for now you can just continue or throw
                    continue;
                }

                // 5. Calculate line price
                // price per meter * wood length (meters) * quantity
                double pricePerMeter = wood.pricePerMeter;
                double lengthMeters  = wood.length / 1000.0;
                double linePrice     = pricePerMeter * lengthMeters * need.count;
                // 6. Create bill object
                Bill bill = new Bill(
                        offer.id,
                        wood.id,       // the woods.id from DB
                        need.count,
                        linePrice
                );

                BillMapper.insert(Server.connectionPool, bill);

                bills.add(bill);
            }
            double total = bills.stream().mapToDouble(b -> b.price).sum();

            offer.price = total;
            OfferMapper.updatePrice(Server.connectionPool, offer.id, total);

        } catch(Exception e){
                System.out.println("ERROR: " + e.getMessage());
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            }


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
            offer.text = ctx.formParam("text");
            offer.status = OfferStatus.CUSTOMER;
            OfferMapper.updateOffer(Server.connectionPool, offer);
        } catch (SQLException e) {
            System.out.println("ERROR: "+e.getMessage());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            return;
        }
        ctx.redirect(Path.Web.SALES);
    }
    public static void handleClaimOffer(Context ctx) {
        User user = ctx.sessionAttribute("user");
        if (user == null || user.role != UserRole.SALESPERSON) {
            ctx.status(403);
            return;
        }

        int offerId = Integer.parseInt(ctx.pathParam("id"));
        try {
            OfferMapper.assignSalesperson(Server.connectionPool, offerId, user.id);
            ctx.redirect(Path.Web.SALES);
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500);
        }
    }





}