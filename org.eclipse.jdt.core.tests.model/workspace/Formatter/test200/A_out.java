if (((c1 = Character.getNumericValue(source[currentPosition++])) > 15 || c1 < 0)
		|| ((c2 = Character.getNumericValue(source[currentPosition++])) > 15 || c2 < 0)
		|| ((c3 = Character.getNumericValue(source[currentPosition++])) > 15 || c3 < 0)
		|| ((c4 = Character.getNumericValue(source[currentPosition++])) > 15 || c4 < 0))
{
	currentPosition = temp;
	return 2;
}
