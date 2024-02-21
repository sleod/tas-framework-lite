package ch.qa.testautomation.tas.exception;


import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ExceptionBase extends RuntimeException {

    private final ExceptionErrorKeys messageKeyEnum;
    private final List<Object> parameter = new LinkedList<>();

    public ExceptionBase(ExceptionErrorKeys messageKey, Object... params) {
        super("");
        this.messageKeyEnum = messageKey;
        if (Objects.nonNull(params)) {
            this.parameter.addAll(List.of(params));
        }
    }

    public ExceptionBase(ExceptionErrorKeys messageKey, Throwable throwable, Object... params) {
        super(throwable);
        this.messageKeyEnum = messageKey;
        if (Objects.nonNull(params)) {
            this.parameter.addAll(List.of(params));
        }
    }

    public ExceptionBase(ExceptionErrorKeys messageKey) {
        super("");
        this.messageKeyEnum = messageKey;
    }

    @Override
    public String getMessage() {
        parameter.add(super.getMessage());
        return MessageResolver.getMessage(messageKeyEnum.getErrorKey(), parameter.toArray());
    }
}
