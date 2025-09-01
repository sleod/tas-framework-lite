package io.github.sleod.tas.exception;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Löst den MessageKey auf
 */
public class MessageResolver {

    private static final Locale locale = new Locale("en", "DE");
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("messages", locale);

    /**
     * Privater Konstruktor um Instanziierung zu verhindern
     */
    private MessageResolver() {
        throw new IllegalStateException(this.getClass().getSimpleName() + "will not be instantiated");
    }

    /**
     * Auflösen des messageKey in die konkrete Fehlermeldung
     *
     * @param messageKey key für die Fehlermeldung
     * @param parameter  zusätzliche Parameter welche für die Fehlermeldung benötigt wird
     * @return Aufgelöste Fehlermeldung. Wird der Key nicht gefunden wird der Key + Parameter zurückgegeben
     */
    public static String getMessage(String messageKey, Object... parameter) {
        try {
            String message = resourceBundle.getString(messageKey);
            if (parameter != null && parameter.length > 0) {
                message = MessageFormat.format(message, parameter);
            }
            return message;
        } catch (MissingResourceException e) {
            return messageKey + " " + Arrays.toString(parameter);
        }
    }
}