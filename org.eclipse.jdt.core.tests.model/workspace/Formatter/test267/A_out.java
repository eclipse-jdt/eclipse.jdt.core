currentObject = new MethodDeclaration(flags & getModifiers,
		modifiersEndPosition, typeName, aName, identifierStartPosition,
		parameterList, parametersPositions, throwList, throwsPositions,
		scanner.deprecatedPtr > -1);