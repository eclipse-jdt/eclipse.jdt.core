/**
 * This is a sample class, to demonstrate the problems
 * with the Eclipse formatter.
 * @author Adalbert Homa
 * @version 1.0
 */
public class FormatTest2 {

    public void testMethod() {
        for (int i = 0; i < 10; i++) {
            // Next line show the problem
                AccountAccessGroupBean aags = new AccountAccessGroupBean(//
        "a", // groupId
        "b", // groupName 
        "c", // lastModified
        "d" // modifiedFlag
    );
            // The second line is without end of line comments
            AccountAccessGroupBean b =
                new AccountAccessGroupBean("a", "b", "c", "d");
        }
    }

    private static class AccountAccessGroupBean {
        AccountAccessGroupBean(String a, String b, String c, String d) {
        }
    }
}