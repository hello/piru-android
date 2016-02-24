package is.hello.piru.exception;

public class PillNotFoundException extends RuntimeException{
    public PillNotFoundException(){
        super("Pill not found");
    }
    public PillNotFoundException(String detailMessage) {
        super(detailMessage);
    }

    public PillNotFoundException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

}
