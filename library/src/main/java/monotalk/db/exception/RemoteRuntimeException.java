package monotalk.db.exception;

import android.os.RemoteException;

public class RemoteRuntimeException extends BaseRuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 2568472638003203329L;

    /**
     * コンストラクター
     *
     * @param e
     */
    public RemoteRuntimeException(RemoteException e) {
        super(e);
    }


}
