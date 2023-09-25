package ch.qa.testautomation.tas.exception;


import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ApollonBaseException extends RuntimeException {

    private final ApollonErrorKeys messageKeyEnum;
    private final List<Object> parameter = new LinkedList<>();

    public ApollonBaseException(ApollonErrorKeys messageKey, Object... params) {
        super("");
        this.messageKeyEnum = messageKey;
        if (Objects.nonNull(params)) {
            this.parameter.addAll(List.of(params));
        }
    }

    public ApollonBaseException(ApollonErrorKeys messageKey, Throwable throwable, Object... params) {
        super(throwable);
        this.messageKeyEnum = messageKey;
        if (Objects.nonNull(params)) {
            this.parameter.addAll(List.of(params));
        }
    }

    public ApollonBaseException(ApollonErrorKeys messageKey) {
        super("");
        this.messageKeyEnum = messageKey;
    }

    @Override
    public String getMessage() {
        parameter.add(super.getMessage());
        return MessageResolver.getMessage(messageKeyEnum.getErrorKey(), parameter.toArray());
    }
}
