package app.web;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Translator {
    private static Properties props = new Properties();

    static
    {
        try {
            props.load(new InputStreamReader(Translator.class.getResourceAsStream("/lang/da_DK.lang"),
                    StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.out.println("ERROR: failed to load lang file");
            props = null;
        }
    }

    public static String translate(String key)
    {
        if (props == null)
            return key;
        return props.getProperty(key, key);
    }
}
