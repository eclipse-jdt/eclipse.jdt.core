package g6.t.ref;
import g6.t.def.Table;
public class Multiple {
	Table.Entry entry;
	Table<String, Exception>.Entry<String, Exception> entryException;
	Table<String, Exception>.Entry<String, Exception>[] entryExceptionArray;
	Table<String, Table<String, Exception>.Entry<String, Exception>[]>.Entry<String, Table<String, Exception>.Entry<String, Exception>[]> tableOfEntryExceptionArray;
}
