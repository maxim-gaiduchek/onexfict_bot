package datasource.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Converter
public class StringToStringList implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> strings) {
        return String.join(",", strings);
    }

    @Override
    public List<String> convertToEntityAttribute(String s) {
        if (s == null || s.equals("")) return new ArrayList<>();

        return new ArrayList<>(Arrays.stream(s.split(",")).toList());
    }
}
