package app.web;

public class Path {

    public static class Web {
        public static final String INDEX = "/";
        public static final String LOGIN = "/login";
        public static final String SALES = "/sales";
        public static final String SALES_NEW_OFFER = "/sales/new-offer/";//+{id}
        public static final String SALES_SEND_OFFER = "/sales/send-offer";
        public static final String SALES_SET_PRICE = "/sales/set-price";
        public static final String SALES_CALC = "/sales/calc";
        public static final String SALES_CLAIM_OFFER = "/sales/claim-offer/{id}";
        public static final String REGISTER ="/register";
        public static final String SEND_REQUEST ="/SendRequest";
        public static final String USER_OFFERS = "/customer/offers";
        public static final String LOGOUT = "/logout";
    }

    public static class Template {
        public static final String INDEX = "/index.html";
        public static final String LOGIN = "/login.html";
        public static final String SALES = "/sales.html";
        public static final String SALES_NEW_OFFER = "/new_offer.html";
        public static final String USER_OFFERS = "customerPage.html";
    }
}
