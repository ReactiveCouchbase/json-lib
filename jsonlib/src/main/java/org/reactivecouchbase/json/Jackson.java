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
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.reactivecouchbase.common.Throwables;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

public class Jackson {

    private static ObjectMapper mapper = null;
    private static JsonFactory jsonFactory = null;

    static {
        init(Jackson.class.getClassLoader());
    }

    public static void init(final ClassLoader classLoader) {
        SimpleModule module = new SimpleModule("json-lib", Version.unknownVersion()) {
            @Override
            public void setupModule(SetupContext setupContext) {
                setupContext.addDeserializers(new JsDeserializers(classLoader));
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

    public static class JsDeserializers extends Deserializers.Base {

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

    public static class JsSerializers extends Serializers.Base {
        @Override
        public JsonSerializer<?> findSerializer(SerializationConfig serializationConfig, JavaType javaType, BeanDescription beanDescription) {
            if (JsValue.class.isAssignableFrom(beanDescription.getBeanClass())) {
                return new JsValueSerializer();
            }
            return null;
        }
    }

    private static class JsValueSerializer extends JsonSerializer<JsValue> {

        // Maximum magnitude of BigDecimal to write out as a plain string
        private static final BigDecimal MaxPlain = new BigDecimal(1e20);
        // Minimum magnitude of BigDecimal to write out as a plain string
        private static final BigDecimal MinPlain = new BigDecimal(1e-10);

        @Override
        public void serialize(JsValue value, JsonGenerator json, SerializerProvider provider) throws IOException, JsonProcessingException {
            for (JsNumber number : value.asOpt(JsNumber.class)) {
                // Workaround  Same behaviour as if JsonGenerator were
                // configured with WRITE_BIGDECIMAL_AS_PLAIN, but forced as this
                // configuration is ignored when called from ObjectMapper.valueToTree
                BigDecimal v = number.value;
                BigDecimal va = v.abs();
                boolean shouldWritePlain = va.compareTo(MaxPlain) < 0 && va.compareTo(MinPlain) > 0;
                BigDecimal stripped = v.stripTrailingZeros();
                String raw = stripped.toString();
                if (shouldWritePlain) {
                    raw = stripped.toPlainString();
                }
                if (raw.indexOf('E') < 0 && raw.indexOf('.') < 0) {
                    json.writeTree(new BigIntegerNode(new BigInteger(raw)));
                } else {
                    json.writeTree(new DecimalNode(new BigDecimal(raw)));
                }
                // json.writeNumber(number.value);
            }
            for (JsString str : value.asOpt(JsString.class)) {
                json.writeString(str.value);
            }
            for (JsBoolean bool : value.asOpt(JsBoolean.class)) {
                json.writeBoolean(bool.value);
            }
            for (JsNull nevermind : value.asOpt(JsNull.class)) {
                json.writeNull();
            }
            for (JsUndefined nevermind : value.asOpt(JsUndefined.class)) {
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
            return false;
        }

        @Override
        public JsValue getNullValue() {
            return JsNull.JSNULL_INSTANCE;
        }

        @Override
        public JsValue deserialize(JsonParser jp, DeserializationContext ctx) throws IOException, JsonProcessingException {
            if (jp.getCurrentToken() == null) {
                jp.nextToken();
            }
            JsonToken token = jp.getCurrentToken();
            JsValue value = Syntax.nill();
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
                value = JsNull.JSNULL_INSTANCE;
            }
            if (token.equals(JsonToken.VALUE_EMBEDDED_OBJECT)) {
                jp.nextToken();
                value = readObject(jp, ctx);
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
                    object = object.add(Syntax.$(key, val));
                }
                if (!token.equals(JsonToken.FIELD_NAME)) {
                    jp.nextToken();
                }
            }
            return object;
        }

        private JsArray readArray(JsonParser jp, DeserializationContext ctx) throws java.io.IOException, JsonParseException {
            JsArray array = new JsArray();
            while(jp.getCurrentToken() != null && !jp.getCurrentToken().equals(JsonToken.END_ARRAY)) {
                JsValue val = deserialize(jp, ctx);
                array = array.addElement(val);
            }
            return array;
        }
    }
}
