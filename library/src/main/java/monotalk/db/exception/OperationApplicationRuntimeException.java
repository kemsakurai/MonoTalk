package monotalk.db.exception;

import android.content.OperationApplicationException;

/**
 * Created by Kem on 2015/01/12.
 */
public class OperationApplicationRuntimeException extends BaseRuntimeException {
    /**
     * コンストラクター
     *
     * @param e
     */
    public OperationApplicationRuntimeException(OperationApplicationException e) {
        super(e);
    }
}
