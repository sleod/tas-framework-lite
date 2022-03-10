package ch.qa.testautomation.framework.exception;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Löst den MessageKey auf
 */
public class MessageResolver {

    private static Locale locale = new Locale("en", "DE");
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("messages", locale);

    private MessageResolver() {
        throw new IllegalStateException( this.getClass().getSimpleName() + "will not be instantiated");
    }

    /**
     * Auflösen des messageKey in die konkrete Fehlermeldung
     * @param messageKey key für die Fehlermeldung
     * @param parameter zusätzliche Parameter welche für de Fehlermeldung benötigt wird
     * @return Aufgelöste Fehlermeldung. Wird der Key nicht gefunden wird der Key + Parameter zurückgegeben
     */
    public static String getMessage(String messageKey, Object... parameter) {

        try {

            var message = resourceBundle.getString(messageKey);
            return MessageFormat.format(message, parameter);

        } catch (MissingResourceException e) {

            return messageKey + " " + parameter;
        }
    }

    public static String getMessage(String messageKey) {

        return getMessage(messageKey,(Object)null);
    }
}