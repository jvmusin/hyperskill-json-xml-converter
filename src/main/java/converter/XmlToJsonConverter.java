package converter;

import converter.base.AbstractConverter;
import converter.json.JsonFormatter;
import converter.xml.XmlParser;

public class XmlToJsonConverter extends AbstractConverter {
    public XmlToJsonConverter() {
        super(new XmlParser(), new JsonFormatter());
    }
}
