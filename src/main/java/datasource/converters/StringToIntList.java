package datasource.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class StringToIntList implements AttributeConverter<List<Integer>, String> {

    @Override
    public String convertToDatabaseColumn(List<Integer> integers) {
        return integers.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    @Override
    public List<Integer> convertToEntityAttribute(String s) {
        if (s == null || s.equals("")) return new ArrayList<>();

        return new ArrayList<>(Arrays.stream(s.split(",")).map(Integer::parseInt).toList());
    }
}
