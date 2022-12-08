package ch.qa.testautomation.framework.exception;


public class
ApollonBaseException extends RuntimeException {

    private ApollonErrorKeys messageKeyEnum;
    private Object[] parameter = null;

    public ApollonBaseException(Throwable e) {
        super(e);
    }

    public ApollonBaseException(ApollonErrorKeys messageKey, Throwable e) {
        super(e);
        this.messageKeyEnum = messageKey;
    }

    public ApollonBaseException(ApollonErrorKeys messageKey, Object... params) {
        this.messageKeyEnum = messageKey;
        this.parameter = params;
    }

    public ApollonBaseException(ApollonErrorKeys messageKey, Throwable throwable, Object... params) {
        super(throwable);
        this.messageKeyEnum = messageKey;
        this.parameter = params;
    }

    public ApollonBaseException(ApollonErrorKeys messageKey) {
        this.messageKeyEnum = messageKey;
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
            if (messageKeyEnum != null) {
                message = MessageResolver.getMessage(messageKeyEnum.getErrorKey(), parameter);
            }
        return message;
    }
}
