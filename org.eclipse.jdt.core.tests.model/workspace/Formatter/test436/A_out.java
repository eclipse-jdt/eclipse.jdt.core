public class A
{

    public Object getColumnDataCell(int col)
    {
        Object obj = "";
        switch (col) {
        case 0:
            obj = getId();
            break;
        case 1:
            obj = getDistrict();
            break;
        case 2:
            obj = getPack();
            break;
        case 3:
            obj = getDen();
            break;
        case 4:
            obj = getLastName();
            break;
        case 5:
            obj = getFirstName();
            break;
        case 6:
            obj = getVehicleNumber();
            break;
        default:
            break;
        }
        return obj;
    }
}