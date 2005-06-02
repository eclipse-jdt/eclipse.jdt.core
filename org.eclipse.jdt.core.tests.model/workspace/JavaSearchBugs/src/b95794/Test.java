package b95794;
import static b95794.Test.Color.WHITE;
import static b95794.Test.Color.BLACK;
public class Test {
    enum Color {WHITE, BLACK}
    Test there;
    public static void main(String[] args) {
        Color c = BLACK;
        switch(c) {
        case BLACK:
            break;
        case WHITE:
            break;
        }
    }
}