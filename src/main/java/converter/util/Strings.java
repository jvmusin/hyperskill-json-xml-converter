package converter.util;

public class Strings {
    public static String emptyIfNull(String s) {
        return s == null ? "" : s;
    }

    public static String quoted(String s) {
        return s == null ? null : '"' + s + '"';
    }
}
