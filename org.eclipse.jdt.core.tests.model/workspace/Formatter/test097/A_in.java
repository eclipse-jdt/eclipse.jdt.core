StringBuffer sql = new StringBuffer( ConstTypes.SOME_TYPE ) ;
sql
	.append( " WHERE " )
	.append( ConstAttrs.SOME_ATTR1 )
	.append( " = '" )
	.append( someValue1 )
	.append( "' AND" )
	.append( ConstAttrs.SOME_ATTR2 )
	.append( " = '" )
	.append( someValue2 )
	.append( "' AND " ) ;