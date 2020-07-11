package converter;

import converter.base.AbstractConverter;
import converter.json.JsonParser;
import converter.xml.XmlFormatter;

public class JsonToXmlConverter extends AbstractConverter {
    public JsonToXmlConverter() {
        super(new JsonParser(), new XmlFormatter());
    }
}
