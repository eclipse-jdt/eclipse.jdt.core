package test0329;

public @interface JoinTable {
	String name();
	JoinColumn[] joinColumns();
	JoinColumn[] inverseJoinColumns();
}
