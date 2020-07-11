package converter.xml;

import converter.base.Attribute;
import converter.base.Element;
import converter.base.Parser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static converter.util.RegExps.compileMultispace;

public class XmlParser implements Parser {

    private static final Pattern ATTRIBUTE_PATTERN = compileMultispace("(?<name>\\w+) = ([\"'])(?<value>.*?)\\2");

    private static final Pattern TAG_PATTERN = compileMultispace("< (?<name>\\w+) (?<attributes>( \\w+ = ([\"']).*?\\4 )*?) (/>|>)");
    private Queue<String> tokens;

    private static List<Attribute> parseAttributes(String s) {
        return ATTRIBUTE_PATTERN.matcher(s).results()
                .map(r -> new Attribute(r.group(1), r.group(3)))
                .collect(Collectors.toList());
    }

    private static List<String> split(String s) {
        Pattern p = Pattern.compile("<.*?>");
        Matcher m = p.matcher(s);
        int lastEnd = 0;
        List<String> res = new ArrayList<>();
        while (m.find()) {
            res.add(s.substring(lastEnd, m.start()));
            res.add(s.substring(m.start(), m.end()));
            lastEnd = m.end();
        }
        return res.stream()
                .map(String::trim)
                .filter(str -> !str.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public List<Element> parse(String s) {
        tokens = new ArrayDeque<>(split(s));
        if (!tokens.isEmpty() && tokens.element().startsWith("<?")) {
            tokens.remove();
        }
        if (tokens.isEmpty() || !TAG_PATTERN.matcher(tokens.element()).matches()) {
            return null;
        }
        return List.of(parse(new ArrayList<>()));
    }

    private String parseLiteral() {
        if (!TAG_PATTERN.matcher(tokens.element()).find()) {
            return tokens.remove().trim();
        }
        return null;
    }

    private Element parse(List<String> path) {
        String rootTag = tokens.remove();
        boolean isSelfClosing = rootTag.endsWith("/>");

        Matcher matcher = TAG_PATTERN.matcher(rootTag);
        if (!matcher.find()) {
            return new Element(Collections.emptyList(), null, null, null);
        }

        String name = matcher.group("name");
        path.add(name);

        List<Attribute> attributes = parseAttributes(matcher.group("attributes"));

        if (isSelfClosing) {
            return new Element(attributes, null, null, path);
        }

        List<Element> children = new ArrayList<>();
        String value = null;
        while (!tokens.element().equals("</" + name + ">")) {
            String literal = parseLiteral();
            if (literal != null) {
                value = literal;
            } else {
                children.add(parse(new ArrayList<>(path)));
            }
        }
        tokens.poll();

        if (children.isEmpty()) {
            children = null;
            if (value == null) {
                value = "";
            }
        } else if (value != null) {
            throw new IllegalStateException("Found both children and a literal value");
        }

        return new Element(attributes, children, value, path);
    }
}
