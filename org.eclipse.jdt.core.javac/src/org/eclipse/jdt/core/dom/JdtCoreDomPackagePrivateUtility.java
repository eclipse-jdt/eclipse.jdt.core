package org.eclipse.jdt.core.dom;

public class JdtCoreDomPackagePrivateUtility {
	public static BindingResolver getBindingResolver(ASTNode node) {
		return node.getAST().getBindingResolver();
	}
	
	public static JavacBindingResolver getJavacBindingResolverOrNull(ASTNode node) {
		BindingResolver br = getBindingResolver(node);
		return br instanceof JavacBindingResolver br2 ? br2 : null;
	}
	
	public static IBinding findBindingForType(ASTNode node, String signature) {
		JavacBindingResolver jcbr = getJavacBindingResolverOrNull(node);
		IBinding ret1 = jcbr instanceof JavacBindingResolver br2 ? br2.findBinding(signature) : null;
		if( ret1 == null ) {
			String sig2 = signature.replaceAll("\\.", "/");
			ret1 = jcbr instanceof JavacBindingResolver br2 ? br2.findBinding(sig2) : null;
		}
		return ret1;
	}
	
	public static IBinding findUnresolvedBindingForType(ASTNode node, String signature) {
		JavacBindingResolver jcbr = getJavacBindingResolverOrNull(node);
		IBinding ret1 = jcbr instanceof JavacBindingResolver br2 ? br2.findBinding(signature) : null;
		if( ret1 == null ) {
			String sig2 = signature.replaceAll("\\.", "/");
			ret1 = jcbr instanceof JavacBindingResolver br2 ? br2.findUnresolvedBinding(sig2) : null;
		}
		return ret1;
	}


}
