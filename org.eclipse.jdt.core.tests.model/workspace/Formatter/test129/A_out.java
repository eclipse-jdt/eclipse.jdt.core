String qualifiedPackageName = qualifiedTypeName.length() == typeName.length
		? "" //$NON-NLS-1$
		: qualifiedBinaryFileName.substring(0,
				qualifiedTypeName.length() - typeName.length - 1);
