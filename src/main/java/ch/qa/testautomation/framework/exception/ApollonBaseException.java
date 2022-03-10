package ch.qa.testautomation.framework.exception;

import ch.qa.testautomation.framework.common.logging.SystemLogger;

public class ApollonBaseException extends RuntimeException{

    //TODO es gibt keine Möglichkeit um einen direkten String mitzugeben ist das ok?
    private String messageKey;
    private Object[] parameter;

    public ApollonBaseException(Throwable e) {
        super(e);
    }


    public ApollonBaseException(String messageKey, Throwable e) {
        super(e);
        this.messageKey = messageKey;
    }

    public ApollonBaseException(String messageKey, Object... params) {

        this.messageKey = messageKey;
        this.parameter = params;
    }

    public ApollonBaseException(String messageKey) {

        this.messageKey = messageKey;
    }

    public ApollonBaseException(String messageKey, Throwable e,Object... params) {

        super(e);
        this.messageKey = messageKey;
        this.parameter = params;
    }

    @Override
    public String getMessage() {

        String message = super.getMessage();
        try {
            if (messageKey != null) {
                message = MessageResolver.getMessage(messageKey, parameter);
            }
        } catch (Exception e) {
            SystemLogger.error(e);
        }

        return message;
    }
}
