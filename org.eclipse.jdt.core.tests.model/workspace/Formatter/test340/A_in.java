/**
 * Braces
 */
class Example {
    SomeClass fField = new SomeClass() {
        public int value;
    };
    void bar(int p) {
        for (int i = 0; i < 10; i++) {
            fField.add(i);
        }
        switch (p) {
            case 0 :
                fField.set(0);
                break;
            }
        }
}