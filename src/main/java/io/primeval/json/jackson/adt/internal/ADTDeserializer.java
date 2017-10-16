package io.primeval.json.jackson.adt.internal;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.node.TextNode;

import io.primeval.reflex.service.adt.ADTInfo;

public class ADTDeserializer extends JsonDeserializer<Object> {

    private final BeanDescription beanDesc;
    private final ADTInfo adtInfo;

    public ADTDeserializer(BeanDescription beanDesc, ADTInfo adtInfo) {
        super();
        this.beanDesc = beanDesc;
        this.adtInfo = adtInfo;
    }

    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        Class<?> rawClass = beanDesc.getBeanClass();

        // The type property can be anywhere, we have to load the json in-memory
        // and look for it first before we can determine the concrete type to
        // deserialise
        TreeNode tree = jp.readValueAsTree();

        TextNode typeNode = (TextNode) tree.get(adtInfo.selectorName());
        if (typeNode != null) {
            String className = typeNode.asText();

            Class<?> clazz = adtInfo.classFor(className);
            if (clazz == null) {
                throw new JsonMappingException(jp, "Unknown type " + className);
            }

            if (rawClass != clazz && !Modifier.isAbstract(clazz.getModifiers()) && rawClass.isAssignableFrom(clazz)) {
                // With concrete type the "type" property is dynamically
                // marked as ignorable in the ADTJacksonModule, so we don't have
                // to weed it out

                return readTreeForType(jp, ctxt, ctxt.getTypeFactory().constructSpecializedType(beanDesc.getType(), clazz), tree);
            }
        } // missing property "type"
        throw new JsonMappingException(jp, "Unknown type " + rawClass);
    }

    @SuppressWarnings("unchecked")
    private <T> T readTreeForType(JsonParser jp, DeserializationContext ctxt, JavaType paramType, TreeNode resourceNode)
            throws IOException {
        JsonParser parser = resourceNode.traverse(jp.getCodec());
        parser.nextToken();
        JsonDeserializer<Object> deser = deserCache.computeIfAbsent(paramType, k -> {
            try {
                return ctxt.findRootValueDeserializer(k);
            } catch (JsonMappingException e) {
                throw new IllegalStateException(e);
            }
        });
        return (T) deser.deserialize(parser, ctxt);
    }

    private final Map<JavaType, JsonDeserializer<Object>> deserCache = new ConcurrentHashMap<>();
}
