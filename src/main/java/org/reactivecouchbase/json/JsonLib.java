package fr.alliancesoftware.json;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fr.alliancesoftware.common.Functionnal.*;

/**
 * JsonLib API Usage :
 *
 * import fr.alliancesoftware.json.JsonLib
 * import static fr.alliancesoftware.json.JsonLib.*
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
 * import static fr.alliancesoftware.json.JsonLib.JsResult.*
 * import static fr.alliancesoftware.json.JsonLib.DefaultReaders.*
 * import static fr.alliancesoftware.json.JsonLib.ReaderConstraints.*
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

    public static class JsWrappedException extends RuntimeException {
        private final List<Exception> exceptions;

        public JsWrappedException(List<Exception> exceptions) {
            this.exceptions = exceptions;
        }

        @Override
        public void setStackTrace(StackTraceElement[] stackTraceElements) {
           throw new IllegalAccessError("Can set stackstrace");
        }

        @Override
        public String getMessage() {
            return Joiner.on(" | ").join(Iterables.transform(exceptions, new Function<Exception, String>() {
                @Override
                public String apply(java.lang.Exception e) {
                    return e.getMessage();
                }
            }));
        }

        @Override
        public String getLocalizedMessage() {
            return Joiner.on(" | ").join(Iterables.transform(exceptions, new Function<Exception, String>() {
                @Override
                public String apply(java.lang.Exception e) {
                    return e.getLocalizedMessage();
                }
            }));
        }

        @Override
        public String toString() {
            return getMessage();
        }

        @Override
        public void printStackTrace() {
            for (Exception e : exceptions) {
                e.printStackTrace();
            }
        }
    }

    private static interface GsonDelegator {
        public void delegateToGson();
        public void undelegateToGson();
        public boolean delegatingToGson();
    }

    private enum Delegator implements GsonDelegator {
        GSON {
            private boolean delegate = false;
            @Override
            public void delegateToGson() {
                delegate = true;
            }
            @Override
            public void undelegateToGson() {
                delegate = false;
            }
            @Override
            public boolean delegatingToGson() {
                return delegate;
            }
        }
    }

    private static boolean delegatingToGson() {
        return Delegator.GSON.delegatingToGson();
    }

    public static void delegateToGson() {
        Delegator.GSON.delegateToGson();
    }

    public static void undelegateToGson() {
        Delegator.GSON.undelegateToGson();
    }

    public static abstract class JsResult<T> implements Iterable<T> {
        public abstract T get();
        public abstract Option<T> getOpt();
        public abstract JsResult<T> getOrElse(JsResult<T> result);
        public abstract T getValueOrElse(T result);
        public abstract T getValueOrElse(Throwable result);
        public abstract <B> JsResult<B> map(Function<T, B> map);
        public abstract <B> JsResult<B> flatMap(Function<T, JsResult<B>> map);
        public abstract JsResult<T> filter(Function<T, Boolean> predicate);
        public abstract JsResult<T> filterNot(Function<T, Boolean> predicate);
        public abstract JsResult<T> filter(Function<T, Boolean> predicate, JsResult<T> val, List<Throwable> errors);
        public abstract JsResult<T> filterNot(Function<T, Boolean> predicate, JsResult<T> val, List<Throwable> errors);
        public abstract boolean hasErrors();
        public abstract boolean isErrors();
        public abstract boolean isSuccess();
        public abstract int countErrors();
        public abstract Option<JsError<T>> asError();
        public abstract Option<JsSuccess<T>> asSuccess();
        public abstract T orError(Throwable t);

        private static <T> JsResult<T> populateErrs(JsResult<T> finalResult, JsResult<?>... results) {
            List<Throwable> throwables = new ArrayList<Throwable>();
            for (JsResult<?> res : results) {
                if (res.isErrors()) {
                    for (Throwable t : res.asError().get().errors)
                        throwables.add(t);
                }
            }
            if (throwables.isEmpty() && finalResult.isSuccess()) {
                return new JsSuccess<T>(finalResult.asSuccess().get().get());
            } else {
                // should never happens
            }
            return new JsError<T>(throwables);
        }

        public static <A, B> JsResult<T2<A, B>> combine(final JsResult<A> res1, final JsResult<B> res2) {
            return populateErrs(res1.flatMap(new Function<A, JsResult<T2<A, B>>>() {
                public JsResult<T2<A, B>> apply(final A a) {
                    return res2.map(new Function<B, T2<A, B>>() {
                        public T2<A, B> apply(B b) {
                            return new T2(a, b);
                        }
                    });
                }
            }), res1, res2);
        }

        public static <A, B, C> JsResult<T3<A, B, C>> combine(final JsResult<A> res1, final JsResult<B> res2, final JsResult<C> res3) {
            return populateErrs(res1.flatMap(new Function<A, JsResult<T3<A, B, C>>>() {
                public JsResult<T3<A, B, C>> apply(final A a) {
                    return res2.flatMap(new Function<B, JsResult<T3<A, B, C>>>() {
                        public JsResult<T3<A, B, C>> apply(final B b) {
                            return res3.map(new Function<C, T3<A, B, C>>() {
                                public T3<A, B, C> apply(final C c) {
                                    return new T3(a, b, c);
                                }
                            });
                        }
                    });
                }
            }), res1, res2, res3);
        }

        public static <A, B, C, D> JsResult<T4<A, B, C, D>> combine(final JsResult<A> res1, final JsResult<B> res2, final JsResult<C> res3, final JsResult<D> res4) {
            return populateErrs(res1.flatMap(new Function<A, JsResult<T4<A, B, C, D>>>() {
                public JsResult<T4<A, B, C, D>> apply(final A a) {
                    return res2.flatMap(new Function<B, JsResult<T4<A, B, C, D>>>() {
                        public JsResult<T4<A, B, C, D>> apply(final B b) {
                            return res3.flatMap(new Function<C, JsResult<T4<A, B, C, D>>>() {
                                public JsResult<T4<A, B, C, D>> apply(final C c) {
                                    return res4.map(new Function<D, T4<A, B, C, D>>() {
                                        public T4<A, B, C, D> apply(final D d) {
                                            return new T4(a, b, c, d);
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }), res1, res2, res3, res4);
        }

        public static <A, B, C, D, E> JsResult<T5<A, B, C, D, E>> combine(final JsResult<A> res1, final JsResult<B> res2, final JsResult<C> res3, final JsResult<D> res4, final JsResult<E> res5) {
            return populateErrs(res1.flatMap(new Function<A, JsResult<T5<A, B, C, D, E>>>() {
                public JsResult<T5<A, B, C, D, E>> apply(final A a) {
                    return res2.flatMap(new Function<B, JsResult<T5<A, B, C, D, E>>>() {
                        public JsResult<T5<A, B, C, D, E>> apply(final B b) {
                            return res3.flatMap(new Function<C, JsResult<T5<A, B, C, D, E>>>() {
                                public JsResult<T5<A, B, C, D, E>> apply(final C c) {
                                    return res4.flatMap(new Function<D, JsResult<T5<A, B, C, D, E>>>() {
                                        public JsResult<T5<A, B, C, D, E>> apply(final D d) {
                                            return res5.map(new Function<E, T5<A, B, C, D, E>>() {
                                                public T5<A, B, C, D, E> apply(final E e) {
                                                    return new T5(a, b, c, d, e);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }), res1, res2, res3, res4, res5);
        }

        public static <A, B, C, D, E, F> JsResult<T6<A, B, C, D, E, F>> combine(final JsResult<A> res1, final JsResult<B> res2, final JsResult<C> res3, final JsResult<D> res4, final JsResult<E> res5, final JsResult<F> res6) {
            return populateErrs(res1.flatMap(new Function<A, JsResult<T6<A, B, C, D, E, F>>>() {
                public JsResult<T6<A, B, C, D, E, F>> apply(final A a) {
                    return res2.flatMap(new Function<B, JsResult<T6<A, B, C, D, E, F>>>() {
                        public JsResult<T6<A, B, C, D, E, F>> apply(final B b) {
                            return res3.flatMap(new Function<C, JsResult<T6<A, B, C, D, E, F>>>() {
                                public JsResult<T6<A, B, C, D, E, F>> apply(final C c) {
                                    return res4.flatMap(new Function<D, JsResult<T6<A, B, C, D, E, F>>>() {
                                        public JsResult<T6<A, B, C, D, E, F>> apply(final D d) {
                                            return res5.flatMap(new Function<E, JsResult<T6<A, B, C, D, E, F>>>() {
                                                public JsResult<T6<A, B, C, D, E, F>> apply(final E e) {
                                                    return res6.map(new Function<F, T6<A, B, C, D, E, F>>() {
                                                        public T6<A, B, C, D, E, F> apply(final F f) {
                                                            return new T6(a, b, c, d, e, f);
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }), res1, res2, res3, res4, res5, res6);
        }

        public static <A, B, C, D, E, F, G> JsResult<T7<A, B, C, D, E, F, G>> combine(final JsResult<A> res1, final JsResult<B> res2, final JsResult<C> res3, final JsResult<D> res4, final JsResult<E> res5, final JsResult<F> res6, final JsResult<G> res7) {
            return populateErrs(res1.flatMap(new Function<A, JsResult<T7<A, B, C, D, E, F, G>>>() {
                public JsResult<T7<A, B, C, D, E, F, G>> apply(final A a) {
                    return res2.flatMap(new Function<B, JsResult<T7<A, B, C, D, E, F, G>>>() {
                        public JsResult<T7<A, B, C, D, E, F, G>> apply(final B b) {
                            return res3.flatMap(new Function<C, JsResult<T7<A, B, C, D, E, F, G>>>() {
                                public JsResult<T7<A, B, C, D, E, F, G>> apply(final C c) {
                                    return res4.flatMap(new Function<D, JsResult<T7<A, B, C, D, E, F, G>>>() {
                                        public JsResult<T7<A, B, C, D, E, F, G>> apply(final D d) {
                                            return res5.flatMap(new Function<E, JsResult<T7<A, B, C, D, E, F, G>>>() {
                                                public JsResult<T7<A, B, C, D, E, F, G>> apply(final E e) {
                                                    return res6.flatMap(new Function<F, JsResult<T7<A, B, C, D, E, F, G>>>() {
                                                        public JsResult<T7<A, B, C, D, E, F, G>> apply(final F f) {
                                                            return res7.map(new Function<G, T7<A, B, C, D, E, F, G>>() {
                                                                public T7<A, B, C, D, E, F, G> apply(final G g) {
                                                                    return new T7(a, b, c, d, e, f, g);
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }), res1, res2, res3, res4, res5, res6, res7);
        }

        public static <A, B, C, D, E, F, G, H> JsResult<T8<A, B, C, D, E, F, G, H>> combine(final JsResult<A> res1, final JsResult<B> res2, final JsResult<C> res3, final JsResult<D> res4, final JsResult<E> res5, final JsResult<F> res6, final JsResult<G> res7, final JsResult<H> res8) {
            return populateErrs(res1.flatMap(new Function<A, JsResult<T8<A, B, C, D, E, F, G, H>>>() {
                public JsResult<T8<A, B, C, D, E, F, G, H>> apply(final A a) {
                    return res2.flatMap(new Function<B, JsResult<T8<A, B, C, D, E, F, G, H>>>() {
                        public JsResult<T8<A, B, C, D, E, F, G, H>> apply(final B b) {
                            return res3.flatMap(new Function<C, JsResult<T8<A, B, C, D, E, F, G, H>>>() {
                                public JsResult<T8<A, B, C, D, E, F, G, H>> apply(final C c) {
                                    return res4.flatMap(new Function<D, JsResult<T8<A, B, C, D, E, F, G, H>>>() {
                                        public JsResult<T8<A, B, C, D, E, F, G, H>> apply(final D d) {
                                            return res5.flatMap(new Function<E, JsResult<T8<A, B, C, D, E, F, G, H>>>() {
                                                public JsResult<T8<A, B, C, D, E, F, G, H>> apply(final E e) {
                                                    return res6.flatMap(new Function<F, JsResult<T8<A, B, C, D, E, F, G, H>>>() {
                                                        public JsResult<T8<A, B, C, D, E, F, G, H>> apply(final F f) {
                                                            return res7.flatMap(new Function<G, JsResult<T8<A, B, C, D, E, F, G, H>>>() {
                                                                public JsResult<T8<A, B, C, D, E, F, G, H>> apply(final G g) {
                                                                    return res8.map(new Function<H, T8<A, B, C, D, E, F, G, H>>() {
                                                                        public T8<A, B, C, D, E, F, G, H> apply(final H h) {
                                                                            return new T8(a, b, c, d, e, f, g, h);
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }), res1, res2, res3, res4, res5, res6, res7, res8);
        }

        public static <A, B, C, D, E, F, G, H, I> JsResult<T9<A, B, C, D, E, F, G, H, I>> combine(final JsResult<A> res1, final JsResult<B> res2, final JsResult<C> res3, final JsResult<D> res4, final JsResult<E> res5, final JsResult<F> res6, final JsResult<G> res7, final JsResult<H> res8, final JsResult<I> res9) {
            return populateErrs(res1.flatMap(new Function<A, JsResult<T9<A, B, C, D, E, F, G, H, I>>>() {
                public JsResult<T9<A, B, C, D, E, F, G, H, I>> apply(final A a) {
                    return res2.flatMap(new Function<B, JsResult<T9<A, B, C, D, E, F, G, H, I>>>() {
                        public JsResult<T9<A, B, C, D, E, F, G, H, I>> apply(final B b) {
                            return res3.flatMap(new Function<C, JsResult<T9<A, B, C, D, E, F, G, H, I>>>() {
                                public JsResult<T9<A, B, C, D, E, F, G, H, I>> apply(final C c) {
                                    return res4.flatMap(new Function<D, JsResult<T9<A, B, C, D, E, F, G, H, I>>>() {
                                        public JsResult<T9<A, B, C, D, E, F, G, H, I>> apply(final D d) {
                                            return res5.flatMap(new Function<E, JsResult<T9<A, B, C, D, E, F, G, H, I>>>() {
                                                public JsResult<T9<A, B, C, D, E, F, G, H, I>> apply(final E e) {
                                                    return res6.flatMap(new Function<F, JsResult<T9<A, B, C, D, E, F, G, H, I>>>() {
                                                        public JsResult<T9<A, B, C, D, E, F, G, H, I>> apply(final F f) {
                                                            return res7.flatMap(new Function<G, JsResult<T9<A, B, C, D, E, F, G, H, I>>>() {
                                                                public JsResult<T9<A, B, C, D, E, F, G, H, I>> apply(final G g) {
                                                                    return res8.flatMap(new Function<H, JsResult<T9<A, B, C, D, E, F, G, H, I>>>() {
                                                                        public JsResult<T9<A, B, C, D, E, F, G, H, I>> apply(final H h) {
                                                                            return res9.map(new Function<I, T9<A, B, C, D, E, F, G, H, I>>() {
                                                                                public T9<A, B, C, D, E, F, G, H, I> apply(final I i) {
                                                                                    return new T9(a, b, c, d, e, f, g, h, i);
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }), res1, res2, res3, res4, res5, res6, res7, res8, res9);
        }

        public static <A, B, C, D, E, F, G, H, I, J> JsResult<T10<A, B, C, D, E, F, G, H, I, J>> combine(final JsResult<A> res1, final JsResult<B> res2, final JsResult<C> res3, final JsResult<D> res4, final JsResult<E> res5, final JsResult<F> res6, final JsResult<G> res7, final JsResult<H> res8, final JsResult<I> res9, final JsResult<J> res10) {
            return populateErrs(res1.flatMap(new Function<A, JsResult<T10<A, B, C, D, E, F, G, H, I, J>>>() {
                public JsResult<T10<A, B, C, D, E, F, G, H, I, J>> apply(final A a) {
                    return res2.flatMap(new Function<B, JsResult<T10<A, B, C, D, E, F, G, H, I, J>>>() {
                        public JsResult<T10<A, B, C, D, E, F, G, H, I, J>> apply(final B b) {
                            return res3.flatMap(new Function<C, JsResult<T10<A, B, C, D, E, F, G, H, I, J>>>() {
                                public JsResult<T10<A, B, C, D, E, F, G, H, I, J>> apply(final C c) {
                                    return res4.flatMap(new Function<D, JsResult<T10<A, B, C, D, E, F, G, H, I, J>>>() {
                                        public JsResult<T10<A, B, C, D, E, F, G, H, I, J>> apply(final D d) {
                                            return res5.flatMap(new Function<E, JsResult<T10<A, B, C, D, E, F, G, H, I, J>>>() {
                                                public JsResult<T10<A, B, C, D, E, F, G, H, I, J>> apply(final E e) {
                                                    return res6.flatMap(new Function<F, JsResult<T10<A, B, C, D, E, F, G, H, I, J>>>() {
                                                        public JsResult<T10<A, B, C, D, E, F, G, H, I, J>> apply(final F f) {
                                                            return res7.flatMap(new Function<G, JsResult<T10<A, B, C, D, E, F, G, H, I, J>>>() {
                                                                public JsResult<T10<A, B, C, D, E, F, G, H, I, J>> apply(final G g) {
                                                                    return res8.flatMap(new Function<H, JsResult<T10<A, B, C, D, E, F, G, H, I, J>>>() {
                                                                        public JsResult<T10<A, B, C, D, E, F, G, H, I, J>> apply(final H h) {
                                                                            return res9.flatMap(new Function<I, JsResult<T10<A, B, C, D, E, F, G, H, I, J>>>() {
                                                                                public JsResult<T10<A, B, C, D, E, F, G, H, I, J>> apply(final I i) {
                                                                                    return res10.map(new Function<J, T10<A, B, C, D, E, F, G, H, I, J>>() {
                                                                                        public T10<A, B, C, D, E, F, G, H, I, J> apply(final J j) {
                                                                                            return new T10(a, b, c, d, e, f, g, h, i, j);
                                                                                        }
                                                                                    });
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }), res1, res2, res3, res4, res5, res6, res7, res8, res9, res10);
        }
    }

    public static class JsError<T> extends JsResult<T> {

        public final List<Throwable> errors;

        @Override
        public T getValueOrElse(T result) {
            return result;
        }

        @Override
        public T getValueOrElse(Throwable result) {
            throw Throwables.propagate(result);
        }

        public Option<JsError<T>> asError() {
            return Option.some(this);
        }

        public Option<JsSuccess<T>> asSuccess() {
            return Option.none();
        }

        public boolean hasErrors() {
            return true;
        }

        @Override
        public boolean isErrors() {
            return true;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public int countErrors() {
            return errors.size();
        }

        @Override
        public T orError(Throwable t) {
            throw Throwables.propagate(t);
        }

        @Override
        public T get() {
            throw new IllegalStateException("No value");
        }

        @Override
        public Option<T> getOpt() {
            return Option.none();
        }

        @Override
        public JsResult<T> getOrElse(JsResult<T> result) {
            return result;
        }

        @Override
        public <B> JsResult<B> map(Function<T, B> map) {
            return new JsError<B>(errors);
        }

        @Override
        public <B> JsResult<B> flatMap(Function<T, JsResult<B>> map) {
            return new JsError<B>(errors);
        }

        @Override
        public JsResult<T> filter(Function<T, Boolean> predicate) {
            return new JsError<T>(errors);
        }

        @Override
        public JsResult<T> filterNot(Function<T, Boolean> predicate) {
            return new JsError<T>(errors);
        }

        @Override
        public JsResult<T> filter(Function<T, Boolean> predicate, JsResult<T> val, List<Throwable> errs) {
            List<Throwable> thrs = new ArrayList<Throwable>();
            thrs.addAll(this.errors);
            if (val.isSuccess() && predicate.apply(val.get())) {
                thrs.addAll(errs);
            }
            return new JsError<T>(thrs);
        }

        @Override
        public JsResult<T> filterNot(Function<T, Boolean> predicate, JsResult<T> val, List<Throwable> errs) {
            List<Throwable> thrs = new ArrayList<Throwable>();
            thrs.addAll(this.errors);
            if (val.isSuccess() && !predicate.apply(val.get())) {
                thrs.addAll(errs);
            }
            return new JsError<T>(thrs);
        }

        public JsError(List<Throwable> errors) {
            this.errors = errors;
        }

        public JsError(Throwable errors) {
            this.errors = new ArrayList<Throwable>();
            this.errors.add(errors);
        }

        public Throwable firstError() {
            if (errors.isEmpty()) {
                return new IllegalAccessError("No error, that's weird !!!");
            }
            return errors.iterator().next();
        }

        @Override
        public Iterator<T> iterator() {
            return Collections.<T>emptyList().iterator();
        }

        public JsArray errors() {
            return Json.arr(errorsAsString());
        }

        public List<String> errorsAsString() {
            return Lists.newArrayList(Lists.transform(errors, new Function<Throwable, String>() {
                public String apply(Throwable throwable) {
                    return throwable.getMessage();
                }
            }));
        }
    }

    public static class JsSuccess<T> extends JsResult<T> {
        private final T value;

        public JsSuccess(T value) {
            this.value = value;
        }
        public Option<JsError<T>> asError() {
            return Option.none();
        }

        public Option<JsSuccess<T>> asSuccess() {
            return Option.some(this);
        }
        public boolean hasErrors() {
            return false;
        }

        @Override
        public boolean isErrors() {
            return false;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public int countErrors() {
            return 0;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public T orError(Throwable t) {
            return get();
        }

        @Override
        public Option<T> getOpt() {
            return Option.some(value);
        }

        @Override
        public T getValueOrElse(T result) {
            return value;
        }

        @Override
        public T getValueOrElse(Throwable result) {
            return value;
        }

        @Override
        public JsResult<T> getOrElse(JsResult<T> result) {
            return new JsSuccess<T>(value);
        }

        @Override
        public <B> JsResult<B> map(Function<T, B> map) {
            return new JsSuccess<B>(map.apply(value));
        }

        @Override
        public <B> JsResult<B> flatMap(Function<T, JsResult<B>> map) {
            return map.apply(value);
        }

        @Override
        public JsResult<T> filter(final Function<T, Boolean> p) {
            return this.flatMap(new Function<T, JsResult<T>>() {
                public JsResult<T> apply(T a) {
                    if (p.apply(a)) {
                        return new JsSuccess<T>(a);
                    }
                    return new JsError<T>(new ArrayList<Throwable>());
                }
            });
        }

        @Override
        public JsResult<T> filterNot(final Function<T, Boolean> p) {
            return this.flatMap(new Function<T, JsResult<T>>() {
                public JsResult<T> apply(T a) {
                    if (p.apply(a)) {
                        return new JsError<T>(new ArrayList<Throwable>());
                    }
                    return new JsSuccess<T>(a);
                }
            });
        }

        @Override
        public JsResult<T> filter(final Function<T, Boolean> predicate, final JsResult<T> val, final List<Throwable> errors) {
            return this.flatMap(new Function<T, JsResult<T>>() {
                public JsResult<T> apply(T a) {
                    if (predicate.apply(a)) {
                        return new JsSuccess<T>(a);
                    }
                    List<Throwable> ts = new ArrayList<Throwable>();
                    ts.addAll(errors);
                    return new JsError<T>(ts);
                }
            });
        }

        @Override
        public JsResult<T> filterNot(final Function<T, Boolean> predicate, final JsResult<T> val, final List<Throwable> errors) {
            return this.flatMap(new Function<T, JsResult<T>>() {
                public JsResult<T> apply(T a) {
                    if (predicate.apply(a)) {
                        List<Throwable> ts = new ArrayList<Throwable>();
                        ts.addAll(errors);
                        return new JsError<T>(ts);
                    }
                    return new JsSuccess<T>(a);
                }
            });
        }

        @Override
        public Iterator<T> iterator() {
            return Collections.singletonList(value).iterator();
        }
    }

    public static interface Writer<T>  {
        public JsValue write(T value);
    }

    public static interface Reader<T>  {
        public JsResult<T> read(JsValue value);
    }

    public static interface Format<T> extends Reader<T>, Writer<T> {}

    public static class DefaultReaders {

        public static <A> Reader<A> pure(final A a) {
            return new Reader<A>() {
                @Override
                public JsResult<A> read(JsValue value) {
                    return new JsSuccess<A>(a);
                }
            };
        }

        public static final <T> Option<Reader<T>> getReader(Class<T> clazz) {
            Reader<T> val = (Reader<T>) readers.get(clazz);
            if (val == null) return Option.none();
            return Option.some(val);
        }
        public static final Reader<JsObject> JS_OBJECT_READER = new Reader<JsObject>() {
            @Override
            public JsResult<JsObject> read(JsValue value) {
                if (value.is(JsObject.class)) {
                    return new JsSuccess<JsObject>((JsObject) value);
                }
                return new JsError<JsObject>(new IllegalAccessError("Not a JsObject"));
            }
        };
        public static final Reader<JsArray> JS_ARRAY_READER = new Reader<JsArray>() {
            @Override
            public JsResult<JsArray> read(JsValue value) {
                if (value.is(JsArray.class)) {
                    return new JsSuccess<JsArray>((JsArray) value);
                }
                return new JsError<JsArray>(new IllegalAccessError("Not a JsArray"));
            }
        };
        public static final Reader<JsBoolean> JS_BOOLEAN_READER = new Reader<JsBoolean>() {
            @Override
            public JsResult<JsBoolean> read(JsValue value) {
                if (value.is(JsBoolean.class)) {
                    return new JsSuccess<JsBoolean>((JsBoolean) value);
                }
                return new JsError<JsBoolean>(new IllegalAccessError("Not a JsBoolean"));
            }
        };
        public static final Reader<JsPair> JS_PAIR_READER = new Reader<JsPair>() {
            @Override
            public JsResult<JsPair> read(JsValue value) {
                if (value.is(JsPair.class)) {
                    return new JsSuccess<JsPair>((JsPair) value);
                }
                return new JsError<JsPair>(new IllegalAccessError("Not a JsPair"));
            }
        };
        public static final Reader<JsNull> JS_NULL_READER = new Reader<JsNull>() {
            @Override
            public JsResult<JsNull> read(JsValue value) {
                if (value.is(JsNull.class)) {
                    return new JsSuccess<JsNull>((JsNull) value);
                }
                return new JsError<JsNull>(new IllegalAccessError("Not a JsNull"));
            }
        };
        public static final Reader<JsUndefined> JS_UNDEFINED_READER = new Reader<JsUndefined>() {
            @Override
            public JsResult<JsUndefined> read(JsValue value) {
                if (value.is(JsUndefined.class)) {
                    return new JsSuccess<JsUndefined>((JsUndefined) value);
                }
                return new JsError<JsUndefined>(new IllegalAccessError("Not a JsUndefined"));
            }
        };
        public static final Reader<JsonFormattedValue> JS_FORMATTED_READER = new Reader<JsonFormattedValue>() {
            @Override
            public JsResult<JsonFormattedValue> read(JsValue value) {
                if (value.is(JsonFormattedValue.class)) {
                    return new JsSuccess<JsonFormattedValue>((JsonFormattedValue) value);
                }
                return new JsError<JsonFormattedValue>(new IllegalAccessError("Not a JsonFormattedValue"));
            }
        };
        public static final Reader<JsNumber> JS_NUMBER_READER = new Reader<JsNumber>() {
            @Override
            public JsResult<JsNumber> read(JsValue value) {
                if (value.is(JsNumber.class)) {
                    return new JsSuccess<JsNumber>((JsNumber) value);
                }
                return new JsError<JsNumber>(new IllegalAccessError("Not a JsNumber"));
            }
        };
        public static final Reader<JsString> JS_STRING_READER = new Reader<JsString>() {
            @Override
            public JsResult<JsString> read(JsValue value) {
                if (value.is(JsString.class)) {
                    return new JsSuccess<JsString>((JsString) value);
                }
                return new JsError<JsString>(new IllegalAccessError("Not a JsString"));
            }
        };
        public static final Reader<Boolean> BOOLEAN_READER = new Reader<Boolean>() {
            @Override
            public JsResult<Boolean> read(JsValue value) {
                if (value.is(JsBoolean.class)) {
                    return new JsSuccess<Boolean>(((JsBoolean) value).value);
                }
                return new JsError<Boolean>(new IllegalAccessError("Not a JsBoolean"));
            }
        };
        public static final Reader<String> STRING_READER = new Reader<String>() {
            @Override
            public JsResult<String> read(JsValue value) {
                if (value.is(JsString.class)) {
                    return new JsSuccess<String>(((JsString) value).value);
                }
                return new JsError<String>(new IllegalAccessError("Not a JsString"));
            }
        };
        public static final Reader<Double> DOUBLE_READER = new Reader<Double>() {
            @Override
            public JsResult<Double> read(JsValue value) {
                if (value.is(JsNumber.class)) {
                    return new JsSuccess<Double>(((JsNumber) value).value.doubleValue());
                }
                return new JsError<Double>(new IllegalAccessError("Not a JsNumber"));
            }
        };
        public static final Reader<Long> LONG_READER = new Reader<Long>() {
            @Override
            public JsResult<Long> read(JsValue value) {
                if (value.is(JsNumber.class)) {
                    return new JsSuccess<Long>(((JsNumber) value).value.longValue());
                }
                return new JsError<Long>(new IllegalAccessError("Not a JsNumber"));
            }
        };
        public static final Reader<Integer> INTEGER_READER = new Reader<Integer>() {
            @Override
            public JsResult<Integer> read(JsValue value) {
                if (value.is(JsNumber.class)) {
                    return new JsSuccess<Integer>(((JsNumber) value).value.intValue());
                }
                return new JsError<Integer>(new IllegalAccessError("Not a JsNumber"));
            }
        };
        public static final Reader<BigDecimal> BIGDEC_READER = new Reader<BigDecimal>() {
            @Override
            public JsResult<BigDecimal> read(JsValue value) {
                if (value.is(JsNumber.class)) {
                    return new JsSuccess<BigDecimal>(((JsNumber) value).value);
                }
                return new JsError<BigDecimal>(new IllegalAccessError("Not a JsNumber"));
            }
        };
        public static final Reader<BigInteger> BIGINT_READER = new Reader<BigInteger>() {
            @Override
            public JsResult<BigInteger> read(JsValue value) {
                if (value.is(JsNumber.class)) {
                    return new JsSuccess<BigInteger>(((JsNumber) value).value.toBigInteger());
                }
                return new JsError<BigInteger>(new IllegalAccessError("Not a JsNumber"));
            }
        };
        public static final Reader<DateTime> DATETIME_READER = new Reader<DateTime>() {
            @Override
            public JsResult<DateTime> read(JsValue value) {
                if (value.is(JsString.class)) {
                    try {
                    return new JsSuccess<DateTime>(DateTime.parse(value.as(String.class)));
                    } catch (Exception e) {
                        return new JsError<DateTime>(e);
                    }
                }
                return new JsError<DateTime>(new IllegalAccessError("Not a JsString"));
            }
        };
        public static final Reader<JsValue> JSVALUE_READER = new Reader<JsValue>() {
            @Override
            public JsResult<JsValue> read(JsValue value) {
                    return new JsSuccess<JsValue>(value);
            }
        };
        private static final Map<Class<?>, Reader<?>> readers = new HashMap<Class<?>, Reader<?>>() {{
            put(JsObject.class, JS_OBJECT_READER);
            put(JsArray.class, JS_ARRAY_READER);
            put(JsBoolean.class, JS_BOOLEAN_READER);
            put(JsPair.class, JS_PAIR_READER);
            put(JsNull.class, JS_NULL_READER);
            put(JsUndefined.class, JS_UNDEFINED_READER);
            put(JsonFormattedValue.class, JS_FORMATTED_READER);
            put(JsNumber.class , JS_NUMBER_READER);
            put(JsString.class, JS_STRING_READER);
            put(Boolean.class, BOOLEAN_READER);
            put(String.class, STRING_READER);
            put(Double.class, DOUBLE_READER);
            put(Long.class, LONG_READER);
            put(Integer.class, INTEGER_READER);
            put(BigDecimal.class, BIGDEC_READER);
            put(BigInteger.class, BIGINT_READER);
            put(JsValue.class, JSVALUE_READER);
            put(DateTime.class, DATETIME_READER);
        }};
    }

    public static class ReaderConstraints {
        public static Reader<String> email(final Reader<String> reads) {
            return matches("[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[a-zA-Z0-9](?:[\\w-]*[\\w])?", reads);
        }
        public static Reader<String> url(final Reader<String> reads) {
            return matches("^(http|https|ftp)\\://[a-zA-Z0-9\\-\\.]+\\.[a-z" +
                    "A-Z]{2,3}(:[a-zA-Z0-9]*)?/?([a-zA-Z0-9\\-\\._\\?\\,\\'/\\\\\\+&amp;%\\$#\\=~\\!])*$", reads);
        }
        public static Reader<String> phone(final Reader<String> reads) {
            return matches("^([\\+][0-9]{1,3}([ \\.\\-]))?([\\(]{1}[0-9]{2,6}[\\)])?([0-9 \\.\\-/]{3,20})((x|ext|extension)[ ]?[0-9]{1,4})?$", reads);
        }
        public static Reader<Integer> max(final int max, final Reader<Integer> reads) {
           return new Reader<Integer>() {
               @Override
               public JsResult<Integer> read(JsValue value) {
                   JsResult<Integer> jsr = reads.read(value);
                   return jsr.filterNot(new Function<Integer, Boolean>() {
                       @Override
                       public Boolean apply(Integer val) {
                           return val > max;
                       }
                   }, jsr, Lists.<Throwable>newArrayList(new IllegalStateException("Value is to big.")));
               }
           };
        }
        public static Reader<Integer> min(final int min, final Reader<Integer> reads) {
            return new Reader<Integer>() {
                @Override
                public JsResult<Integer> read(JsValue value) {
                    JsResult<Integer> jsr = reads.read(value);
                    return jsr.filterNot(new Function<Integer, Boolean>() {
                        @Override
                        public Boolean apply(Integer val) {
                            return val < min;
                        }
                    }, jsr, Lists.<Throwable>newArrayList(new IllegalStateException("Value is to small.")));
                }
            };
        }
        public static Reader<String> matches(final String pattern, final Reader<String> reads) {
            return new Reader<String>() {
                @Override
                public JsResult<String> read(JsValue value) {
                    JsResult<String> jsr = reads.read(value);
                    return jsr.filter(new Function<String, Boolean>() {
                        @Override
                        public Boolean apply(String val) {
                            return val.matches(pattern);
                        }
                    }, jsr, Lists.<Throwable>newArrayList(new IllegalStateException("Value does not match.")));
                }
            };
        }
        public static <A> Reader<A> verifying(final Predicate<A> p, final Reader<A> reads) {
            return new Reader<A>() {
                @Override
                public JsResult<A> read(JsValue value) {
                    JsResult<A> jsr = reads.read(value);
                    return jsr.filter(new Function<A, Boolean>() {
                        @Override
                        public Boolean apply(A val) {
                            return p.apply(val);
                        }
                    }, jsr, Lists.<Throwable>newArrayList(new IllegalStateException("Value does not verify.")));
                }
            };
        }
    }

    public static abstract class JsValue implements Serializable {
        public <T extends JsValue> Iterable<T> extractAs(Class<T> clazz) {
            if (is(clazz)) {
                return Collections.singletonList(clazz.cast(this));
            }
            return Collections.emptyList();
        }
        public <T extends JsValue> boolean is(Class<T> clazz) {
            return clazz.isAssignableFrom(this.getClass());
        }
        public abstract String toJsonString();
        public abstract JsonElement toJsonElement();
        public <T> T as(Class<T> clazz, Reader<T> reader) {
            return reader.read(this).getOpt().get();
        }
        public <T> T as(Class<T> clazz) {
            return asOpt(clazz).get();
        }
        public <T> Option<T> asOpt(Class<T> clazz, Reader<T> reader) {
            return reader.read(this).getOpt();
        }
        public <T> Option<T> asOpt(Class<T> clazz) {
            for (Reader<T> reader : DefaultReaders.getReader(clazz)) {
                return reader.read(this).getOpt();
            }
            return Option.none();
        }
        public <T> JsResult<T> read(Class<T> clazz) {
            for (Reader<T> reader : DefaultReaders.getReader(clazz)) {
                return reader.read(this);
            }
            return new JsError<T>(new IllegalStateException("Cannot find reader for type " + clazz.getName()));
        }
        public <T> JsResult<T> read(Reader<T> reader) {
            return reader.read(this);
        }
        public <T> JsResult<T> validate(Reader<T> reader) {
            return reader.read(this);
        }
        public <A extends JsValue> JsResult<A> transform(Reader<A> reader) {
            return reader.read(this);
        }
        public Boolean exists(String field) {
            return false;
        }
        public JsValue field(String field) {
            return JSUNDEFINED_INSTANCE;
        }
        public Option<JsValue> fieldAsOpt(String field) {
            return Option.none();
        }
        public List<JsValue> fields(String fieldName) {
            return Lists.newArrayList();
        }
        public JsValue _(String field) {
            return this.field(field);
        }
        public List<JsValue> __(String fieldName) {
            return this.fields(fieldName);
        }
        public JsValue get(int idx) {
            return JSUNDEFINED_INSTANCE;
        }
    }
    public static class JsArray extends JsValue implements Iterable<JsValue> {
        public final List<JsValue> values;
        private final transient JsonArray jsonArray;
        public JsArray(List<JsValue> values) {
            if (values == null) throw new IllegalArgumentException("Values can't be null !");
            this.values = values;
            this.jsonArray = new JsonArray();
            for (JsValue value : values) {
                if (value != null) jsonArray.add(value.toJsonElement());
            }
        }
        public JsArray() {
            this.values = new ArrayList<JsValue>();
            this.jsonArray = new JsonArray();
        }

        @Override
        public Iterator<JsValue> iterator() {
            return values.iterator();
        }

        public JsValue get(int idx) {
            try {
                return values.get(idx);
            }catch (Exception e) {
                return JSUNDEFINED_INSTANCE;
            }
        }
        public JsArray append(JsArray arr) {
            if (arr == null) return new JsArray(values);
            List<JsValue> vals = values;
            vals.addAll(arr.values);
            return new JsArray(vals);
        }
        public JsArray preprend(JsArray arr) {
            if (arr == null) return new JsArray(values);
            List<JsValue> vals = values;
            vals.addAll(0, arr.values);
            return new JsArray(vals);
        }
        public JsArray addElement(JsValue arr) {
            if (arr == null) return new JsArray(values);
            List<JsValue> vals = values;
            vals.add(arr);
            return new JsArray(vals);
        }
        public JsArray preprendElement(JsValue arr) {
            if (arr == null) return new JsArray(values);
            List<JsValue> vals = values;
            vals.add(0, arr);
            return new JsArray(vals);
        }
        public JsArray map(Function<JsValue, JsValue> map) {
            return new JsArray(Lists.newArrayList(Lists.transform(values, map)));
        }
        public <T> List<T> mapWith(Reader<T> reader) {
            List<T> resultList = new ArrayList<T>();
            for (JsValue value : this.values) {
                JsResult<T> result = value.read(reader);
                if (result.hasErrors()) {
                    throw Throwables.propagate(result.asError().get().firstError());
                }
                resultList.add(result.get());
            }
            return resultList;
        }
        public <T> List<T> mapWith(Reader<T> reader, Function<JsResult<T>, T> onError) {
            List<T> resultList = new ArrayList<T>();
            for (JsValue value : this.values) {
                T v = null;
                JsResult<T> result = value.read(reader);
                if (result.hasErrors()) {
                    v = onError.apply(result);
                } else {
                    v = result.get();
                }
                resultList.add(v);
            }
            return resultList;
        }
        public JsArray filter(Predicate<JsValue> predicate) {
            return new JsArray(Lists.newArrayList(Iterables.filter(values, predicate)));
        }
        public JsArray filterNot(final Predicate<JsValue> predicate) {
            Predicate<JsValue> p = new Predicate<JsValue>() {
                public boolean apply(JsValue jsValue) {
                    return !predicate.apply(jsValue);
                }
            };
            return new JsArray(Lists.newArrayList(Iterables.filter(values, p)));
        }
        public String toJsonString() {
            if (delegatingToGson()) {
                return new Gson().toJson(toJsonElement());
            }
            return "[" + Joiner.on(",").join(Lists.transform(values, new Function<JsValue, String>() {
                public String apply(JsValue jsValue) {
                    return jsValue.toJsonString();
                }
            })) + "]";
        }
        public String toString() {
            return "JsArray[" + Joiner.on(", ").join(values) + "]";
        }
        public JsonElement toJsonElement() {
            return jsonArray;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof JsArray)) return false;
            JsArray jsArray = (JsArray) o;
            if (!values.equals(jsArray.values)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return values.hashCode();
        }
    }
    public static class JsBoolean extends JsValue implements java.lang.Comparable<JsBoolean> {
        public final Boolean value;
        private final transient JsonPrimitive primitive;
        public JsBoolean(Boolean value) {
            if (value == null) throw new IllegalArgumentException("Value can't be null !");
            this.value = value;
            this.primitive = new JsonPrimitive(value);
        }

        @Override
        public int compareTo(JsBoolean aBoolean) {
            return value.compareTo(aBoolean.value);
        }

        public String toJsonString() {
            if (delegatingToGson()) {
                return new Gson().toJson(toJsonElement());
            }
            if (value == null) {
                return Boolean.FALSE.toString();
            }
            return value.toString();
        }
        public String toString() {
            return "JsBoolean(" + value + ")";
        }
        public JsonElement toJsonElement() {
            return primitive;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof JsBoolean)) return false;
            JsBoolean jsBoolean = (JsBoolean) o;
            if (!value.equals(jsBoolean.value)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }
    public static class JsPair extends JsObject {
        public JsPair(String name, JsValue value) {
            super(asMap(name, value));
        }
        public JsPair(String name, Long value) {
            super(asMap(name, new JsNumber(value)));
        }
        public JsPair(String name, Integer value) {
            super(asMap(name, new JsNumber(value)));
        }
        public JsPair(String name, Double value) {
            super(asMap(name, new JsNumber(value)));
        }
        public JsPair(String name, BigDecimal value) {
            super(asMap(name, new JsNumber(value)));
        }
        public JsPair(String name, BigInteger value) {
            super(asMap(name, new JsNumber(value)));
        }
        public JsPair(String name) {
            super(asMap(name, JSNULL_INSTANCE));
        }
        public JsPair(String name, String value) {
            super(asMap(name, new JsString(value)));
        }
        public JsPair(String name, Boolean value) {
            super(asMap(name, new JsBoolean(value)));
        }
        public String toString() {
            return super.toString();
        }
    }
    public static class JsObject extends JsValue implements Iterable<Map.Entry<String, JsValue>> {
        public final Map<String, JsValue> values;
        private final transient JsonObject jsonObject;
        public JsObject(Map<String, JsValue> values) {
            if (values == null) throw new IllegalArgumentException("Values can't be null !");
            this.values = values;
            this.jsonObject = new JsonObject();
            for (Map.Entry<String, JsValue> entry : values.entrySet()) {
                this.jsonObject.add(entry.getKey(), entry.getValue().toJsonElement());
            }
        }
        public JsObject() {
            this.values = new HashMap<String, JsValue>();
            this.jsonObject = new JsonObject();
        }
        public JsObject merge(JsObject with) {
            if (with == null) throw new IllegalArgumentException("Value can't be null !");
            Map<String, JsValue> newValues = new HashMap<String, JsValue>();
            newValues.putAll(with.values);
            newValues.putAll(values);
            return new JsObject(newValues);
        }

        @Override
        public Iterator<Map.Entry<String, JsValue>> iterator() {
            return values.entrySet().iterator();
        }

        public JsObject deepMerge(JsObject with) {
            if (with == null) throw new IllegalArgumentException("Value can't be null !");
            Map<String, JsValue> newValues = new HashMap<String, JsValue>();
            newValues.putAll(with.values);
            for (Map.Entry<String, JsValue> entry : values.entrySet()) {
               if (with.values.containsKey(entry.getKey()) && entry.getValue().is(JsObject.class)) {
                   newValues.put(entry.getKey(), entry.getValue().as(JsObject.class).deepMerge(with.values.get(entry.getKey()).as(JsObject.class)));
               } else {
                   newValues.put(entry.getKey(), entry.getValue());
               }
            }
            return new JsObject(newValues);
        }
        public Set<String> fieldsSet() {
            return values.keySet();
        }
        public Collection<JsValue> values() {
            return values.values();
        }
        public JsObject add(JsObject jsObject) {
            if (jsObject == null) return new JsObject(values);
            Map<String, JsValue> newValues = values;
            newValues.putAll(jsObject.values);
            return new JsObject(newValues);
        }
        public JsObject remove(String field) {
            if (field == null) return new JsObject(values);
            values.remove(field);
            return new JsObject(values);
        }
        public JsValue field(String field) {
            if (field == null) return JSUNDEFINED_INSTANCE;
            JsValue value = values.get(field);
            if (value == null) return JSUNDEFINED_INSTANCE;
            return value;
        }
        public Option<JsValue> fieldAsOpt(String field) {
            if (field == null) return Option.none();
            JsValue val = values.get(field);
            if (val == null) {
                return Option.none();
            }
            return Option.some(val);
        }
        public List<JsValue> fields(String fieldName) {
            if (fieldName == null) return Collections.emptyList();
            List<JsValue> vals = new ArrayList<JsValue>();
            for (Map.Entry<String, JsValue> field : values.entrySet()) {
                if (field.getKey().equals(fieldName)) {
                    vals.add(field.getValue());
                }
                for (JsObject obj : field.getValue().asOpt(JsObject.class)) {
                    vals.addAll(obj.fields(fieldName));
                }
                for (JsObject obj : field.getValue().asOpt(JsPair.class)) {
                    vals.addAll(obj.fields(fieldName));
                }
            }
            return vals;
        }
        @Override
        public String toJsonString() {
            if (delegatingToGson()) {
                return new Gson().toJson(toJsonElement());
            }
            return "{" + toJsonPairString() + "}";
        }

        public String toString() {
            return "JsObject(" + toJsonPairString() + ")";
        }

        public String toJsonPairString() {
            return Joiner.on(",").join(Iterables.transform(values.entrySet(), new Function<Map.Entry<String, JsValue>, String>() {
                public String apply(Map.Entry<String, JsValue> entry) {
                    return "\"" + entry.getKey() + "\":" + entry.getValue().toJsonString();
                }
            }));
        }
        public JsonElement toJsonElement() {
            return jsonObject;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof JsObject)) return false;
            JsObject object = (JsObject) o;
            if (!values.equals(object.values)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return values.hashCode();
        }
        public Boolean exists(String field) {
            return values.containsKey(field);
        }
        public JsObject mapProperties(Function<Tuple<String, JsValue>, JsValue> block) {
            Map<String, JsValue> resulting = new HashMap<String, JsValue>();
            for (Map.Entry<String, JsValue> entry : values.entrySet()) {
                JsValue tuple = block.apply(new Tuple<String, JsValue>(entry.getKey(), entry.getValue()));
                resulting.put(entry.getKey(), tuple);
            }
            return new JsObject(resulting);
        }

        public <T> Map<String, T> mapPropertiesWith(Reader<T> reader) {
            Map<String, T> resultMap = new HashMap<String, T>();
            for (Map.Entry<String, JsValue> entry : values.entrySet()) {
                JsResult<T> result = reader.read(entry.getValue());
                if (result.hasErrors()) {
                    throw Throwables.propagate(result.asError().get().firstError());
                }
                resultMap.put(entry.getKey(), result.get());
            }
            return resultMap;
        }

        public <T> Map<String, T> mapPropertiesWith(Reader<T> reader, Function<JsResult<T>, T> onError) {
            Map<String, T> resultMap = new HashMap<String, T>();
            for (Map.Entry<String, JsValue> entry : values.entrySet()) {
                JsResult<T> result = reader.read(entry.getValue());
                if (result.hasErrors()) {
                    resultMap.put(entry.getKey(), onError.apply(result));
                } else {
                    resultMap.put(entry.getKey(), result.get());
                }
            }
            return resultMap;
        }
    }

    public static class JsNull extends JsValue {
        public String toJsonString() {
            if (delegatingToGson()) {
                return new Gson().toJson(toJsonElement());
            }
            return "null";
        }
        public JsonElement toJsonElement() {
            return JsonNull.INSTANCE;
        }
        public String toString() {
            return "JsNull()";
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof JsNull)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return 42;
        }
    }
    public static class JsUndefined extends JsValue {
        public String toJsonString() {
            return "undefined";
        }
        public String toString() {
            return "JsUndefined()";
        }
        public JsonElement toJsonElement() {
            throw new IllegalStateException("Can't process 'undefined' with Gson");
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof JsUndefined)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return 42;
        }
    }
    /**
     * To wrap already JSON formatted data
     */
    public static class JsonFormattedValue extends JsValue {
        public final String value;
        private final transient JsonElement element;
        public JsonFormattedValue(String value) {
            if (value == null) throw new IllegalArgumentException("Value can't be null !");
            this.value = value;
            this.element = new JsonParser().parse(value);
        }
        public String toJsonString() {
            if (delegatingToGson()) {
                return new Gson().toJson(toJsonElement());
            }
            return value;
        }
        public String toString() {
            return "Json(" + value + ")";
        }
        public JsonElement toJsonElement() {
            return element;
        }

    }
    public static class JsNumber extends JsValue implements java.lang.Comparable<JsNumber> {
        public final BigDecimal value;
        private final transient JsonPrimitive number;
        public JsNumber(BigDecimal value) {
            if (value == null) throw new IllegalArgumentException("Value can't be null !");
            this.value = value;
            this.number = new JsonPrimitive(value);
        }
        public JsNumber(BigInteger value) {
            if (value == null) throw new IllegalArgumentException("Value can't be null !");
            this.value = new BigDecimal(value);
            this.number = new JsonPrimitive(value);
        }
        public JsNumber(Integer value) {
            if (value == null) throw new IllegalArgumentException("Value can't be null !");
            this.value = BigDecimal.valueOf(value).setScale(0);
            this.number = new JsonPrimitive(value);
        }
        public JsNumber(Long value) {
            if (value == null) throw new IllegalArgumentException("Value can't be null !");
            this.value = BigDecimal.valueOf(value).setScale(0);
            this.number = new JsonPrimitive(value);
        }
        public JsNumber(Double value) {
            if (value == null) throw new IllegalArgumentException("Value can't be null !");
            this.value = BigDecimal.valueOf(value);
            this.number = new JsonPrimitive(value);
        }
        @Override
        public int compareTo(JsNumber jsNumber) {
            return value.compareTo(jsNumber.value);
        }
        public String toJsonString() {
            if (delegatingToGson()) {
                return new Gson().toJson(toJsonElement());
            }
            return value.toString();
        }
        public String toString() {
            return "JsNumber(" + value.toString() + ")";
        }
        public JsonElement toJsonElement() {
            return number;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof JsNumber)) return false;
            JsNumber jsNumber = (JsNumber) o;
            if (!value.equals(jsNumber.value)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }
    public static class JsString extends JsValue implements java.lang.Comparable<JsString> {
        public final String value;
        private final transient JsonPrimitive primitive;
        public JsString(String value) {
            if (value == null) {
                value = "";
            }
            this.primitive = new JsonPrimitive(value);
            //if (!value.isEmpty()) {
            //    String v = new Gson().toJson(value);
            //    this.value = v.substring(1, v.length() - 1);
            //} else {
            this.value = value;
            //}
        }

        @Override
        public int compareTo(JsString jsString) {
            return value.compareTo(jsString.value);
        }

        public String toJsonString() {
            if (delegatingToGson()) {
                return new Gson().toJson(toJsonElement());
            }
            /*if (!value.isEmpty()) {
                return value;
            } else { */
            //return "\"" + value + "\"";
            //}
            return new Gson().toJson(value);
        }
        public String toString() {
            return "JsString(" + value + ")";
        }
        public JsonElement toJsonElement() {
            return primitive;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof JsString)) return false;

            JsString jsString = (JsString) o;

            if (!value.equals(jsString.value)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    public static class Json {

        public static <T> Format<T> format(final Class<T> clazz) {
            final Writer<T> writer = Json.writes(clazz);
            final Reader<T> reader = Json.reads(clazz);
            return new Format<T>() {
                @Override
                public JsResult<T> read(JsValue value) {
                    return reader.read(value);
                }

                @Override
                public JsValue write(T value) {
                    return writer.write(value);
                }
            };
        }

        public static <T> Reader<T> reads(final Class<T> clazz) {
            if (DefaultReaders.readers.containsKey(clazz)) {
                return (Reader<T>) DefaultReaders.readers.get(clazz);
            }
            return new Reader<T>() {
                @Override
                public JsResult<T> read(JsValue value) {
                    try {
                        return new JsSuccess<T>(new Gson().fromJson(value.toJsonString(), clazz));
                    } catch(Exception e) {
                        return new JsError<T>(Collections.<Throwable>singletonList(e));
                    }
                }
            };
        }

        public static <T> Writer<T> writes(final Class<T> clazz) {
            return new Writer<T>() {
                @Override
                public JsValue write(T value) {
                    return Json.parse(new Gson().toJson(value));
                }
            };
        }

        public static JsValue toJson(Object o) {
            JsonElement jsonElement = null;
            if (o instanceof JsonElement) {
                jsonElement = (JsonElement) o;
            } else {
                jsonElement = new Gson().toJsonTree(o);
            }
            return toJson(jsonElement);
        }

        private static JsValue toJson(JsonElement jsonElement) {
            if (jsonElement.isJsonArray()) {
                JsonArray arr = jsonElement.getAsJsonArray();
                return new JsArray(Lists.newArrayList(Iterables.transform(arr, new Function<JsonElement, JsValue>() {
                    public JsValue apply(JsonElement jsonElement) {
                        return toJson(jsonElement);
                    }
                })));
            }
            if (jsonElement.isJsonNull()) {
                return JSNULL_INSTANCE;
            }
            if (jsonElement.isJsonObject()) {
                JsonObject obj = jsonElement.getAsJsonObject();
                Iterable<Map.Entry<String, JsValue>> values = Iterables.transform(obj.entrySet(), new Function<Map.Entry<String, JsonElement>, Map.Entry<String, JsValue>>() {
                    public Map.Entry<String, JsValue> apply(Map.Entry<String, JsonElement> entry) {
                        return new AbstractMap.SimpleEntry<String, JsValue>(entry.getKey(), toJson(entry.getValue()));
                    }
                });
                Map<String, JsValue> map = new HashMap<String, JsValue>();
                for (Map.Entry<String, JsValue> value : values) {
                    map.put(value.getKey(), value.getValue());
                }
                return new JsObject(map);
            }
            if (jsonElement.isJsonPrimitive()) {
                JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
                if (primitive.isBoolean()) {
                    return new JsBoolean(primitive.getAsBoolean());
                }
                if (primitive.isString()) {
                   return new JsString(primitive.getAsString());
                }
                if (primitive.isNumber()) {
                    try {
                        return new JsNumber(primitive.getAsLong());
                    } catch (Exception e) {}
                    return new JsNumber(BigDecimal.valueOf(primitive.getAsNumber().doubleValue()));
                }
            }
            throw new RuntimeException("Unknow Json type : " + jsonElement);
        }

        public static JsValue parse(String json) {
            return toJson(new JsonParser().parse(json));
        }

        public static JsObject obj(Iterable<? extends JsObject> objects) {
            JsObject root = new JsObject();
            for (JsObject object : objects) {
                root = root.add(object);
            }
            return root;
        }

        public static JsObject obj(JsObject... objects) {
            return obj(Arrays.asList(objects));
        }

        public static JsArray array(Iterable<? extends Object> objects) {
            return new JsArray(Lists.newArrayList(Lists.transform(Lists.newArrayList(objects), new Function<Object, JsValue>() {
                public JsValue apply(Object o) {
                    return wrap(o);
                }
            })));
        }

        public static JsArray arr(Object... objects) {
            if (objects != null && objects.length == 1 && Iterable.class.isAssignableFrom(objects[0].getClass())) {
               return array((Iterable<Object>) objects[0]);
            }
            List<Object> objs = Arrays.asList(objects);
            return array(objs);
        }
        public static <T> JsArray arr(Iterable<T> collection, final Writer<T> writer) {
            return Json.arr(Iterables.transform(collection, new Function<T, JsValue>() {
                @Override
                public JsValue apply(T t) {
                    return writer.write(t);
                }
            }));
        }

        public static String stringify(JsValue value) {
            return value.toJsonString();
        }

        public static String stringify(JsValue value, boolean pretty) {
            if (pretty) return prettyPrint(value);
            return stringify(value);
        }

        public static JsValue wrap(Object o) {
            if (o == null) {
                return JSNULL_INSTANCE;
            }
            if (Iterable.class.isAssignableFrom(o.getClass()) && !( o instanceof JsObject) && !(o instanceof JsArray)) {
                return new JsArray(Lists.newArrayList(Lists.transform(Lists.newArrayList((Iterable<Object>) o), new Function<Object, JsValue>() {
                    public JsValue apply(Object o) {
                        return wrap(o);
                    }
                })));
            }
            if (o instanceof JsValue) {
                return (JsValue) o;
            }
            if (o instanceof String) {
                return new JsString((String) o);
            }
            if (o instanceof Boolean) {
                return new JsBoolean((Boolean) o);
            }
            if (o instanceof Integer) {
                return new JsNumber((Integer) o);
            }
            if (o instanceof Long) {
                return new JsNumber((Long) o);
            }
            if (o instanceof Double) {
                return new JsNumber((Double) o);
            }
            if (o instanceof BigDecimal) {
                return new JsNumber((BigDecimal) o);
            }
            if (o instanceof BigInteger) {
                return new JsNumber((BigInteger) o);
            }
            if (o instanceof Date) {
                return new JsString(new DateTime((Date) o).toString("dd-MM-yyyy HH:mm:ss"));
            }
            if (o instanceof java.sql.Date) {
                return new JsString(new DateTime((Date) o).toString("dd-MM-yyyy HH:mm:ss"));
            }
            if (o instanceof Time) {
                return new JsString(((Time) o).toString());
            }
            if (o instanceof Timestamp) {
                return new JsNumber(((Timestamp) o).getTime());
            }
            if (o instanceof DateTime) {
                return new JsString(((DateTime) o).toString("dd-MM-yyyy HH:mm:ss"));
            }
            throw new RuntimeException("Unprocessable type " + o.getClass());
        }

        public static <T> JsResult<T> fromJson(JsValue value, Reader<T> reader) {
            return reader.read(value);
        }

        public static <T> JsResult<T> fromJson(String value, Reader<T> reader) {
            return reader.read(Json.parse(value));
        }

        public static <T> JsValue toJson(T o, Writer<T> writer) {
            return writer.write(o);
        }

        public static String prettyPrint(JsValue value) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(new JsonParser().parse(value.toJsonString()));
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

    private static final JsNull JSNULL_INSTANCE = new JsNull();
    public static JsNull nill() {
        return JSNULL_INSTANCE;
    }

    private static final JsUndefined JSUNDEFINED_INSTANCE = new JsUndefined();
    public static JsUndefined undefined() {
        return JSUNDEFINED_INSTANCE;
    }

    private static Map<String, JsValue> asMap(String name, JsValue value) {
        Map<String, JsValue> values = new HashMap<String, JsValue>();
        values.put(name, value);
        return values;
    }
}
