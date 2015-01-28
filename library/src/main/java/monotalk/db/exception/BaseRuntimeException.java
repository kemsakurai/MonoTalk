package monotalk.db.exception;

public class BaseRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 4848170416420764516L;

    public BaseRuntimeException() {
        super();
    }

    public BaseRuntimeException(String message) {
        super(message);
    }

    public BaseRuntimeException(Throwable e) {
        super(e);
    }
}
