package monotalk.db.query;

import monotalk.db.typeconverter.TypeConverter;
import monotalk.db.typeconverter.TypeConverterCache;
import monotalk.db.utility.AssertUtils;

public class Expressions<E extends AbstractSelection> {

    private E abstractSelection;

    public Expressions(E abstractSelection) {
        this.abstractSelection = abstractSelection;
    }

    public E eq(Object selectionArg) {
        abstractSelection.addSelection(" = ");
        appendCastAs(selectionArg);
        abstractSelection.addArguments(selectionArg);
        return abstractSelection;
    }

    private void appendCastAs(Object selectionArg) {
        TypeConverter converter = TypeConverterCache.getTypeConverter(selectionArg.getClass());
        abstractSelection.addSelection("CAST(? AS " + converter.getSqlType() + ") ");
    }

    public E lt(Object selectionArg) {
        abstractSelection.addSelection(" < ");
        appendCastAs(selectionArg);
        abstractSelection.addArguments(selectionArg);
        return abstractSelection;
    }

    public E le(Object selectionArg) {
        abstractSelection.addSelection(" <= ");
        appendCastAs(selectionArg);
        abstractSelection.addArguments(selectionArg);
        return abstractSelection;
    }

    public E gt(Object selectionArg) {
        abstractSelection.addSelection(" > ");
        appendCastAs(selectionArg);
        abstractSelection.addArguments(selectionArg);
        return abstractSelection;
    }

    public E ge(Object selectionArg) {
        abstractSelection.addSelection(" >= ");
        appendCastAs(selectionArg);
        abstractSelection.addArguments(selectionArg);
        return abstractSelection;
    }

    public E ne(Object selectionArg) {
        abstractSelection.addSelection(" <> ");
        appendCastAs(selectionArg);
        abstractSelection.addArguments(selectionArg);
        return abstractSelection;
    }

    @SuppressWarnings("unchecked")
    public E in(Object... selectionArg) {
        AssertUtils.assertArgument(selectionArg.length > 0, "selectionArg is Empty");
        StringBuilder sb = new StringBuilder();
        sb.append(" in (");
        for (Object value : selectionArg) {
            @SuppressWarnings("rawtypes")
            TypeConverter typeConverter = TypeConverterCache.getTypeConverter(value.getClass());
            if (typeConverter != null) {
                sb.append(typeConverter.toSql(value));
                sb.append(",");
                continue;
            } else {
                sb.append(selectionArg.toString());
                sb.append(",");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(") ");
        abstractSelection.addSelection(sb.toString());
        return (E) abstractSelection;
    }

    public E isNull() {
        abstractSelection.addSelection(" IS NULL");
        return (E) abstractSelection;
    }

    public E isNotNull() {
        abstractSelection.addSelection(" IS NOT NULL");
        return (E) abstractSelection;
    }
}
