JsonLib
======================================

Highly inspired and copied from @mandubian (Pascal Voitot) Json lib for Play 2 Scala

Will be the standard Json library for the ReactiveCouchbase Java wrapper

Create a Json Object
--------------------

```java
JsObject o = Json.obj(
    $( "key1", "value1" ),
    $( "key2", "value2" ),
    $( "key3", 1 ),
    $( "key4", 2.3 ),
    $( "key5", Json.obj(
        $( "key1", "value1" ),
        $( "key2", "value2" ),
        $( "key3", Json.arr(
            "val1", "val2", 3, Json.obj(
                $( "key1", "value1" )
            )
        ))
    ))
);

JsObject user = Json.toJson(new User(...));
```

Create Json string
-------------------

```java
JsObject o = Json.obj(
    $( "key1", "value1" ),
    $( "key2", "value2" )
);
String json = Json.stringify( o );  // {"key1":"value1", "key2":"value2"}
```

Create pretty Json string
-------------------------

```java
String prettyJson = Json.prettyPrint( o );  // {
                                            //     "key1":"value1",
                                            //     "key2":"value2"
                                            // }
```

Create JsValue from string
--------------------------

```java
JsValue jsVal = Json.parse( "{\"key1\":\"val1\"}" );
JsObject o = jsVal.as(JsObject.class);
```

Json object manipulation
------------------------

```java
JsObject jsObj = jsVal.as(JsObject.class);
for (JsObject obj : jsVal.asOpt(JsObject.class)) {
    JsObject newObj = obj.add(Json.obj( $( "key1", "value1" )));
    JsObject other = Json.obj(
        $( "key1", "value1" ),
        $( "key2", "value2" )
    );
    JsObject merged = newObj.merge(other);
    JsObject merged2 = newObj.deepMerge(other);
    String key1 = merged.field("key1").as(String.class);
}

for (String key1 : jsObj.field("key1").readOpt(String.class)) {
    System.out.println("the value of key1 is : " + key1);
}
```

Json array manipulation
-----------------------

```java
Json.stringify(Json.arr("val1", "val2", "val3").append(Json.arr("val4", "val5")).add("val6")) // ["val1","val2","val3","val4","val5","val6"]
Json.arr("val1", "val2", "val3").append(Json.arr("val4", "val5")).add("val6").get(5) // val6
```

Custom Json reading and validating
----------------------------------

```java
import org.reactivecouchbase.json.*
import static org.reactivecouchbase.json.Syntax.*
import static org.reactivecouchbase.json.JsResult.*
import static org.reactivecouchbase.json.DefaultReaders.*
import static org.reactivecouchbase.json.ReaderConstraints.*

Reader<User> userReader = new Reader<User>() {
     @Override
     public JsResult<User> read(JsValue value) {
         try {
             return new JsSuccess<User>(new User(
                 value.field("name").read(String.class),
                 value.field("surname").read(String.class),
                 value.field("age").read(Integer.class, max( 99, min( 18, Json.reads(Integer.class) ))),
                 value.field("email").read(String.class, matches( "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)+$" , Json.reads(String.class) ))
             ));
         } catch (Exception e) {
             return new JsError<T>(new IllegalStateException("Ooops"));
         }
     }
};
```

or using functionnal composition of JsResults

```java
// Java 8 lambda syntax for shorter examples
Reader<User> userReader = new Reader<User>() {
     @Override
     public JsResult<User> read(JsValue value) {
         return combine(
             value.field("name").read(String.class),
             value.field("surname").read(String.class),
             value.field("age").read(Integer.class, max( 99, min( 18, Json.reads(Integer.class) ))),
             value.field("email").read(String.class, matches( "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)+$" , Json.reads(String.class) ))
         ).map(tuple -> {
             return new User(tuple._1, tuple._2, tuple._3, tuple._4);
         });
     }
};

JsResult<T> maybeUser = Json.fromJson(Json.parse(...), userReader);
for (User user : maybeUser) {
    System.out.println("Yeah user is : " + user.toString());
}

// Java 8 lambda syntax for shorter examples
maybeUser.filter(user -> user.age > 18).filterNot(user -> user.age < 99).map(user -> {
    System.out.println("Yeah user is : " + user.toString());
});
```

Custom Json writing
-------------------

```java
Writer<User> userWriter = new Writer<User>() {
    public JsValue write(User user) {
        return Json.obj(
            $("name", user.name),
            $("surname", user.surname),
            $("age", user.age),
            $("email", user.email)
        );
    }
};

JsObject user = Json.toJson(new User(...), userWriter).as(JsObject.class);
```
