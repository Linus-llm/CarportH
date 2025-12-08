package app.web;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Properties;

public class Translator {
    private static Properties props = new Properties();
    private static NumberFormat numberFormat;

    static
    {
        try {
            props.load(new InputStreamReader(Translator.class.getResourceAsStream("/lang/da_DK.lang"),
                    StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.out.println("ERROR: failed to load lang file");
            props = null;
        }
        numberFormat = NumberFormat.getInstance(Locale.US);
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
    }

    public static String translate(String key)
    {
        if (props == null)
            return key;
        return props.getProperty(key, key);
    }

    public static String currency(double value)
    {
        return numberFormat.format(value);
    }

    public static double parseCurrency(String value)
            throws ParseException
    {
        Number n = numberFormat.parse(value);
        return n.doubleValue();
    }
}
