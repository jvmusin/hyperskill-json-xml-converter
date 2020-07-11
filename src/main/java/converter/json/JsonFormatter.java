package converter.json;

import converter.base.Element;
import converter.base.Formatter;

import java.util.stream.Collectors;

import static converter.util.Strings.quoted;

public class JsonFormatter implements Formatter {
    @Override
    public String format(Element element) {
        return format(element, true, true);
    }

    private String format(Element element, boolean inBraces, boolean includeName) {
        StringBuilder res = new StringBuilder();
        if (inBraces) res.append('{');

        if (includeName) {
            res.append(quoted(element.name())).append(':');
        }

        if (element.isArray()) {
            res.append(formatArray(element));
        } else if (element.getAttributes().isEmpty()) {
            res.append(formatValue(element));
        } else {
            String attrs = element.getAttributes().stream()
                    .map(a -> quoted("@" + a.getName()) + ":" + quoted(a.getValue()) + ",")
                    .collect(Collectors.joining());
            attrs += quoted("#" + element.name()) + ":" + formatValue(element);
            res.append('{').append(attrs).append('}');
        }

        if (inBraces) res.append('}');
        return res.toString();
    }

    private String formatArray(Element element) {
        return element.getChildren().stream()
                .map(e -> format(e, false, false))
                .collect(Collectors.joining(",", "[", "]"));
    }

    private String formatValue(Element element) {
        if (element.getChildren() == null) {
            return quoted(element.getSingleValue());
        }
        if (element.isContentArray()) {
            return formatArray(element);
        }

        return element.getChildren().stream()
                .map(e -> format(e, false, true))
                .collect(Collectors.joining(",", "{", "}"));
    }
}
