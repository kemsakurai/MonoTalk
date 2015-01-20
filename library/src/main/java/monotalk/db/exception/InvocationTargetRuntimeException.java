package monotalk.db.exception;

import java.lang.reflect.InvocationTargetException;

public class InvocationTargetRuntimeException extends BaseRuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -168072220345305127L;

    public InvocationTargetRuntimeException(InvocationTargetException e) {
        super(e);
    }

}
