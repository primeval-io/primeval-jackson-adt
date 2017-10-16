package io.primeval.json.jackson.adt;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

public interface CustomBeanSerializer {
    static CustomBeanSerializerBase base(BeanSerializerBase src) {
        return new CustomBeanSerializerBase(src);
    }

    void serializeSelfFields(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException;

    BeanSerializerBase getBase();

}
