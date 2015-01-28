package monotalk.db.exception;

/**
 * Created by Kem on 2015/01/22.
 */
public class ConnectionSourceNotFoundException extends BaseRuntimeException {
    public ConnectionSourceNotFoundException(String name) {
        super("connectionSource not found name=[" + name + "]");
    }
}
