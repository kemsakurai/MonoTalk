package monotalk.db.exception;

public class NoTypeSerializerFoundException extends RuntimeException {

    private static final long serialVersionUID = 2617697703721283015L;

    public NoTypeSerializerFoundException(Class<?> type) {
        super(String.format("Could not serialize field with class %s, no TypeConverter found.", type.getName()));
    }

}