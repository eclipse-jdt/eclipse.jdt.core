switch (VARIABLE)
{
    case CASE_1 :
	case CASE_3 :    	
        _foo= false;
    doSomething1();
    doSomething2();
    doSomething3();
    default :
        _foo= true;
    doSomething4();
        break;
}