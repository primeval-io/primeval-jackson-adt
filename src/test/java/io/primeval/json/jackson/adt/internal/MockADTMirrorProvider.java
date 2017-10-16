package io.primeval.json.jackson.adt.internal;

import java.util.function.Function;

import io.primeval.reflex.service.adt.ADTInfo;
import io.primeval.reflex.service.adt.spi.ADTMirrorProvider;

public class MockADTMirrorProvider implements ADTMirrorProvider {

    @Override
    public boolean isADT(Class<?> raw) {
        return (raw == UnionLike.class || raw == D1.class || raw == D2.class);
    }

    @Override
    public ADTInfo getInfo(Class<?> raw, Function<Class<?>, ADTInfo> callback) {
        return new ADTInfo() {

            @Override
            public String typeName(Class<?> clazz) {
                if (clazz == D1.class) {
                    return "one";
                } else if (clazz == D2.class) {
                    return "two";
                }
                throw new IllegalStateException("union subclass for UnionLike!");
            }

            @Override
            public Class<?> rootType() {
                return UnionLike.class;
            }

            @Override
            public Class<?> classFor(String typeName) {
                switch (typeName) {
                case "one":
                    return D1.class;
                case "two":
                    return D2.class;
                }
                throw new IllegalStateException("unknown typename for UnionLike!");
            }

            @Override
            public String selectorName() {
                return "kind";
            }
        };
    }
}
