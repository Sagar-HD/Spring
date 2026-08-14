package com.sagar.OrmMapping.dto.convertors;

import jakarta.persistence.AttributeConverter;

public class FeesConvertor implements AttributeConverter<Boolean, String> {
    @Override
    public String convertToDatabaseColumn(Boolean aBoolean) {
        if(aBoolean == null) return null;
        return aBoolean ? "PAID" : "NOT PAID";
    }

    @Override
    public Boolean convertToEntityAttribute(String s) {
        if(s==null) return null;
        return "PAID".equals(s);
    }
}
