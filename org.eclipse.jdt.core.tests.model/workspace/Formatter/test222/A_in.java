private int getDivColumnNum(TableData t, String divId)
{
Vector v = t.getColNames();
for (int i = 0; i < v.size(); i++)
{
Object o = v.elementAt(i);
if (o instanceof Division)
{
Division div = (Division) o;
if (div.getId().equals(divId))
{
return i;
}
}
}
return -1;
}