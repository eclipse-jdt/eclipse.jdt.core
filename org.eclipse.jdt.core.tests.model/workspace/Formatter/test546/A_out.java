public class Bug {
    private static String buildStartTag(String namespace, String serviceName) {
        return " <ns1:"
                + serviceName
                + (" soapenv:encodingStyle=\"http://schemas.xmlsoap.org/"
                        + "soap/encoding/\" xmlns:ns1=\"") + namespace
                + "\">\n";
    }
}
