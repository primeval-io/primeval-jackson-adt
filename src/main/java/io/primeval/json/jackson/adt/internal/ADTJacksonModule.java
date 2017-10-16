package io.primeval.json.jackson.adt.internal;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.deser.AbstractDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

import io.primeval.json.jackson.adt.CustomBeanSerializer;
import io.primeval.reflex.service.adt.ADTInfo;
import io.primeval.reflex.service.adt.ADTMirror;

@Component(service = Module.class)
public final class ADTJacksonModule extends SimpleModule {
    private ADTMirror adtMirror;

    @Reference
    public void setADTMirror(ADTMirror adtMirror) {
        this.adtMirror = adtMirror;
    }

    @Override
    public String getModuleName() {
        return "Algebraic DataType Jackson Module";
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addBeanSerializerModifier(new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc,
                    JsonSerializer<?> serializer) {
                Class<?> beanClass = beanDesc.getBeanClass();
                ADTInfo adtInfo = adtMirror.getInfo(beanClass).orElse(null);
                if (beanClass != null && adtInfo != null) {
                    CustomBeanSerializer customBeanSerializer = null;
                    if (serializer instanceof CustomBeanSerializer) {
                        customBeanSerializer = (CustomBeanSerializer) serializer;
                    } else if (serializer instanceof BeanSerializerBase) {
                        customBeanSerializer = CustomBeanSerializer.base((BeanSerializerBase) serializer);
                    }
                    if (customBeanSerializer != null) {
                        return new ADTSerializer(customBeanSerializer.getBase(), customBeanSerializer, adtInfo,
                                beanDesc.getBeanClass());
                    }
                }
                return serializer;
            }
        });

        context.addBeanDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            // This one for abstract sealed classes
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
                    JsonDeserializer<?> deserializer) {
                Class<?> beanClass = beanDesc.getBeanClass();
                ADTInfo adtInfo = adtMirror.getInfo(beanClass).orElse(null);

                if (beanClass != null && adtInfo != null) {
                    if (deserializer instanceof AbstractDeserializer) {
                        return new ADTDeserializer(beanDesc, adtInfo);
                    }
                }
                return deserializer;
            }

            @Override
            public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config,
                    BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs) {
                ADTInfo adtInfo = adtMirror.getInfo(beanDesc.getBeanClass()).orElse(null);

                if (adtInfo != null && Modifier.isAbstract(beanDesc.getBeanClass().getModifiers())) {
                    // If sealed abstract, completely ignore creator properties
                    // until we know the concrete type (otherwise it will fail)
                    // Actual deserialisation will be re-routed to concrete type
                    // anyway!
                    return Collections.emptyList();
                }
                return propDefs;
            }

            @Override
            // This one just tells the concrete sealed class the type property
            // can be ignored
            public BeanDeserializerBuilder updateBuilder(DeserializationConfig config, BeanDescription beanDesc,
                    BeanDeserializerBuilder builder) {
                ADTInfo adtInfo = adtMirror.getInfo(beanDesc.getBeanClass()).orElse(null);

                if (adtInfo != null && Modifier.isFinal(beanDesc.getBeanClass().getModifiers())) {
                    builder.addIgnorable(adtInfo.selectorName());
                }
                return builder;
            }
        });

    }

}
