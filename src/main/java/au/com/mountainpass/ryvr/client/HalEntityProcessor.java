package au.com.mountainpass.ryvr.client;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.NotImplementedException;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.inflector.converters.ConversionException;
import io.swagger.inflector.processors.EntityProcessor;

public class HalEntityProcessor implements EntityProcessor {

    public static final MediaType APPLICATION_HAL_TYPE = MediaType
            .valueOf("application/hal+json ");

    @Override
    public boolean supports(MediaType mediaType) {
        return MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)
                || APPLICATION_HAL_TYPE.isCompatible(mediaType);
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON_TYPE,
                APPLICATION_HAL_TYPE });
    }

    @Override
    public void enableType(MediaType type) {
        // do nothing
    }

    @Override
    public Object process(MediaType mediaType, InputStream entityStream,
            Class<?> cls) throws ConversionException {
        throw new NotImplementedException();
    }

    @Override
    public Object process(MediaType mediaType, InputStream entityStream,
            JavaType javaType) {
        throw new NotImplementedException();
    }

}
