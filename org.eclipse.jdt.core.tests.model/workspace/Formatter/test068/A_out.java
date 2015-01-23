public static Compiler getCompiler() {
	if (compiler == null) {
		CompilerOptions options = new CompilerOptions();
		options.handleImportProblemAsError(false);
		compiler = new Compiler(
				null,
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				options.getConfigurableOptions(Locale.getDefault()),
				new ICompilerRequestor() {
					private CompilationResult compilationResult; // toto
					public void acceptResult(
							CompilationResult compilationResult) {
						this.compilationResult = compilationResult;
					}
					public CompilationResult retrieveCompilationResult() {
						return compilationResult;
					}
				},
				compilerProblemFactory);
	}
	return compiler;
}