import java.util.Map;

interface MethodProperty<ActualType extends MethodProperty<ActualType>> {
	public void copyFrom(ActualType other);
}

class MethodPropertyDatabase<Property extends MethodProperty<Property>> {
	Map<String, Property> propertyMap;

	void read(String fileName) {
	}
}

class FooProperty implements MethodProperty<FooProperty> {
	String value;

	public void copyFrom(FooProperty other) {
		this.value = other.value;
	}
}

class FooPropertyDatabase extends MethodPropertyDatabase<FooProperty> {
}

public class GenericsBug {
	FooPropertyDatabase fooDatabase;

	public void readDatabase() {
		FooPropertyDatabase database = new FooPropertyDatabase();

		fooDatabase = readDatabase(database, "foodatabase.db");
	}

	private <Property extends MethodProperty<Property>, DatabaseType extends MethodPropertyDatabase<Property>> DatabaseType readDatabase(
			DatabaseType database, String fileName) {
		database.read(fileName);
		return database;
	}

}
