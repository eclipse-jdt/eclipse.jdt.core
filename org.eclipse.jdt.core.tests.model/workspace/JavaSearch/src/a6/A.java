package a6;

class A {
}

class B extends A {
        private int f;
        private class P{}
        private void a(){}
        void m() { 
                f++;
                P p= new P();
                a();
        }       
}