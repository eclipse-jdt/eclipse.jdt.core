class F {
	void foo() {
		if (local.useFlag == LocalVariableBinding.UNUSED
				&& (local.declaration != null) // unused (and non secret) local
				&& ((local.declaration.bits & AstNode.IsLocalDeclarationReachableMASK) != 0)) { // declaration is reachable
		}
	}
}