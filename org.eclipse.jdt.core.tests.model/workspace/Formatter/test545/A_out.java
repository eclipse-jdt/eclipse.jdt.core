import java.util.List;

public class Bug {
    void case1(List<Object>... lists) {
        int notFormatted;
    }

    void case2(List... lists) {
        int notFormatted;
    }

    void case3(List<Object> lists) {
        int notFormatted;
    }

    void case4(java.lang.List<Object>... lists) {
        int notFormatted;
    }
}
