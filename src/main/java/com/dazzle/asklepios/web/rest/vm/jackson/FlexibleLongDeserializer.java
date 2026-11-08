package com.dazzle.asklepios.web.rest.vm.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Accepts a Long as a number, numeric string, or {@code {"id": 1}} / {@code {"value": 1}}.
 */
public class FlexibleLongDeserializer extends JsonDeserializer<Long> {

    @Override
    public Long deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        if (token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT) {
            return parser.getLongValue();
        }
        if (token == JsonToken.VALUE_STRING) {
            String text = parser.getText();
            if (text == null || text.isBlank()) {
                return null;
            }
            return Long.parseLong(text.trim());
        }
        if (token == JsonToken.START_OBJECT) {
            JsonNode node = parser.getCodec().readTree(parser);
            JsonNode idNode = node.get("id");
            if (idNode == null || idNode.isNull()) {
                idNode = node.get("value");
            }
            if (idNode == null || idNode.isNull() || idNode.isMissingNode()) {
                return null;
            }
            if (idNode.isNumber()) {
                return idNode.longValue();
            }
            if (idNode.isTextual() && !idNode.asText().isBlank()) {
                return Long.parseLong(idNode.asText().trim());
            }
            return null;
        }
        return (Long) context.handleUnexpectedToken(Long.class, parser);
    }
}
