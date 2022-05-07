package bot.datasource.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class StringToLongList implements AttributeConverter<List<Long>, String> {

    @Override
    public String convertToDatabaseColumn(List<Long> integers) {
        return integers.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    @Override
    public List<Long> convertToEntityAttribute(String s) {
        if (s == null || s.equals("")) return new ArrayList<>();

        return new ArrayList<>(Arrays.stream(s.split(",")).map(Long::parseLong).toList());
    }
}
