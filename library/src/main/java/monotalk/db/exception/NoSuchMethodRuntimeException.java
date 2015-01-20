package monotalk.db.exception;

public class NoSuchMethodRuntimeException extends BaseRuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 6182682358537883806L;

    public NoSuchMethodRuntimeException(NoSuchMethodException e) {
        super(e);
    }

}
