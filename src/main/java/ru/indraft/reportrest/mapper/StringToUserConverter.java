package ru.indraft.reportrest.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.indraft.reportrest.model.UserModel;

@Component
public class StringToUserConverter implements Converter<String, UserModel> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public UserModel convert(String source) {
        return objectMapper.readValue(source, UserModel.class);
    }
}
