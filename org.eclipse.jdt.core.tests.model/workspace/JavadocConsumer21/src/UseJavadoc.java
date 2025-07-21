import java.util.List;
import java.util.Map;

import package1.MyAnnotation;
import package1.MyClass;
import package1.MyClass.NestedAnnotation;
import package1.MyClass.NestedClass;
import package1.MyClass.NestedEnum;
import package1.MyEnum;
import package1.MyInterface;
import package1.MyRecord;

public class UseJavadoc {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		MyClass clazz = new MyClass() {
			@Override
			public String abstractPublicMethod() {
				return publicField;
			}
			@Override
			protected String abstractProtectedMethod() {
				return protectedField;
			}
		};
		MyEnum en = MyEnum.ONE;
		@MyAnnotation
		MyInterface<String> iface = new MyInterface<String>() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public <T> T[] toArray(T[] arg0) {
				return null;
			}
			/**
			 * {@inheritDoc}
			 */
			@Override
			public String m2(int index) {
				return null;
			}
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void m1(int arg0, String arg1) {
			}
			/**
			 * {@inheritDoc}
			 */
			@Override
			public boolean m(String arg0) {
				return false;
			}
			/**
			 * {@inheritDoc}
			 */
			@Override
			public List<String> d() {
				return null;
			}
			/**
			 * {@inheritDoc}
			 */
			@Override
			public <K, V> Map<K, V> bar5(Map<K, V> arg0, int arg1, Map<K, V> arg2) {
				return null;
			}
			/**
			 * {@inheritDoc}
			 */
			@Override
			public boolean bar4(int arg0, List<? extends String> arg1) {
				return false;
			}
			/**
			 * {@inheritDoc}
			 */
			@Override
			public boolean bar3(List<?> c) {
				return false;
			}
			/**
			 * {@inheritDoc}
			 */
			@Override
			public String bar1(int arg0, String arg1) {
				return null;
			}
			/**
			 * {@inheritDoc}
			 */
			@Override
			public Object[] bar() {
				return null;
			}
			/**
			 * {@inheritDoc}
			 */
			@Override
			public String abstractPublicMethod() {
				return null;
			}
		};

		iface.abstractPublicMethod();
		iface.bar5(null, 0, null);

		@MyAnnotation(explanation = "Hello")
		MyClass.NestedEnum nestedEnum = NestedEnum.ONE;
		@NestedAnnotation(explanation = "Hello")
		MyClass.NestedInterface<List<String>> iface2 = new MyClass.NestedInterface<List<String>>() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public String abstractPublicMethod() {
				return null;
			}
			/**
			 * {@inheritDoc}
			 */
			@Override
			public Object[] bar() {
				return null;
			}
			/**
			 * {@inheritDoc}
			 */
			@Override
			public List<String> bar1(int arg0, List<String> arg1) {
				return null;
			}
			/**
			 * {@inheritDoc}
			 */
			@Override
			public boolean bar3(List<?> c) {
				return false;
			}
			/**
			 * {@inheritDoc}
			 */
			@Override
			public boolean bar4(int arg0, List<? extends List<String>> arg1) {
				return false;
			}
			/**
			 * {@inheritDoc}
			 */
			@Override
			public <K, V> Map<K, V> bar5(Map<K, V> arg0, int arg1, Map<K, V> arg2) {
				return null;
			}
			/**
			 * {@inheritDoc}
			 */
			@Override
			public List<List<String>> d() {
				return null;
			}
			/**
			 * {@inheritDoc}
			 */
			@Override
			public boolean m(List<String> arg0) {
				return false;
			}
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void m1(int arg0, List<String> arg1) {
			}
			/**
			 * {@inheritDoc}
			 */
			@Override
			public List<String> m2(int index) {
				return null;
			}
			/**
			 * {@inheritDoc}
			 */
			@Override
			public <T> T[] toArray(T[] arg0) {
				return null;
			}
		};
		NestedClass<Map<String,Integer>> nestedClass = new MyClass.NestedClass<>() {
			@Override
			public List<Map<String, Integer>> getSomething() {
				this.fieldT = null;
				return super.getSomething();
			}
		};
		nestedClass.getField();
		nestedClass.getSomething(null, null);
		nestedClass = new NestedClass<>();
		nestedClass = new NestedClass<Map<String,Integer>>(null);
		nestedClass = new NestedClass<Map<String,Integer>>(null, null);

		MyRecord rec = new MyRecord("", "");
		rec.one();
		rec.two();
		MyClass.NestedRecord rec2 = new MyClass.NestedRecord("", "");
		rec2.one();
		rec2.two();


	}

}
