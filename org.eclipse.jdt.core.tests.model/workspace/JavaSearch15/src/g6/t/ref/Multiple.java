package g6.t.ref;
import g6.t.def.Table;
public class Multiple {
	Table./**s1*/Entry entry;
	Table<String, Exception>./**s2*/Entry<String, Exception> entryException;
	Table<String, Exception>./**s3*/Entry<String, Exception>[] entryExceptionArray;
	Table<String, Table<String, Exception>.Entry<String, Exception>[]>./**s4*/Entry<String, Table<String, Exception>.Entry<String, Exception>[]> tableOfEntryExceptionArray;
}
