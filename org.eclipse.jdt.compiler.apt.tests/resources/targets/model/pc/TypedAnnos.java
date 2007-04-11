package targets.model.pc;

/**
 * A collection of annotation types with variously typed values.
 */
public interface TypedAnnos {
	public enum Enum { A, B, C }
	
	public @interface AnnoByte {
		byte value();
	}
	public @interface AnnoBoolean {
		boolean value();
	}
	public @interface AnnoChar {
		char value();
	}
	public @interface AnnoDouble {
		double value();
	}
	public @interface AnnoFloat {
		float value();
	}
	public @interface AnnoInt {
		int value();
	}
	public @interface AnnoLong {
		long value();
	}
	public @interface AnnoShort {
		short value();
	}
	public @interface AnnoString {
		String value();
	}
	public @interface AnnoEnumConst {
		Enum value();
	}
	public @interface AnnoArrayType {
		Class<?>[] value();
	}
	public @interface AnnoType {
		Class<?> value();
	}
	public @interface AnnoAnnoChar {
		AnnoChar value();
	}
	
	// Do not change this code without also changing VisitorProc.
	@AnnoByte(3)
	@AnnoBoolean(true)
	@AnnoChar('c')
	@AnnoDouble(6.3)
	@AnnoFloat(26.7F)
	@AnnoInt(19)
	@AnnoLong(300L)
	@AnnoShort(289)
	@AnnoString("foo")
	@AnnoEnumConst(Enum.A)
	@AnnoArrayType({ String.class, Annotated.class })
	@AnnoType(Exception.class)
	@AnnoAnnoChar(@AnnoChar('x'))
	public class Annotated {}
}