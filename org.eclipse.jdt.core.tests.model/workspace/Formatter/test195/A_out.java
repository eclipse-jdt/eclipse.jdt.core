public void setFieldObject(Object anObject, PreparedStatement aStatement,
		int anIndex) throws SQLException {
	if (getSQLType() == Types.BINARY || getSQLType() == Types.VARBINARY)
		aStatement.setBytes(anIndex, (byte[]) anObject);
	else
		aStatement.setObject(anIndex, anObject, getSQLType());
}