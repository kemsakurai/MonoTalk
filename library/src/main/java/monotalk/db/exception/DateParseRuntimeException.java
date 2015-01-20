package monotalk.db.exception;

import org.apache.http.impl.cookie.DateParseException;

public class DateParseRuntimeException extends BaseRuntimeException {
    /**
     * シリアルVerUID
     */
    private static final long serialVersionUID = -3319978942387649174L;

    /**
     * コンストラクター
     *
     * @param e
     */
    public DateParseRuntimeException(DateParseException e) {
        super(e);
    }

}
