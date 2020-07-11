package converter.base;

import java.util.List;
import java.util.stream.Collectors;

public interface Formatter {
    String format(Element element);

    default String format(List<Element> elements) {
        return elements.stream()
                .map(this::format)
                .collect(Collectors.joining());
    }
}
