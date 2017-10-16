package io.primeval.json.jackson.adt.internal;

import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.primeval.json.jackson.test.rules.WithJacksonMapper;
import io.primeval.reflex.service.adt.internal.ADTMirrorImpl;

public class ADTJacksonModuleTest {

    @ClassRule
    public static final WithJacksonMapper wJacksonMapper = new WithJacksonMapper(Collections.emptyList());

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static ObjectMapper mapper;

    @BeforeClass
    public static void setup() {
        ADTMirrorImpl adtMirrorImpl = new ADTMirrorImpl();
        adtMirrorImpl.addADTMirrorProvider(new MockADTMirrorProvider());

        ADTJacksonModule adtJacksonModule = new ADTJacksonModule();
        adtJacksonModule.setADTMirror(adtMirrorImpl);
        wJacksonMapper.getJacksonMapper().addModule(adtJacksonModule);
        mapper = wJacksonMapper.getJacksonMapper().objectMapper();
    }

    @Test
    public void shouldSerializeADTsWithTypeProperty() throws Exception {
        UnionLike obj = new D1("bar");
        String actual = mapper.writeValueAsString(obj);
        String json = "{\"kind\":\"one\",\"bar\":\"bar\"}";

        Assertions.assertThat(actual).isEqualTo(json);
    }

    @Test
    public void shouldDeserializeADTsWithTypeProperty() throws Exception {
        String json = "{\"kind\":\"one\",\"bar\":\"bar\"}";
        UnionLike actual = mapper.readValue(json, UnionLike.class);
        UnionLike expected = new D1("bar");
        Assertions.assertThat(actual).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void shouldDeserializeADTsWithTypePropertyWithConcreteType() throws Exception {
        String json = "{\"kind\":\"one\",\"bar\":\"bar\"}";
        D1 actual = mapper.readValue(json, D1.class);
        D1 expected = new D1("bar");
        Assertions.assertThat(actual).isEqualToComparingFieldByField(expected);
    }
    
    @Test
    public void shouldDeserializeConcreteTypesWithoutTypeProperty() throws Exception {
        String json = "{\"bar\":\"bar\"}";
        D1 actual = mapper.readValue(json, D1.class);
        D1 expected = new D1("bar");
        Assertions.assertThat(actual).isEqualToComparingFieldByField(expected);
    }


    // @Test
    // public void genericSealedTypeDeserializerShouldBeOverridable() throws
    // Exception {
    // LikeAKPIJacksonModule likeAKPIJacksonModule = new
    // LikeAKPIJacksonModule();
    // SealedTypeJacksonModule sealedTypeJacksonModule = new
    // SealedTypeJacksonModule();
    // mapper.registerModules(sealedTypeJacksonModule, likeAKPIJacksonModule);
    // String json = "{\"foo\":\"foo\",\"label\":\"bar\",\"myTypes\":[]}";
    // LikeAnAbstractKPI actual = mapper.readValue(json,
    // LikeAnAbstractKPI.class);
    //
    // LikeAnAbstractKPI expected = LikeAnAbstractKPI.mySubType("foo", "bar",
    // ImmutableList.of());
    // Assertions.assertThat(actual).isEqualToComparingFieldByField(expected);
    // }
    //
    // @Test
    // public void genericSealedTypeSerializerShouldBeOverridable() throws
    // Exception {
    // LikeAKPIJacksonModule likeAKPIJacksonModule = new
    // LikeAKPIJacksonModule();
    // SealedTypeJacksonModule sealedTypeJacksonModule = new
    // SealedTypeJacksonModule();
    // mapper.registerModules(likeAKPIJacksonModule, sealedTypeJacksonModule);
    // String expected = "{\"label\":\"bar\",\"myTypes\":[]}";
    //
    // LikeAnAbstractKPI from = LikeAnAbstractKPI.mySubType("foo", "bar",
    // ImmutableList.of());
    // String actual = mapper.writeValueAsString(from);
    //
    // Assertions.assertThat(actual).isEqualTo(expected);
    // }

    @Test
    public void shouldFailWithoutTypeOrSpecificModule() throws Exception {
        String json = "{\"bar\":\"bar\"}";
        expectedException.expect(JsonMappingException.class);
        mapper.readValue(json, UnionLike.class);
    }
}
