package ch.qa.testautomation.framework.exception;

public class JsonProcessException extends RuntimeException {

    public JsonProcessException(String errorMessage, Throwable e){
        super(errorMessage,e);
    }
    public JsonProcessException(Throwable e){
        super(e);
    }
    public JsonProcessException(String errorMessage){
        super(errorMessage);
    }
}
