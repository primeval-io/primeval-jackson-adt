package io.primeval.json.jackson.adt;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.impl.BeanAsArraySerializer;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

public class CustomBeanSerializerBase extends BeanSerializerBase implements CustomBeanSerializer {

    protected CustomBeanSerializerBase(BeanSerializerBase src) {
        super(src);
    }

    CustomBeanSerializerBase(CustomBeanSerializerBase delegate, ObjectIdWriter objectIdWriter) {
        super(delegate, objectIdWriter);
    }

    @Deprecated
    CustomBeanSerializerBase(CustomBeanSerializerBase delegate, String[] toIgnore) {
        super(delegate, toIgnore);
    }
    
    CustomBeanSerializerBase(CustomBeanSerializerBase delegate, Set<String> toIgnore) {
        super(delegate, toIgnore);
    }

    public CustomBeanSerializerBase(CustomBeanSerializerBase extraFieldSerializer, ObjectIdWriter _objectIdWriter,
                                    Object filterId) {
        super(extraFieldSerializer, _objectIdWriter, filterId);
    }

    @Override
    public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
        return new CustomBeanSerializerBase(this, objectIdWriter);
    }

    @Override
    protected BeanSerializerBase withIgnorals(String[] toIgnore) {
        return new CustomBeanSerializerBase(this, toIgnore);
    }

    @Override
    public void serialize(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();

        if (provider.getFilterProvider() != null && provider.getFilterProvider().findPropertyFilter("propertyfilter", bean) != null) {
            serializeFieldsFiltered(bean, jgen, provider);
        } else {
            serializeFields(bean, jgen, provider);
        }

        jgen.writeEndObject();
    }

    @Override
    protected BeanSerializerBase asArraySerializer() {
        /*
         * Can not:
         * 
         * - have Object Id (may be allowed in future) - have any getter
         */
        if ((_objectIdWriter == null) && (_anyGetterWriter == null) && (_propertyFilterId == null)) {
            return new BeanAsArraySerializer(this);
        }
        // already is one, so:
        return this;
    }

    @Override
    public BeanSerializerBase withFilterId(Object filterId) {
        return new CustomBeanSerializerBase(this, _objectIdWriter, filterId);
    }

    @Override
    public void serializeSelfFields(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (provider.getFilterProvider() != null && provider.getFilterProvider().findPropertyFilter("propertyfilter", bean) != null) {
            serializeFieldsFiltered(bean, jgen, provider);
        } else {
            serializeFields(bean, jgen, provider);
        }
    }

    @Override
    public BeanSerializerBase getBase() {
        return this;
    }

    @Override
    protected BeanSerializerBase withIgnorals(Set<String> ignorals) {
        return new CustomBeanSerializerBase(this, ignorals);
    }

}
