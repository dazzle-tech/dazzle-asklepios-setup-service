package com.dazzle.asklepios.domain.childgrowth;

import com.dazzle.asklepios.domain.enumeration.ChildGrowthAlertRecipient;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Accepts the recipient list, and the earlier single sentence such as "Physician + task".
 */
public class ChildGrowthAlertedDeserializer extends JsonDeserializer<List<ChildGrowthAlertRecipient>> {

    @Override
    public List<ChildGrowthAlertRecipient> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        if (node == null || node.isNull()) {
            return new ArrayList<>();
        }
        if (node.isTextual()) {
            return new ArrayList<>(ChildGrowthAlertRecipient.fromLegacyLabel(node.asText()));
        }
        if (!node.isArray()) {
            throw new IllegalArgumentException("Who is alerted must be a list of recipients");
        }
        List<ChildGrowthAlertRecipient> recipients = new ArrayList<>();
        for (JsonNode item : node) {
            if (item == null || item.isNull() || !item.isTextual()) {
                throw new IllegalArgumentException("Who is alerted must be a list of recipients");
            }
            ChildGrowthAlertRecipient recipient = ChildGrowthAlertRecipient.fromValue(item.asText());
            if (recipient == null) {
                throw new IllegalArgumentException("Who is alerted must be a list of recipients");
            }
            recipients.add(recipient);
        }
        return recipients;
    }
}
