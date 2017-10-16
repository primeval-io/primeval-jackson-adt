package io.primeval.json.jackson.adt.internal;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.impl.BeanAsArraySerializer;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.util.NameTransformer;

import io.primeval.json.jackson.adt.CustomBeanSerializer;
import io.primeval.reflex.service.adt.ADTInfo;

public class ADTSerializer extends BeanSerializerBase implements CustomBeanSerializer {

    private ADTInfo adtInfo;
    private CustomBeanSerializer delegate;
    private boolean needWrapping = true;
    private Class<?> rootType;

    ADTSerializer(BeanSerializerBase source, CustomBeanSerializer delegate, ADTInfo adtInfo, Class<?> rootType) {
        super(source);
        this.delegate = delegate;
        this.adtInfo = adtInfo;
        this.rootType = rootType;
    }

    ADTSerializer(ADTSerializer delegate, ObjectIdWriter objectIdWriter) {
        super(delegate, objectIdWriter);
    }

    @Deprecated
    ADTSerializer(ADTSerializer delegate, String[] toIgnore) {
        super(delegate, toIgnore);
    }

    ADTSerializer(ADTSerializer delegate, Set<String> toIgnore) {
        super(delegate, toIgnore);
    }

    public ADTSerializer(ADTSerializer extraFieldSerializer, ObjectIdWriter _objectIdWriter,
            Object filterId, ADTInfo adtInfo, Class<?> rootType) {
        super(extraFieldSerializer, _objectIdWriter, filterId);
        this.adtInfo = adtInfo;
        this.rootType = rootType;
    }

    public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
        return new ADTSerializer(this, objectIdWriter);
    }

    protected BeanSerializerBase withIgnorals(String[] toIgnore) {
        return new ADTSerializer(this, toIgnore);
    }

    @Override
    protected BeanSerializerBase withIgnorals(Set<String> toIgnore) {
        return new ADTSerializer(this, toIgnore);
    }

    public ADTSerializer(ADTSerializer metaSerializers, boolean needWrapping) {
        super(metaSerializers);
        this.adtInfo = metaSerializers.adtInfo;
        this.delegate = metaSerializers.delegate;
        this.rootType = metaSerializers.rootType;
        this.needWrapping = needWrapping;
    }

    @Override
    public JsonSerializer<Object> unwrappingSerializer(NameTransformer unwrapper) {
        return new ADTSerializer(this, false);
    }

    @Override
    public void serialize(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (needWrapping) {
            jgen.writeStartObject();
        }
        internalSerializeFields(bean, jgen, provider);
        if (needWrapping) {
            jgen.writeEndObject();
        }
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
        return new ADTSerializer(this, _objectIdWriter, filterId, adtInfo, rootType);
    }

    @Override
    protected void serializeFieldsFiltered(Object bean, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        internalSerializeFields(bean, jgen, provider);
    }

    @Override
    protected void serializeFields(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        internalSerializeFields(bean, jgen, provider);
    }

    @Override
    public void serializeSelfFields(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        internalSerializeFields(bean, jgen, provider);
    }

    private void internalSerializeFields(Object bean, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        if (Modifier.isAbstract(rootType.getModifiers())) {
            JsonSerializer<Object> serializer = provider.findTypedValueSerializer(bean.getClass(), true, null);
            CustomBeanSerializer customBeanSerializer = (CustomBeanSerializer) serializer;
            customBeanSerializer.serializeSelfFields(bean, jgen, provider);
        } else {
            jgen.writeStringField(adtInfo.selectorName(), adtInfo.typeName(bean.getClass()));
            delegate.serializeSelfFields(bean, jgen, provider);
        }
    }

    @Override
    public BeanSerializerBase getBase() {
        return this;
    }

}
