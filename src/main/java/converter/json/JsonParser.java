package converter.json;

import converter.base.Attribute;
import converter.base.Element;
import converter.base.Parser;
import converter.util.CharQueue;
import converter.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class JsonParser implements Parser {

    private final Predicate<Field> isSpecial = f -> f.name.matches("([#@]).*");
    private final CharQueue q = new CharQueue();

    @Override
    public List<Element> parse(String s) {
        System.err.println(s);
        q.init(s);
        Node node = readNode();
        if (node == null) return null;
        return node.fields.stream()
                .map(f -> fix(f, new ArrayList<>()))
                .collect(Collectors.toList());
    }

    private String readStringLiteral() {
        StringBuilder value = new StringBuilder();
        q.poll('"');
        while (q.peek() != '"') value.append(q.poll());
        q.poll('"');
        return value.toString();
    }

    private String readNumberLiteral() {
        StringBuilder value = new StringBuilder();
        while (Character.isDigit(q.peek()) || q.peek() == '.') {
            value.append(q.poll());
        }
        return value.toString();
    }

    private String readKeywordLiteral() {
        switch (q.peek()) {
            case 't':
                q.poll("true");
                return "true";
            case 'f':
                q.poll("false");
                return "false";
            case 'n':
                q.poll("null");
                return null;
            default:
                throw new IllegalStateException("Not a keyword");
        }
    }

    private String readLiteral() {
        if (q.peek() == '"') {
            return readStringLiteral();
        }
        if (Character.isDigit(q.peek())) {
            return readNumberLiteral();
        }
        if ("tfn".indexOf(q.peek()) != -1) {
            return readKeywordLiteral();
        }
        return null;
    }

    private boolean isSingleValue(Field field) {
        Field value = null;
        for (Field subField : field.value.fields) {
            if (subField.name.startsWith("@")) { // found attribute
                if (subField.name.length() == 1 || subField.value != null) {
                    return false;
                }
            } else if (subField.name.equals("#" + field.name)) { // found value
                if (value != null) {
                    return false;
                }
                value = subField;
            } else { // not an attribute nor value
                return false;
            }
        }
        return value != null;
    }

    private Element fixSingleElement(Field field, List<String> path) {
        List<Attribute> attributes = field.value.fields.stream()
                .filter(f -> f.name.startsWith("@"))
                .map(f -> new Attribute(f.name.substring(1), Strings.emptyIfNull(f.literalValue)))
                .collect(Collectors.toList());

        Field value = field.value.fields.stream()
                .filter(f -> f.name.startsWith("#"))
                .findAny().orElseThrow();

        if (value.value == null) {
            return Element.singleElement(attributes, value.literalValue, new ArrayList<>(path));
        }

        List<Element> children = value.value.fields.stream()
                .map(f -> fix(f, new ArrayList<>(path)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return Element.withChildren(attributes, children, path);
    }

    private Element fixComplexElement(Field field, List<String> path) {
        List<Element> children = field.value.fields.stream()
                .filter(f -> !isSpecial.test(f) || field.value.fields.stream().noneMatch(f1 -> f1.name.equals(f.name.substring(1))))
                .map(f -> isSpecial.test(f) ? new Field(f.name.substring(1), f.value, f.literalValue) : f)
                .filter(f -> !f.name.isBlank())
                .map(f -> fix(f, new ArrayList<>(path)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (children.isEmpty()) {
            return Element.singleElement(emptyList(), "", path);
        }

        return Element.withChildren(emptyList(), children, path);
    }

    private Element fix(Field field, List<String> path) {
        if (field.name.isBlank()) return null;

        path.add(field.name);

        if (field.value == null) {
            return Element.singleElement(emptyList(), field.literalValue, path);
        }

        if (isSingleValue(field)) {
            return fixSingleElement(field, path);
        }

        return fixComplexElement(field, path);
    }

    private Node readNode() {
        char start = q.peek();
        if (start != '{' && start != '[') return null;
        q.poll(start);

        boolean isArray = start == '[';

        List<Field> fields = new ArrayList<>();
        while (true) {
            String name;
            if (isArray) {
                if (q.peek() == ']') break;
                name = "element";
            } else {
                name = readLiteral();
                if (name == null) break;
                q.poll(':');
            }
            Node nodeValue = readNode();
            String literalValue = nodeValue == null ? readLiteral() : null;

            if (nodeValue != null && nodeValue.fields.isEmpty()) {
                nodeValue = null;
                literalValue = "";
            }

            fields.add(new Field(name, nodeValue, literalValue));
            if (q.peek() == ',') q.poll();
            else break;
        }

        q.poll(start == '{' ? '}' : ']');
        return new Node(fields);
    }

    private static class Node {
        final List<Field> fields;

        Node(List<Field> fields) {
            this.fields = fields;
        }
    }

    private static class Field {
        final String name;
        final Node value;
        final String literalValue;

        Field(String name, Node value, String literalValue) {
            this.name = name;
            this.value = value;
            this.literalValue = literalValue;
        }
    }
}
