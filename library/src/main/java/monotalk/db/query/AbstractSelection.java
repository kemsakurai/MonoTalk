package monotalk.db.query;

abstract public class AbstractSelection {
    protected abstract void addSelection(String string);

    protected abstract void addArguments(Object... args);
}