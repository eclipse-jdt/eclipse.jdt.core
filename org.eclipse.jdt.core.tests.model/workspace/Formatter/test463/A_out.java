protected String createWhereClause() {
	whereClause
			.append(getColumnSQL(FNS_NUMBER))
			.append(getColumnSQLWidthLikeOrEquals(MEAL_SERVICE_NAME))
			.append(getColumnSQL(MEAL_SERVICE_STATUS))
			.append(getColumnSQL(MEAL_SERVICE_TYPE))
			.append(getColumnSQLWithBetween(APPLICATION_REQUEST_BEGIN_DATE))
			.append(getColumnSQL(MAILING_ADDRESS_ID))
			.append(getColumnSQL(MEAL_SITE_ENTITY_ID))
			.append(getColumnSQLWithInClause(FIELD_OFFICES))
			.append(getColumnSQL(EIN))
			.append(getColumnSQL(PERSON_ROLE_PERSON_ID))
			.append(getColumnSQL(PERSON_ROLE_ID))
			.append(getColumnSQL(REGION_ID));
	return whereClause.toString();
}