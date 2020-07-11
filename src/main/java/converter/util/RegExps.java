package converter.util;

import org.intellij.lang.annotations.Language;

import java.util.regex.Pattern;

public class RegExps {
    public static String spaceToMultispace(String s) {
        return s.replace(" ", "\\s*");
    }

    public static Pattern compileMultispace(@Language("RegExp") String regex) {
        return Pattern.compile(spaceToMultispace(" " + regex + " "));
    }
}
