package test;

import java.util.Hashtable;

public class Test {

	public Test() {
		Hashtable fields;

		// TODO some todo text
		// direct field names

		//		==== Dictionaries
		//CountryDictInfo
		fields = new Hashtable();

		fields.put("name", "Name");
		fields.put("numericCode", "NumericCode");

		getSqls().put("finmon.domain.dictionary", new SomeFieldSQLDescriptor(
			this, "SELECT * FROM CountryDict WHERE ",
			" ORDER BY Name", fields))		;

	}
}