# Eclipse JDT over Javac

This fragment contains a Javac backend (instead of ECJ) for JDT features:
* error reporting/reconciling
* build/compilation
* indexing
* code selection (Ctrl + click / hover)
* code completion (requires JDT fork bundle + opt-out flag)
* search/match (requires JDT fork bundle + opt-out flag)

## ❓ Why?

Some background...
* These days, with more frequent and more features Java releases, it's becoming hard for JDT to **cope with new Java features on time** and **facilitate support for upcoming/preview features before Java is released so JDT can participate to consolidation of the spec**. Over recent releases, JDT has failed at providing the features on time. This is mostly because of the difficulty of maintaining the Eclipse compiler: compilers are difficult bits of code to maintain and it takes a lot of time to implement things well in them. There is no clear sign the situation can improve here.
* The Eclipse compiler has always suffered from occasional **inconsistencies with Javac** which end-users fail at understanding. Sometimes, ECJ is right, sometimes Javac is; but for end-users and for the ecosystem, Javac is the reference implementation and it's behavior is what they perceive as the actual specification
* JDT has a very strong ecosystem (JDT-LS, plugins) a tons of nice features, so it seems profitable to **keep relying higher-level JDT APIs, such as model or DOM** to remain compatible with the ecosystem

🎯 The technical proposal here mostly to **allow Javac to be used at the lowest-level of JDT**, under the hood, to populate higher-level models that are used in many operations; named the JDT DOM and IJavaElement models. It is expected that if we can create a good DOM and IJavaElement structure with another strategy (eg using Javac API), then all higher level operations will remain working as well without modification.

## 📥 Install on top of existing JDT in your Eclipse IDE

Using the _Install New Software_ dialog and pointing to https://ci.eclipse.org/ls/job/jdt-core-incubator/job/dom-with-javac/lastSuccessfulBuild/artifact/repository/target/repository/ p2 repository,
install the _Javac backend for JDT_ artifact. Installing it will also tweak your `eclipse.ini` file to add the relevant options.

Note that some required feature switches are not yet available in upstream JDT (see above) and thus will still default to ECJ.

## ⌨️ Development

### ⌨️ Contribute

From a PDE-able IDE using a target platform that is suitable for JDT development (usually default case).

You'll need to import the code of `org.eclipse.jdt.core` and `org.eclipse.jdt.core.javac` from this branch in your Eclipse workspace; and create a Launch Configuration of type "Eclipse Application" which does include the `org.eclipse.jdt.core` bundle. Go to _Arguments_ tab of this launch configuration, and add the following content to the _VM arguments_ list:

* `-DCompilationUnit.DOM_BASED_OPERATIONS=true -DCompilationUnit.codeComplete.DOM_BASED_OPERATIONS=true -DSourceIndexer.DOM_BASED_INDEXER=true --add-opens jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED -DICompilationUnitResolver=org.eclipse.jdt.core.dom.JavacCompilationUnitResolver -DAbstractImageBuilder.compilerFactory=org.eclipse.jdt.internal.javac.JavacCompilerFactory`

* `CompilationUnit.DOM_BASED_OPERATIONS=true`/`CompilationUnit.codeComplete.DOM_BASED_OPERATIONS` / `SourceIndexer.DOM_BASED_INDEXER=true` system properties enables some operations to use build and DOM instead of ECJ Parser (so if DOM comes from Javac, ECJ parser is not involved at all)
* `--add-opens ...` as visible in the `org.eclipse.jdt.core.javac/META-INF/p2.inf` file to allow to access internal API of the JVM, including Javac ones
* `ICompilationUnitResolver=org.eclipse.jdt.core.dom.JavacCompilationUnitResolver` system property enables using Javac instead of ECJ to create JDT DOM AST.
* `AbstractImageBuilder.compilerFactory=org.eclipse.jdt.internal.javac.JavacCompilerFactory` system property instruct the builder to use Javac instead of ECJ to generate the .class file during build.

Those properties can be set separately, which can useful when developing one particular aspect of this proposal, which property to set depends on what you want to focus on.

### 📈 Progress

* Refactorings in upstream JDT got merged in order to allow alternative (non-ECJ strategies) for
  * reconciling model/errors using DOM ✔DONE
  * generating DOM from alternative parser ✔DONE
  * codeSelect ✔DONE
  * indexing ✔DONE
  * completion ⌨️IN PROGRESS
  * search/match ⌨️IN PROGRESS
  * compiler ✔DONE
* Javac backend for
  * producing Javac AST & Symbols ✔DONE (incl. annotation processing)
  * Javac->JDT AST conversion ✔DONE (incl. annotation processing)
  * binding conversion ✔DONE
  * JavacCompiler ✔DONE


🤔 What are the potential concerns:
* **Memory cost** of retaining Javac contexts needs to be evaluated (can we get rid of the context earlier? Can we share subparts of the concerns across multiple files in the project?...)
* It seems hard to find reusable parts from the **CompletionEngine**, although many proposals shouldn't really depend on the parser (so should be reusable)


😧 What are the confirmed concerns:
* **Null analysis** and some other **static analysis** are coded deep in ECJ and cannot be used with Javac. A solution can be to leverage another analysis engine (eg SpotBugs, SonarQube) deal with those features.
* At the moment, Javac cannot be configured to **generate .class despite CompilationError** in them like ECJ can do to allow updating the target application even when some code is not complete yet
  * We may actually be capable of hacking something like this in Eclipse/Javac integration (although it would be best to provide this directly in Javac), but running a 1st attempt of compilation, collecting errors, and then alterating the working copy of the source passed to Javac in case of error. More or less `if (diagnostics.anyMatch(getKind() == "error") { removeBrokenAST(diagnostic); injectAST("throw new CompilationError(diagnostic.getMessage()")`.
