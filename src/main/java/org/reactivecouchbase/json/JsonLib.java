package org.reactivecouchbase.json;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * JsonLib API Usage :
 *
 * import org.reactivecouchbase.json.JsonLib
 * import static org.reactivecouchbase.json.JsonLib.*
 *
 * Create a Json Object
 * --------------------
 *
 * JsObject o = Json.obj(
 *     $( "key1", "value1" ),
 *     $( "key2", "value2" ),
 *     $( "key3", 1 ),
 *     $( "key4", 2.3 ),
 *     $( "key5", Json.obj(
 *         $( "key1", "value1" ),
 *         $( "key2", "value2" ),
 *         $( "key3", Json.arr(
 *             "val1", "val2", 3, Json.obj(
 *                 $( "key1", "value1" )
 *             )
 *         ))
 *     ))
 * );
 *
 * JsObject user = Json.toJson(new User(...));
 *
 * Create Json string
 * -------------------
 *
 * JsObject o = Json.obj(
 *     $( "key1", "value1" ),
 *     $( "key2", "value2" )
 * );
 * String json = Json.stringify( o );  // {"key1":"value1", "key2":"value2"}
 *
 * Create pretty Json string
 * -------------------------
 *
 * String prettyJson = Json.prettyPrint( o );  // {
 *                                             //     "key1":"value1",
 *                                             //     "key2":"value2"
 *                                             // }
 *
 * Create JsValue from string
 * --------------------------
 *
 * JsValue jsVal = Json.parse( "{\"key1\":\"val1\"}" );
 * JsObject o = jsVal.as(JsObject.class);
 *
 * Json object manipulation
 * ------------------------
 *
 * JsObject jsObj = jsVal.as(JsObject.class);
 * for (JsObject obj : jsVal.asOpt(JsObject.class)) {
 *     JsObject newObj = obj.add(Json.obj( $( "key1", "value1" )));
 *     JsObject other = Json.obj(
 *         $( "key1", "value1" ),
 *         $( "key2", "value2" )
 *     );
 *     JsObject merged = newObj.merge(other);
 *     JsObject merged2 = newObj.deepMerge(other);
 *     String key1 = merged.field("key1").as(String.class);
 * }
 *
 * for (String key1 : jsObj.field("key1").readOpt(String.class)) {
 *     System.out.println("the value of key1 is : " + key1);
 * }
 *
 * Json array manipulation
 * -----------------------
 *
 * Json.stringify(Json.arr("val1", "val2", "val3").append(Json.arr("val4", "val5")).add("val6)) // ["val1","val2","val3","val4","val5","val6"]
 * Json.arr("val1", "val2", "val3").append(Json.arr("val4", "val5")).add("val6).get(5) // val6
 *
 * Custom Json reading and validating
 * ----------------------------------
 *
 * import static org.reactivecouchbase.json.JsonLib.JsResult.*
 * import static org.reactivecouchbase.json.JsonLib.DefaultReaders.*
 * import static org.reactivecouchbase.json.JsonLib.ReaderConstraints.*
 *
 * Reader<User> userReader = new Reader<User>() {
 *      @Override
 *      public JsResult<User> read(JsValue value) {
 *          try {
 *              return new JsSuccess<User>(new User(
 *                  value.field("name").as(String.class),
 *                  value.field("surname").as(String.class),
 *                  value.field("age").as(Integer.class, max( 99, min( 18, Json.reads(Integer.class) ))),
 *                  value.field("email").as(String.class, matches( "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)+$" , Json.reads(String.class) ))
 *              ));
 *          } catch (Exception e) {
 *              return new JsError<T>(new IllegalStateException("Ooops"));
 *          }
 *      }
 * };
 *
 * or using functionnal composition of JsResults
 *
 * // Java 8 lambda syntax for shorter examples
 * Reader<User> userReader = new Reader<User>() {
 *      @Override
 *      public JsResult<User> read(JsValue value) {
 *          return combine(
 *              value.field("name").read(String.class),
 *              value.field("surname").read(String.class),
 *              value.field("age").read(Integer.class, max( 99, min( 18, Json.reads(Integer.class) ))),
 *              value.field("email").read(String.class, matches( "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)+$" , Json.reads(String.class) ))
 *          ).map(tuple -> {
 *              return new User(tuple._1, tuple._2, tuple._3, tuple._4);
 *          });
 *      }
 * };
 *
 * JsResult<T> maybeUser = Json.fromJson(Json.parse(...), userReader);
 * for (User user : maybeUser) {
 *     System.out.println("Yeah user is : " + user.toString());
 * }
 *
 * // Java 8 lambda syntax for shorter examples
 * maybeUser.filter(user -> user.age > 18).filterNot(user -> user.age < 99).map(user -> {
 *     System.out.println("Yeah user is : " + user.toString());
 * });
 *
 * Custom Json writing
 * -------------------
 *
 * Writer<User> userWriter = new Writer<User>() {
 *     public JsValue write(User user) {
 *         return Json.obj(
 *             $("name", user.name),
 *             $("surname", user.surname),
 *             $("age", user.age),
 *             $("email", user.email)
 *         );
 *     }
 * };
 *
 * JsObject user = Json.toJson(new User(...), userWriter).as(JsObject.class);
 *
 * 
 * @author Mathieu ANCELIN
 */
public class JsonLib {

    /**
     * To wrap already JSON formatted data
     */
    public static class JsonFormattedValue extends JsValue {
        public final String value;
        public JsonFormattedValue(String value) {
            if (value == null) throw new IllegalArgumentException("Value can't be null !");
            this.value = value;
        }
        String toJsonString() {
            return value;
        }
        public String toString() {
            return "Json(" + value + ")";
        }
    }

    /* Helper to create JsValues on the fly */

    public static JsPair $(String name, JsValue value) {
        return new JsPair(name, value);
    }
    public static JsPair $(String name, Long value) {
        return new JsPair(name, value);
    }
    public static JsPair $(String name, Integer value) {
        return new JsPair(name, value);
    }
    public static JsPair $(String name, Double value) {
        return new JsPair(name, value);
    }
    public static JsPair $(String name, BigDecimal value) {
        return new JsPair(name, value);
    }
    public static JsPair $(String name, BigInteger value) {
        return new JsPair(name, value);
    }
    public static JsPair _(String name) {
        return new JsPair(name);
    }
    public static JsPair __(String name) {
        return new JsPair(name, JSUNDEFINED_INSTANCE);
    }
    public static JsPair $(String name, String value) {
        return new JsPair(name, value);
    }
    public static JsPair $$(String name, String value) {
        return new JsPair(name, new JsonFormattedValue(value));
    }
    public static JsPair $$$(String name, Object value) {
        return new JsPair(name, Json.wrap(value));
    }
    public static JsPair $(String name, Boolean value) {
        return new JsPair(name, value);
    }

    public static JsPair $(String name, Map<String, ?> value) {
        return new JsPair(name, mapAsObj(value));
    }

    public static JsObject mapAsObj(Map<String, ?> value) {
        return new JsObject(Maps.transformValues(value, new Function<Object, JsValue>() {
            @Override
            public JsValue apply(java.lang.Object o) {
                return Json.wrap(o);
            }
        }));
    }

    public static JsString string(String value) { return new JsString(value); }
    public static JsBoolean bool(Boolean value) { return new JsBoolean(value); }
    public static JsNumber number(Integer value) { return new JsNumber(value); }
    public static JsNumber number(Long value) { return new JsNumber(value); }
    public static JsNumber number(Double value) { return new JsNumber(value); }
    public static JsNumber number(BigDecimal value) { return new JsNumber(value); }
    public static JsNumber number(BigInteger value) { return new JsNumber(value); }

    public static JsValue json(String value) {
        return new JsonFormattedValue(value);
    }

    static final JsNull JSNULL_INSTANCE = new JsNull();
    public static JsNull nill() {
        return JSNULL_INSTANCE;
    }

    static final JsUndefined JSUNDEFINED_INSTANCE = new JsUndefined();

    public static JsUndefined undefined() {
        return JSUNDEFINED_INSTANCE;
    }

    static Map<String, JsValue> asMap(String name, JsValue value) {
        Map<String, JsValue> values = new HashMap<String, JsValue>();
        values.put(name, value);
        return values;
    }
}
