package org.reactivecouchbase.json.test;

import org.junit.Assert;
import org.junit.Test;
import org.reactivecouchbase.json.*;
import org.reactivecouchbase.json.mapping.*;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.reactivecouchbase.json.Syntax.*;
import static org.reactivecouchbase.json.mapping.ReaderConstraints.*;

public class JsonTest {

    public static class User {
        public String name;
        public String surname;
        public Integer age;

        public User() {
        }

        public User(String name, String surname, Integer age) {
            this.name = name;
            this.surname = surname;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSurname() {
            return surname;
        }

        public void setSurname(String surname) {
            this.surname = surname;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof User)) return false;

            User user = (User) o;

            if (!age.equals(user.age)) return false;
            if (!name.equals(user.name)) return false;
            if (!surname.equals(user.surname)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + surname.hashCode();
            result = 31 * result + age.hashCode();
            return result;
        }
    }

    @Test
    public void checkNumbers() {
        JsValue value = Json.obj().with("value", new BigDecimal("12334535345456700.12345634534534578901"));
        JsValue value2 = Json.obj().with("value", new BigDecimal(12334535345456700.12345634534534578901));
        String str = Json.stringify(value);
        String str2 = Json.stringify(value2);
        Assert.assertEquals("{\"value\":12334535345456700.12345634534534578901}", str);
        Assert.assertEquals("{\"value\":12334535345456700}", str2);
    }

    @Test
    public void primitiveTest() {
        JsNull nill = nill();
        JsNull nill2 = nill();
        JsUndefined undefined = undefined();
        JsUndefined undefined2 = undefined();
        JsBoolean boolTrue = bool(true);
        JsBoolean boolTrue2 = bool(true);
        JsBoolean boolFalse = bool(false);
        JsNumber number1 = number(1);
        JsNumber number12 = number(1);
        JsNumber number2 = number(2);
        JsNumber number21 = number(2.1);
        JsNumber number212 = number(2.1);
        JsString hello = string("Hello");
        JsString helloWorld = string("Hello World!");
        JsString helloWorld2 = string("Hello World!");

        Assert.assertEquals(nill, nill2);
        Assert.assertEquals(undefined, undefined2);
        Assert.assertEquals(boolTrue, boolTrue2);
        Assert.assertEquals(undefined, undefined2);
        Assert.assertEquals(number1, number12);
        Assert.assertEquals(number21, number212);
        Assert.assertEquals(helloWorld, helloWorld2);

        Assert.assertNotSame(nill, undefined);
        Assert.assertNotSame(boolTrue, boolFalse);
        Assert.assertNotSame(number1, number2);
        Assert.assertNotSame(number2, number21);
        Assert.assertNotSame(hello, helloWorld);
    }

    @Test
    public void objectTest() {
        JsObject basicObject1 = Json.obj(
                $("key1", "value1"),
                $("key2", "value2")
        );
        JsObject basicObject2 = Json.obj(
                $("key1", "value1"),
                $("key2", "value2")
        );
        String basicObject3AsString = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
        JsValue basicObject3 = Json.parse(basicObject3AsString);

        Assert.assertEquals(basicObject1, basicObject2);
        Assert.assertEquals(basicObject1, basicObject3);
        Assert.assertEquals(basicObject2, basicObject3);

        Assert.assertTrue(Json.prettyPrint(basicObject1).contains("\n"));

        JsObject userJson = Json.obj(
                $("name", "John"),
                $("surname", "Doe"),
                $("age", 42)
        );

        JsObject oldUserJson = Json.obj(
                $("name", "John"),
                $("surname", "Doe"),
                $("age", 43)
        );

        JsObject userJson2 = Json.obj(
                $("name", "John"),
                $("surname", "Doe")
        );

        JsObject age = Json.obj($("age", 42));

        JsObject userJson4 = userJson2.merge(age);

        userJson2 = userJson2.add($("age", 42));

        JsObject userJson3 = Json.parse("{\"name\":\"John\", \"surname\":\"Doe\", \"age\":42}").as(JsObject.class);

        System.out.println(userJson3);

        User user = new User("John", "Doe", 42);
        Assert.assertNotSame(userJson, user);
        Assert.assertEquals(userJson, Json.toJson(user));
        Assert.assertNotEquals(userJson, oldUserJson);
        Assert.assertTrue(userJson.deepEquals(userJson));
        Assert.assertFalse(userJson.deepEquals(oldUserJson));
        Assert.assertEquals(userJson, userJson2);
        Assert.assertEquals(userJson2, Json.toJson(user));
        Assert.assertEquals(userJson, userJson3);
        Assert.assertEquals(userJson3, Json.toJson(user));
        Assert.assertEquals(userJson2, userJson3);
        Assert.assertEquals(userJson, userJson4);
        Assert.assertEquals(userJson4, Json.toJson(user));
        Assert.assertEquals(user, Json.fromJson(userJson, Json.reads(User.class)).get());

        for (String name : userJson.field("name").asOpt(String.class)) {
            Assert.assertEquals("John", name);
        }

        for (String surname : userJson.field("surname").asOpt(String.class)) {
            Assert.assertEquals("Doe", surname);
        }

        for (Integer a : userJson.field("age").asOpt(Integer.class)) {
            Assert.assertEquals(Integer.valueOf(42), a);
        }

    }

    @Test
    public void arrayTest() {
        String value = Json.stringify(Json.arr("val1", "val2", "val3").append(Json.arr("val4", "val5")).addElement(string("val6")));
        Assert.assertEquals("[\"val1\",\"val2\",\"val3\",\"val4\",\"val5\",\"val6\"]", value);
        Assert.assertEquals(Json.arr("val1", "val2", "val3").append(Json.arr("val4", "val5", "val6")).get(5), string("val6"));
    }

    @Test
    public void readerWriterTest() {
        JsObject userJson = Json.obj(
                $("name", "John"),
                $("surname", "Doe"),
                $("age", 42)
        );
        JsObject userJson2 = Json.obj(
                $("name", "John"),
                $("surname", "Doe"),
                $("age", 3)
        );
        JsObject userJson3 = Json.obj(
                $("name", "John"),
                $("surname", "Doe"),
                $("age", 103)
        );
        JsObject userJson4 = Json.obj(
                $("name", "Jane"),
                $("surname", "Doe"),
                $("age", 103)
        );

        Reader<User> userReader = value -> {
            JsObject object = value.as(JsObject.class);
            try {
                return JsResult.success(new User(
                        object.field("name").read(matches("John")).get(),
                        object.field("surname").read(String.class).get(),
                        object.field("age").read(JsValidator.of(Integer.class).and(max(99)).and(min(18))).get()
                ));
            } catch (Exception e) {
                return JsResult.error(e);
            }
        };
        JsResult<User> maybeUser = Json.fromJson(userJson, userReader);
        Assert.assertFalse(maybeUser.isErrors());
        Assert.assertTrue(maybeUser.isSuccess());
        for (User user : maybeUser) {
            Assert.assertEquals("John", user.name);
            Assert.assertEquals("Doe", user.surname);
            Assert.assertEquals(Integer.valueOf(42), user.age);
        }
        JsResult<User> maybeUser2 = Json.fromJson(userJson2, userReader);
        JsResult<User> maybeUser3 = Json.fromJson(userJson3, userReader);
        JsResult<User> maybeUser4 = Json.fromJson(userJson4, userReader);

        Assert.assertTrue(maybeUser2.isErrors());
        Assert.assertFalse(maybeUser2.isSuccess());
        Assert.assertTrue(maybeUser2.hasErrors());
        Assert.assertEquals(1, maybeUser2.countErrors());


        Assert.assertTrue(maybeUser3.isErrors());
        Assert.assertFalse(maybeUser3.isSuccess());
        Assert.assertTrue(maybeUser3.hasErrors());
        Assert.assertEquals(1, maybeUser3.countErrors());

        Assert.assertTrue(maybeUser4.isErrors());
        Assert.assertFalse(maybeUser4.isSuccess());
        Assert.assertTrue(maybeUser4.hasErrors());
        Assert.assertEquals(1, maybeUser4.countErrors());

        Writer<User> userWriter = user -> Json.obj(
                $("name", user.name.toUpperCase()),
                $("surname", user.surname.toUpperCase()),
                $("age", user.age)
        );

        JsObject userJsonUpper = Json.obj(
                $("name", "JOHN"),
                $("surname", "DOE"),
                $("age", 42)
        );

        JsObject value = Json.toJson(new User("John", "Doe", 42), userWriter).as(JsObject.class);

        Assert.assertEquals(userJsonUpper, value);
    }

    @Test
    public void combinatorReadersTest() {
        JsObject userJson = Json.obj(
                $("name", "John"),
                $("surname", "Doe"),
                $("age", 42)
        );
        JsObject userJson2 = Json.obj(
                $("name", "John"),
                $("surname", "Doe"),
                $("age", 3)
        );
        JsObject userJson3 = Json.obj(
                $("name", "John"),
                $("surname", "Doe"),
                $("age", 103)
        );
        JsObject userJson4 = Json.obj(
                $("name", "Jane"),
                $("surname", "Doe"),
                $("age", 103)
        );

        Reader<User> userReader = value -> {
            JsObject object = value.as(JsObject.class);
            try {
                return JsResult.success(new User(
                        object.field("name").read(matches("John")).get(),
                        object.field("surname").read(String.class).get(),
                        object.field("age").read(JsValidator.of(Integer.class).and(min(18)).and(max(99))).get()
                ));
            } catch (Exception e) {
                return JsResult.error(e);
            }
        };
        JsResult<User> maybeUser = Json.fromJson(userJson, userReader);
        Assert.assertFalse(maybeUser.isErrors());
        Assert.assertTrue(maybeUser.isSuccess());
        for (User user : maybeUser) {
            Assert.assertEquals("John", user.name);
            Assert.assertEquals("Doe", user.surname);
            Assert.assertEquals(Integer.valueOf(42), user.age);
        }
        JsResult<User> maybeUser2 = Json.fromJson(userJson2, userReader);
        JsResult<User> maybeUser3 = Json.fromJson(userJson3, userReader);
        JsResult<User> maybeUser4 = Json.fromJson(userJson4, userReader);

        Assert.assertTrue(maybeUser2.isErrors());
        Assert.assertFalse(maybeUser2.isSuccess());
        Assert.assertTrue(maybeUser2.hasErrors());
        Assert.assertEquals(1, maybeUser2.countErrors());


        Assert.assertTrue(maybeUser3.isErrors());
        Assert.assertFalse(maybeUser3.isSuccess());
        Assert.assertTrue(maybeUser3.hasErrors());
        Assert.assertEquals(1, maybeUser3.countErrors());

        Assert.assertTrue(maybeUser4.isErrors());
        Assert.assertFalse(maybeUser4.isSuccess());
        Assert.assertTrue(maybeUser4.hasErrors());
        Assert.assertEquals(1, maybeUser4.countErrors());
    }

    @Test
    public void mergeTest() {
        JsObject expected = Json.obj(
                $("key1", "value1"),
                $("key2", "value2"),
                $("key3", 1),
                $("key4", 2.3),
                $("key5", Json.obj(
                        $("key1", "value1"),
                        $("key2", "value2"),
                        $("key3", Json.arr(
                                "val1", "val2", 3, Json.obj(
                                        $("key1", "value1")
                                )
                        ))
                ))
        );

        JsObject obj1 = Json.obj(
                $("key1", "value1"),
                $("key2", "value2")
        );

        JsObject obj2 = Json.obj(
                $("key3", 1),
                $("key4", 2.3)
        );

        JsObject obj4 = Json.obj(
                $("key5", Json.obj(
                        $("key1", "value1"),
                        $("key2", "value2"),
                        $("key3", Json.arr("val1", "val2", 3, Json.obj(
                                $("key1", "value1")
                                )
                                )
                        )
                ))
        );

        JsObject obj5 = Json.obj(
                $("key5", Json.obj(
                        $("key1", "value1"),
                        $("key3", Json.arr("val1", "val2", 3, Json.obj(
                                $("key1", "value1")
                                )
                                )
                        )
                ))
        );

        JsObject obj6 = Json.obj(
                $("key5", Json.obj(
                        $("key2", "value2")
                ))
        );

        System.out.println(Json.stringify(obj4));
        System.out.println(Json.prettyPrint(obj4));
        Json.parse(Json.stringify(obj4));
        Json.parse(Json.prettyPrint(obj4));

        System.out.println(Json.stringify(expected));
        System.out.println(Json.prettyPrint(expected));
        Json.parse(Json.stringify(expected));
        Json.parse(Json.prettyPrint(expected));

        Assert.assertEquals(expected, obj1.merge(obj2).deepMerge(obj4));
        Assert.assertEquals(expected, obj1.deepMerge(obj2).deepMerge(obj5.deepMerge(obj6)));
    }

    @Test
    public void deepSearchTest() {
        JsObject deepObject = Json.obj(
                $("key1", "value1"),
                $("key2", "value2"),
                $("key3", 1),
                $("key4", 2.3),
                $("key5", Json.obj(
                        $("key1", "value12"),
                        $("key2", "value22"),
                        $("key3", Json.obj($("key1", "valueSearched")))
                        )
                )
        );

        Assert.assertEquals("valueSearched", deepObject.field("key5").field("key3").field("key1").as(String.class));
        Assert.assertEquals("valueSearched", deepObject.field("key5").field("key3").field("key1").as(String.class));

        Assert.assertTrue(deepObject.fields("key1").contains(string("value1")));
        Assert.assertTrue(deepObject.fields("key1").contains(string("value1")));
        Assert.assertTrue(deepObject.fields("key1").contains(string("valueSearched")));
        Assert.assertTrue(deepObject.fields("key2").contains(string("value2")));
        Assert.assertTrue(deepObject.fields("key2").contains(string("value22")));

        Assert.assertTrue(deepObject.fields("key1").contains(string("value1")));
        Assert.assertTrue(deepObject.fields("key1").contains(string("value1")));
        Assert.assertTrue(deepObject.fields("key1").contains(string("valueSearched")));
        Assert.assertTrue(deepObject.fields("key2").contains(string("value2")));
        Assert.assertTrue(deepObject.fields("key2").contains(string("value22")));
    }

    public static class Foo {
        public final String value1;
        public final Double value2;
        public final String value3;
        public final String value4;

        public Foo(String value1, Double value2, String value3, String value4) {
            this.value1 = value1;
            this.value2 = value2;
            this.value3 = value3;
            this.value4 = value4;
        }
    }

    @Test
    public void deepExtractionTest() {
        JsObject deepObject = Json.obj(
                $("key1", "value1"),
                $("key2", "value2"),
                $("key3", 1),
                $("key4", 2.3),
                $("key5", Json.obj(
                        $("key1", "value12"),
                        $("key2", "value22"),
                        $("key3", Json.obj(
                                $("key1", "valueSearched")
                                )
                        )
                        )
                )
        );
        Reader<Foo> fooReader = value -> {
            JsObject object = value.as(JsObject.class);
            try {
                return JsResult.success(new Foo(
                        object.field("key1").read(String.class).get(),
                        object.field("key4").read(Double.class).get(),
                        object.field("key5").field("key2").read(String.class).get(),
                        object.field("key5").field("key3").field("key1").read(String.class).get()
                ));
            } catch (Exception e) {
                return JsResult.error(e);
            }
        };
        boolean passes = false;
        for (Foo foo : fooReader.read(deepObject)) {
            passes = true;
            Assert.assertEquals("value1", foo.value1);
            Assert.assertEquals(new Double(2.3), foo.value2);
            Assert.assertEquals("value22", foo.value3);
            Assert.assertEquals("valueSearched", foo.value4);
        }
        Assert.assertTrue(passes);
    }

    @Test
    public void deepEquals() {
        JsObject expected = Json.obj(
                $("productId", "123456"),
                $("price", 20.4),
                $("vat", 19.6),
                $("desc", "Some stuff"),
                $("name", "Stuff")
        );
        JsObject obj = Json.obj(
                $("price", 20.4),
                $("productId", "123456"),
                $("desc", "Some stuff"),
                $("name", "Stuff"),
                $("vat", 19.6)
        );
        JsObject wrongobj1 = Json.obj(
                $("price", 20.4),
                $("productId", "123456"),
                $("desc", "Some stff"),
                $("name", "Stuff"),
                $("vat", 19.6)
        );
        JsObject wrongobj2 = Json.obj(
                $("price", 20.4),
                $("productId", "123456"),
                $("desc", "Some stuff"),
                $("nae", "Stuff"),
                $("vat", 19.6)
        );
        Assert.assertEquals(expected, obj);
        Assert.assertNotEquals(expected, wrongobj1);
        Assert.assertNotEquals(expected, wrongobj2);
    }

    @Test
    public void fmtTest() {
        JsValue personJsValue = Json.obj(
                $("age", 42),
                $("name", "John"),
                $("surname", "Doe"),
                $("address", Json.obj(
                        $("number", "221b"),
                        $("street", "Baker Street"),
                        $("city", "London")
                ))
        );

        JsValue badPersonJsValue = Json.obj(
                $("name", "John"),
                $("surname", "Doe"),
                $("age", 42),
                $("adresse", Json.obj(
                        $("number", "221b"),
                        $("street", "Baker Street"),
                        $("city", "London")
                ))
        );

        String expectedPerson = Json.stringify(personJsValue);
        String badPerson = Json.stringify(badPersonJsValue);

        System.out.println(Json.parse(expectedPerson));

        Assert.assertTrue(Json.parse(expectedPerson).validate(Person.FORMAT).isSuccess());
        Assert.assertTrue(Json.parse(badPerson).validate(Person.FORMAT).isErrors());
    }


    @Test
    public void foldErrorTest() {
        Throwable throwable = new RuntimeException("Oups");
        JsResult<Person> error = JsResult.error(throwable);

        String fold = error.fold(err -> "ERR", person -> "OK");
        Assert.assertEquals("ERR", fold);
    }

    @Test
    public void foldSuccessTest() {
        Person person = new Person("Rambo", "John", 30, null);
        JsResult<Person> error = JsResult.success(person);

        String fold = error.fold(err -> "ERR", p -> "OK");
        Assert.assertEquals("OK", fold);
    }


    public static class Address {
        public final String number;
        public final String street;
        public final String city;

        public Address(String number, String street, String city) {
            this.number = number;
            this.street = street;
            this.city = city;
        }

        public static final Format<Address> FORMAT = new Format<Address>() {
            @Override
            public JsResult<Address> read(JsValue value) {
                try {
                    return JsResult.success(new Address(
                            value.field("number").read(String.class).get(),
                            value.field("street").read(String.class).get(),
                            value.field("city").read(String.class).get()
                    ));
                } catch (Exception e) {
                    return JsResult.error(e);
                }
            }

            @Override
            public JsValue write(Address value) {
                return Json.obj(
                        $("number", value.number),
                        $("street", value.street),
                        $("city", value.city)
                );
            }
        };
    }

    public static class Person {
        public final String name;
        public final String surname;
        public final Integer age;
        public final Address address;

        public Person(String name, String surname, Integer age, Address address) {
            this.name = name;
            this.surname = surname;
            this.age = age;
            this.address = address;
        }

        public static final Format<Person> FORMAT = new Format<Person>() {
            @Override
            public JsResult<Person> read(JsValue value) {
                try {
                    return JsResult.success(new Person(
                            value.field("name").read(String.class).get(),
                            value.field("surname").read(String.class).get(),
                            value.field("age").read(Integer.class).get(),
                            value.field("address").read(Address.FORMAT).get()
                    ));
                } catch (Exception e) {
                    return JsResult.error(e);
                }
            }

            @Override
            public JsValue write(Person value) {
                return Json.obj(
                        $("name", value.name),
                        $("surname", value.surname),
                        $("age", value.age),
                        $("address", Address.FORMAT.write(value.address))
                );
            }
        };
    }
}
