package ch.bfh.tracesentry.daemon.config;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        Converter<LocalDateTime, Timestamp> toTimestampConverter =
                ctx -> ctx.getSource() == null ? null : Timestamp.valueOf(ctx.getSource());
        Converter<Timestamp, LocalDateTime> toLocalDateTimeConverter =
                ctx -> ctx.getSource() == null ? null : ctx.getSource().toLocalDateTime();
        modelMapper.addConverter(toTimestampConverter, LocalDateTime.class, Timestamp.class);
        modelMapper.addConverter(toLocalDateTimeConverter, Timestamp.class, LocalDateTime.class);
        return modelMapper;
    }
}