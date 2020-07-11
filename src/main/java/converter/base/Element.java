package converter.base;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static converter.util.Lists.last;

public class Element {
    private final List<Attribute> attributes;
    private final List<Element> children;
    private final String singleValue;
    private final List<String> path;

    public Element(@NotNull List<Attribute> attributes, List<Element> children, String singleValue, List<String> path) {
        this.attributes = attributes;
        this.children = children;
        this.singleValue = singleValue;
        this.path = path;
    }

    public static Element singleElement(@NotNull List<Attribute> attributes, String value, List<String> path) {
        return new Element(attributes, null, value, path);
    }

    public static Element withChildren(@NotNull List<Attribute> attributes, List<Element> children, List<String> path) {
        return new Element(attributes, children, null, path);
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public List<Element> getChildren() {
        return children;
    }

    public String getSingleValue() {
        return singleValue;
    }

    public boolean isArray() {
        return attributes.isEmpty() && isContentArray();
    }

    public boolean isContentArray() {
        return children != null &&
                children.size() >= 2 &&
                children.stream().map(Element::name).distinct().count() == 1;
    }

    public String name() {
        return last(path);
    }
}
