package a5;
/* Test case for bug 6538 searchDeclarationsOf* incorrect */

class A{
        int i(){
                return 0;
        }
}

class B {
        int f;
        void i(){
                y();
                Object l= new byte[f];
        }
        private void y(){
        }

}

class C extends A{
        public  int i(){
                return 0;
        }
}