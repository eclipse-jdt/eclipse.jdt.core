package java.lang;

public
class NoSuchFieldError extends IncompatibleClassChangeError {

	static final long serialVersionUID = 1L;

	public NoSuchFieldError() {
		super();
	}

	public NoSuchFieldError(String s) {
		super(s);
	}
}
