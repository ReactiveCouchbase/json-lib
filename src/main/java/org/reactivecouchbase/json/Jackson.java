package org.reactivecouchbase.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Throwables;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

class Jackson {

    private static SimpleModule module = null;
    private static ObjectMapper mapper = null;
    private static JsonFactory jsonFactory = null;
    static {
        module = new SimpleModule("reactivecouchbase-json", Version.unknownVersion()) {
            @Override
            public void setupModule(SetupContext setupContext) {
                setupContext.addDeserializers(new JsDeserializers(Jackson.class.getClassLoader()));
                setupContext.addSerializers(new JsSerializers());
            }
        };
        mapper = new ObjectMapper().registerModule(module);
        jsonFactory = new JsonFactory(mapper);
    }

    public static JsonGenerator stringJsonGenerator(StringWriter out) {
        try {
            return jsonFactory.createGenerator(out);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static JsonParser jsonParser(String str) {
        try {
            return jsonFactory.createParser(str);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static JsValue parseJsValue(String in) {
        try {
            return mapper.readValue(in, JsValue.class);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static String generateFromJsValue(JsValue in) {
        try {
            StringWriter sw = new java.io.StringWriter();
            JsonGenerator gen = stringJsonGenerator(sw);
            mapper.writeValue(gen, in);
            sw.flush();
            return sw.getBuffer().toString();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static String prettify(JsValue in) {
        try {
            StringWriter sw = new java.io.StringWriter();
            JsonGenerator gen = stringJsonGenerator(sw).setPrettyPrinter(new com.fasterxml.jackson.core.util.DefaultPrettyPrinter());
            mapper.writerWithDefaultPrettyPrinter().writeValue(gen, in);
            sw.flush();
            return sw.getBuffer().toString();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static JsonNode toJson(final Object data) {
        try {
            return mapper.valueToTree(data);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JsValue jsonNodeToJsValue(JsonNode node) {
        try {
            return mapper.treeToValue(node, JsValue.class);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode jsValueToJsonNode(JsValue val) {
        try {
            return mapper.valueToTree(val);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <A> A fromJson(JsonNode json, Class<A> clazz) {
        try {
            return mapper.treeToValue(json, clazz);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
    object module extends SimpleModule("PlayJson", Version.unknownVersion()) {
        override def setupModule(context: SetupContext) {
            context.addDeserializers(new PlayDeserializers(classLoader))
            context.addSerializers(new PlaySerializers)
        }
    }  */

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private static class JsDeserializers extends Deserializers.Base {

        private final ClassLoader classLoader;

        public JsDeserializers(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        @Override
        public JsonDeserializer<?> findBeanDeserializer(JavaType javaType, DeserializationConfig deserializationConfig, BeanDescription beanDescription) throws JsonMappingException {
            Class<?> clazz = javaType.getRawClass();
            if (JsValue.class.isAssignableFrom(clazz) || clazz.equals(JsNull.class)) {
                return new JsValueDeserializer(deserializationConfig.getTypeFactory(), clazz);
            }
            return null;
        }
    }

    private static class JsSerializers extends Serializers.Base {
        @Override
        public JsonSerializer<?> findSerializer(SerializationConfig serializationConfig, JavaType javaType, BeanDescription beanDescription) {
            if (JsValue.class.isAssignableFrom(beanDescription.getBeanClass())) {
                return new JsValueSerializer();
            }
            return null;
        }
    }

    private static class JsValueSerializer extends JsonSerializer<JsValue> {

        @Override
        public void serialize(JsValue value, JsonGenerator json, SerializerProvider provider) throws IOException, JsonProcessingException {
            for (JsNumber number : value.asOpt(JsNumber.class)) {
                json.writeNumber(number.value);
            }
            for (JsString str : value.asOpt(JsString.class)) {
                json.writeString(str.value);
            }
            for (JsBoolean bool : value.asOpt(JsBoolean.class)) {
                json.writeBoolean(bool.value);
            }
            for (JsNull _ : value.asOpt(JsNull.class)) {
                json.writeNull();
            }
            for (JsUndefined _ : value.asOpt(JsUndefined.class)) {
                json.writeNull();
            }
            for (JsArray array : value.asOpt(JsArray.class)) {
                json.writeStartArray();
                for (JsValue val : array.values) {
                    serialize(val, json, provider);
                }
                json.writeEndArray();
            }
            for (JsObject obj : value.asOpt(JsObject.class)) {
                json.writeStartObject();
                for (Map.Entry<String, JsValue> val : obj.values.entrySet()) {
                    json.writeFieldName(val.getKey());
                    serialize(val.getValue(), json, provider);
                }
                json.writeEndObject();
            }
        }
    }

    private static class JsValueDeserializer extends JsonDeserializer<JsValue> {

        private final Class<?> clazz;
        private final TypeFactory factory;

        private JsValueDeserializer(TypeFactory factory, Class<?> clazz) {
            this.clazz = clazz;
            this.factory = factory;
        }

        @Override
        public boolean isCachable() {
            return true;
        }

        @Override
        public JsValue getNullValue() {
            return JsonLib.JSNULL_INSTANCE;
        }

        @Override
        public JsValue deserialize(JsonParser jp, DeserializationContext ctx) throws IOException, JsonProcessingException {
            if (jp.getCurrentToken() == null) {
                jp.nextToken();
            }
            JsonToken token = jp.getCurrentToken();
            JsValue value = JsonLib.nill();
            if (token.equals(JsonToken.VALUE_NUMBER_FLOAT) || token.equals(JsonToken.VALUE_NUMBER_INT)) {
                value = new JsNumber(jp.getDecimalValue());
            }
            if (token.equals(JsonToken.VALUE_STRING)) {
                value = new JsString(jp.getText());
            }
            if (token.equals(JsonToken.VALUE_TRUE)) {
                value = new JsBoolean(true);
            }
            if (token.equals(JsonToken.VALUE_FALSE)) {
                value = new JsBoolean(false);
            }
            if (token.equals(JsonToken.VALUE_NULL)) {
                value = JsonLib.JSNULL_INSTANCE;
            }
            if (token.equals(JsonToken.START_OBJECT)) {
                jp.nextToken();
                value = readObject(jp, ctx);
            }
            if (token.equals(JsonToken.START_ARRAY)) {
                jp.nextToken();
                value = readArray(jp, ctx);
            }
            jp.nextToken();
            if (!clazz.isAssignableFrom(value.getClass())) {
                throw ctx.mappingException(clazz);
            }
            return value;
        }

        private JsObject readObject(JsonParser jp, DeserializationContext ctx) throws java.io.IOException, JsonParseException {
            JsObject object = new JsObject();
            while(jp.getCurrentToken() != null && !jp.getCurrentToken().equals(JsonToken.END_OBJECT)) {
                JsonToken token = jp.getCurrentToken();
                if (token.equals(JsonToken.FIELD_NAME)) {
                    String key = jp.getCurrentName();
                    jp.nextToken();
                    JsValue val = deserialize(jp, ctx);
                    object.values.put(key, val);
                }
                if (!token.equals(JsonToken.FIELD_NAME)) {
                    jp.nextToken();
                }
            }
            jp.nextToken();
            return object;
        }

        private JsArray readArray(JsonParser jp, DeserializationContext ctx) throws java.io.IOException, JsonParseException {
            JsArray array = new JsArray();
            while(jp.getCurrentToken() != null && !jp.getCurrentToken().equals(JsonToken.END_ARRAY)) {
                JsValue val = deserialize(jp, ctx);
                array.values.add(val);
                jp.nextToken();
            }
            jp.nextToken();
            return array;
        }
    }
}
