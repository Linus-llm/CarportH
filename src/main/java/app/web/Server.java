package app.web;

import io.javalin.Javalin;

import java.sql.SQLException;

import io.javalin.rendering.template.JavalinThymeleaf;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import app.db.ConnectionPool;

public class Server {

    private static final String JDBC_USER = System.getenv("JDBC_USER");
    private static final String JDBC_PASSWORD = System.getenv("JDBC_PASSWORD");
    private static final String JDBC_URL = System.getenv("JDBC_URL");
    private static final String JDBC_DB = System.getenv("JDBC_DB");
    static ConnectionPool connectionPool;

    public static void main(String[] args)
    {
        Javalin app;
        try {
            connectionPool = new ConnectionPool(JDBC_USER, JDBC_PASSWORD, JDBC_URL, JDBC_DB);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Starting server");
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateEngine.setTemplateResolver(templateResolver);

        app = Javalin.create(config -> {
            config.useVirtualThreads = true;
            config.http.asyncTimeout = 10_000L;
            config.fileRenderer(new JavalinThymeleaf(templateEngine));
            config.staticFiles.add("/public");
        });

        app.exception(java.io.FileNotFoundException.class, (e, ctx) -> {
            ctx.status(404);
        });
        app.exception(org.thymeleaf.exceptions.TemplateInputException.class, (e, ctx) -> {
            ctx.status(404);
        });
        app.exception(Exception.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(500);
        });


        UserController.addRoutes(app);
        CustomerController.addRoutes(app);
        SalesController.addRoutes(app);

        app.start(7070);

    }
}
