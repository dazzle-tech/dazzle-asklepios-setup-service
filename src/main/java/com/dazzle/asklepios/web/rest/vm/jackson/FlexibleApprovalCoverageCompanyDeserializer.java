package com.dazzle.asklepios.web.rest.vm.jackson;

import com.dazzle.asklepios.domain.enumeration.ApprovalCoverageCompany;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Treats blank strings as null so optional enum fields do not fail JSON parsing.
 */
public class FlexibleApprovalCoverageCompanyDeserializer extends JsonDeserializer<ApprovalCoverageCompany> {

    @Override
    public ApprovalCoverageCompany deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        String text = parser.getValueAsString();
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return ApprovalCoverageCompany.valueOf(text.trim());
        } catch (IllegalArgumentException ex) {
            return (ApprovalCoverageCompany) context.handleWeirdStringValue(
                    ApprovalCoverageCompany.class,
                    text,
                    "not a valid ApprovalCoverageCompany"
            );
        }
    }
}
