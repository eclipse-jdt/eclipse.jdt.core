			if (scope.isJavaLangCloneable(expressionType) || scope.isJavaIoSerializable(expressionType))
				//potential runtime error
				{
				return true;
			}