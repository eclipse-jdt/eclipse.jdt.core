package r6;
/* Test case for bug 23077  search: does not find type references in some imports  */
import r6.A;
import r6.A.Inner;
import r6.A.Inner.InnerInner;
import r6.A.*;
import r6.A.Inner.*;
import r6.A.Inner.InnerInner.*;
public class B {
}
