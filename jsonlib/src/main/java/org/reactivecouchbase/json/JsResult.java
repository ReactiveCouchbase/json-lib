package org.reactivecouchbase.json;

import com.google.common.base.Function;
import org.reactivecouchbase.common.Functionnal;

import java.util.ArrayList;
import java.util.List;

public abstract class JsResult<T> implements Iterable<T> {

    public abstract T get();

    public abstract Functionnal.Option<T> getOpt();

    public abstract JsResult<T> getOrElse(JsResult<T> result);

    public abstract T getValueOrElse(T result);

    public abstract T getValueOrElse(Throwable result);

    public abstract <B> JsResult<B> map(Function<T, B> map);

    public abstract <B> JsResult<B> flatMap(Function<T, JsResult<B>> map);

    public abstract JsResult<T> filter(Function<T, Boolean> predicate);

    public abstract JsResult<T> filterNot(Function<T, Boolean> predicate);

    public abstract JsResult<T> filter(Function<T, Boolean> predicate, List<Throwable> errors);

    public abstract JsResult<T> filterNot(Function<T, Boolean> predicate, List<Throwable> errors);

    public abstract JsResult<T> filter(Function<T, Boolean> predicate, Throwable error);

    public abstract JsResult<T> filterNot(Function<T, Boolean> predicate, Throwable error);

    public abstract boolean hasErrors();

    public abstract boolean isErrors();

    public abstract boolean isSuccess();

    public abstract int countErrors();

    public abstract Functionnal.Option<JsError<T>> asError();

    public abstract Functionnal.Option<JsSuccess<T>> asSuccess();

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

    public static <A, B> JsResult<Functionnal.T2<A, B>> combine(final JsResult<A> res1, final JsResult<B> res2) {
        return populateErrs(res1.flatMap(new Function<A, JsResult<Functionnal.T2<A, B>>>() {
            public JsResult<Functionnal.T2<A, B>> apply(final A a) {
                return res2.map(new Function<B, Functionnal.T2<A, B>>() {
                    public Functionnal.T2<A, B> apply(B b) {
                        return new Functionnal.T2<A, B>(a, b);
                    }
                });
            }
        }), res1, res2);
    }

    public static <A, B, C> JsResult<Functionnal.T3<A, B, C>> combine(final JsResult<A> res1, final JsResult<B> res2, final JsResult<C> res3) {
        return populateErrs(res1.flatMap(new Function<A, JsResult<Functionnal.T3<A, B, C>>>() {
            public JsResult<Functionnal.T3<A, B, C>> apply(final A a) {
                return res2.flatMap(new Function<B, JsResult<Functionnal.T3<A, B, C>>>() {
                    public JsResult<Functionnal.T3<A, B, C>> apply(final B b) {
                        return res3.map(new Function<C, Functionnal.T3<A, B, C>>() {
                            public Functionnal.T3<A, B, C> apply(final C c) {
                                return new Functionnal.T3<A, B, C>(a, b, c);
                            }
                        });
                    }
                });
            }
        }), res1, res2, res3);
    }

    public static <A, B, C, D> JsResult<Functionnal.T4<A, B, C, D>> combine(final JsResult<A> res1, final JsResult<B> res2, final JsResult<C> res3, final JsResult<D> res4) {
        return populateErrs(res1.flatMap(new Function<A, JsResult<Functionnal.T4<A, B, C, D>>>() {
            public JsResult<Functionnal.T4<A, B, C, D>> apply(final A a) {
                return res2.flatMap(new Function<B, JsResult<Functionnal.T4<A, B, C, D>>>() {
                    public JsResult<Functionnal.T4<A, B, C, D>> apply(final B b) {
                        return res3.flatMap(new Function<C, JsResult<Functionnal.T4<A, B, C, D>>>() {
                            public JsResult<Functionnal.T4<A, B, C, D>> apply(final C c) {
                                return res4.map(new Function<D, Functionnal.T4<A, B, C, D>>() {
                                    public Functionnal.T4<A, B, C, D> apply(final D d) {
                                        return new Functionnal.T4<A, B, C, D>(a, b, c, d);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }), res1, res2, res3, res4);
    }

    public static <A, B, C, D, E> JsResult<Functionnal.T5<A, B, C, D, E>> combine(final JsResult<A> res1, final JsResult<B> res2, final JsResult<C> res3, final JsResult<D> res4, final JsResult<E> res5) {
        return populateErrs(res1.flatMap(new Function<A, JsResult<Functionnal.T5<A, B, C, D, E>>>() {
            public JsResult<Functionnal.T5<A, B, C, D, E>> apply(final A a) {
                return res2.flatMap(new Function<B, JsResult<Functionnal.T5<A, B, C, D, E>>>() {
                    public JsResult<Functionnal.T5<A, B, C, D, E>> apply(final B b) {
                        return res3.flatMap(new Function<C, JsResult<Functionnal.T5<A, B, C, D, E>>>() {
                            public JsResult<Functionnal.T5<A, B, C, D, E>> apply(final C c) {
                                return res4.flatMap(new Function<D, JsResult<Functionnal.T5<A, B, C, D, E>>>() {
                                    public JsResult<Functionnal.T5<A, B, C, D, E>> apply(final D d) {
                                        return res5.map(new Function<E, Functionnal.T5<A, B, C, D, E>>() {
                                            public Functionnal.T5<A, B, C, D, E> apply(final E e) {
                                                return new Functionnal.T5<A, B, C, D, E>(a, b, c, d, e);
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

    public static <A, B, C, D, E, F> JsResult<Functionnal.T6<A, B, C, D, E, F>> combine(final JsResult<A> res1, final JsResult<B> res2, final JsResult<C> res3, final JsResult<D> res4, final JsResult<E> res5, final JsResult<F> res6) {
        return populateErrs(res1.flatMap(new Function<A, JsResult<Functionnal.T6<A, B, C, D, E, F>>>() {
            public JsResult<Functionnal.T6<A, B, C, D, E, F>> apply(final A a) {
                return res2.flatMap(new Function<B, JsResult<Functionnal.T6<A, B, C, D, E, F>>>() {
                    public JsResult<Functionnal.T6<A, B, C, D, E, F>> apply(final B b) {
                        return res3.flatMap(new Function<C, JsResult<Functionnal.T6<A, B, C, D, E, F>>>() {
                            public JsResult<Functionnal.T6<A, B, C, D, E, F>> apply(final C c) {
                                return res4.flatMap(new Function<D, JsResult<Functionnal.T6<A, B, C, D, E, F>>>() {
                                    public JsResult<Functionnal.T6<A, B, C, D, E, F>> apply(final D d) {
                                        return res5.flatMap(new Function<E, JsResult<Functionnal.T6<A, B, C, D, E, F>>>() {
                                            public JsResult<Functionnal.T6<A, B, C, D, E, F>> apply(final E e) {
                                                return res6.map(new Function<F, Functionnal.T6<A, B, C, D, E, F>>() {
                                                    public Functionnal.T6<A, B, C, D, E, F> apply(final F f) {
                                                        return new Functionnal.T6<A, B, C, D, E, F>(a, b, c, d, e, f);
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

    public static <A, B, C, D, E, F, G> JsResult<Functionnal.T7<A, B, C, D, E, F, G>> combine(final JsResult<A> res1, final JsResult<B> res2, final JsResult<C> res3, final JsResult<D> res4, final JsResult<E> res5, final JsResult<F> res6, final JsResult<G> res7) {
        return populateErrs(res1.flatMap(new Function<A, JsResult<Functionnal.T7<A, B, C, D, E, F, G>>>() {
            public JsResult<Functionnal.T7<A, B, C, D, E, F, G>> apply(final A a) {
                return res2.flatMap(new Function<B, JsResult<Functionnal.T7<A, B, C, D, E, F, G>>>() {
                    public JsResult<Functionnal.T7<A, B, C, D, E, F, G>> apply(final B b) {
                        return res3.flatMap(new Function<C, JsResult<Functionnal.T7<A, B, C, D, E, F, G>>>() {
                            public JsResult<Functionnal.T7<A, B, C, D, E, F, G>> apply(final C c) {
                                return res4.flatMap(new Function<D, JsResult<Functionnal.T7<A, B, C, D, E, F, G>>>() {
                                    public JsResult<Functionnal.T7<A, B, C, D, E, F, G>> apply(final D d) {
                                        return res5.flatMap(new Function<E, JsResult<Functionnal.T7<A, B, C, D, E, F, G>>>() {
                                            public JsResult<Functionnal.T7<A, B, C, D, E, F, G>> apply(final E e) {
                                                return res6.flatMap(new Function<F, JsResult<Functionnal.T7<A, B, C, D, E, F, G>>>() {
                                                    public JsResult<Functionnal.T7<A, B, C, D, E, F, G>> apply(final F f) {
                                                        return res7.map(new Function<G, Functionnal.T7<A, B, C, D, E, F, G>>() {
                                                            public Functionnal.T7<A, B, C, D, E, F, G> apply(final G g) {
                                                                return new Functionnal.T7<A, B, C, D, E, F, G>(a, b, c, d, e, f, g);
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

    public static <A, B, C, D, E, F, G, H> JsResult<Functionnal.T8<A, B, C, D, E, F, G, H>> combine(final JsResult<A> res1, final JsResult<B> res2, final JsResult<C> res3, final JsResult<D> res4, final JsResult<E> res5, final JsResult<F> res6, final JsResult<G> res7, final JsResult<H> res8) {
        return populateErrs(res1.flatMap(new Function<A, JsResult<Functionnal.T8<A, B, C, D, E, F, G, H>>>() {
            public JsResult<Functionnal.T8<A, B, C, D, E, F, G, H>> apply(final A a) {
                return res2.flatMap(new Function<B, JsResult<Functionnal.T8<A, B, C, D, E, F, G, H>>>() {
                    public JsResult<Functionnal.T8<A, B, C, D, E, F, G, H>> apply(final B b) {
                        return res3.flatMap(new Function<C, JsResult<Functionnal.T8<A, B, C, D, E, F, G, H>>>() {
                            public JsResult<Functionnal.T8<A, B, C, D, E, F, G, H>> apply(final C c) {
                                return res4.flatMap(new Function<D, JsResult<Functionnal.T8<A, B, C, D, E, F, G, H>>>() {
                                    public JsResult<Functionnal.T8<A, B, C, D, E, F, G, H>> apply(final D d) {
                                        return res5.flatMap(new Function<E, JsResult<Functionnal.T8<A, B, C, D, E, F, G, H>>>() {
                                            public JsResult<Functionnal.T8<A, B, C, D, E, F, G, H>> apply(final E e) {
                                                return res6.flatMap(new Function<F, JsResult<Functionnal.T8<A, B, C, D, E, F, G, H>>>() {
                                                    public JsResult<Functionnal.T8<A, B, C, D, E, F, G, H>> apply(final F f) {
                                                        return res7.flatMap(new Function<G, JsResult<Functionnal.T8<A, B, C, D, E, F, G, H>>>() {
                                                            public JsResult<Functionnal.T8<A, B, C, D, E, F, G, H>> apply(final G g) {
                                                                return res8.map(new Function<H, Functionnal.T8<A, B, C, D, E, F, G, H>>() {
                                                                    public Functionnal.T8<A, B, C, D, E, F, G, H> apply(final H h) {
                                                                        return new Functionnal.T8<A, B, C, D, E, F, G, H>(a, b, c, d, e, f, g, h);
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

    public static <A, B, C, D, E, F, G, H, I> JsResult<Functionnal.T9<A, B, C, D, E, F, G, H, I>> combine(final JsResult<A> res1, final JsResult<B> res2, final JsResult<C> res3, final JsResult<D> res4, final JsResult<E> res5, final JsResult<F> res6, final JsResult<G> res7, final JsResult<H> res8, final JsResult<I> res9) {
        return populateErrs(res1.flatMap(new Function<A, JsResult<Functionnal.T9<A, B, C, D, E, F, G, H, I>>>() {
            public JsResult<Functionnal.T9<A, B, C, D, E, F, G, H, I>> apply(final A a) {
                return res2.flatMap(new Function<B, JsResult<Functionnal.T9<A, B, C, D, E, F, G, H, I>>>() {
                    public JsResult<Functionnal.T9<A, B, C, D, E, F, G, H, I>> apply(final B b) {
                        return res3.flatMap(new Function<C, JsResult<Functionnal.T9<A, B, C, D, E, F, G, H, I>>>() {
                            public JsResult<Functionnal.T9<A, B, C, D, E, F, G, H, I>> apply(final C c) {
                                return res4.flatMap(new Function<D, JsResult<Functionnal.T9<A, B, C, D, E, F, G, H, I>>>() {
                                    public JsResult<Functionnal.T9<A, B, C, D, E, F, G, H, I>> apply(final D d) {
                                        return res5.flatMap(new Function<E, JsResult<Functionnal.T9<A, B, C, D, E, F, G, H, I>>>() {
                                            public JsResult<Functionnal.T9<A, B, C, D, E, F, G, H, I>> apply(final E e) {
                                                return res6.flatMap(new Function<F, JsResult<Functionnal.T9<A, B, C, D, E, F, G, H, I>>>() {
                                                    public JsResult<Functionnal.T9<A, B, C, D, E, F, G, H, I>> apply(final F f) {
                                                        return res7.flatMap(new Function<G, JsResult<Functionnal.T9<A, B, C, D, E, F, G, H, I>>>() {
                                                            public JsResult<Functionnal.T9<A, B, C, D, E, F, G, H, I>> apply(final G g) {
                                                                return res8.flatMap(new Function<H, JsResult<Functionnal.T9<A, B, C, D, E, F, G, H, I>>>() {
                                                                    public JsResult<Functionnal.T9<A, B, C, D, E, F, G, H, I>> apply(final H h) {
                                                                        return res9.map(new Function<I, Functionnal.T9<A, B, C, D, E, F, G, H, I>>() {
                                                                            public Functionnal.T9<A, B, C, D, E, F, G, H, I> apply(final I i) {
                                                                                return new Functionnal.T9<A, B, C, D, E, F, G, H, I>(a, b, c, d, e, f, g, h, i);
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

    public static <A, B, C, D, E, F, G, H, I, J> JsResult<Functionnal.T10<A, B, C, D, E, F, G, H, I, J>> combine(final JsResult<A> res1, final JsResult<B> res2, final JsResult<C> res3, final JsResult<D> res4, final JsResult<E> res5, final JsResult<F> res6, final JsResult<G> res7, final JsResult<H> res8, final JsResult<I> res9, final JsResult<J> res10) {
        return populateErrs(res1.flatMap(new Function<A, JsResult<Functionnal.T10<A, B, C, D, E, F, G, H, I, J>>>() {
            public JsResult<Functionnal.T10<A, B, C, D, E, F, G, H, I, J>> apply(final A a) {
                return res2.flatMap(new Function<B, JsResult<Functionnal.T10<A, B, C, D, E, F, G, H, I, J>>>() {
                    public JsResult<Functionnal.T10<A, B, C, D, E, F, G, H, I, J>> apply(final B b) {
                        return res3.flatMap(new Function<C, JsResult<Functionnal.T10<A, B, C, D, E, F, G, H, I, J>>>() {
                            public JsResult<Functionnal.T10<A, B, C, D, E, F, G, H, I, J>> apply(final C c) {
                                return res4.flatMap(new Function<D, JsResult<Functionnal.T10<A, B, C, D, E, F, G, H, I, J>>>() {
                                    public JsResult<Functionnal.T10<A, B, C, D, E, F, G, H, I, J>> apply(final D d) {
                                        return res5.flatMap(new Function<E, JsResult<Functionnal.T10<A, B, C, D, E, F, G, H, I, J>>>() {
                                            public JsResult<Functionnal.T10<A, B, C, D, E, F, G, H, I, J>> apply(final E e) {
                                                return res6.flatMap(new Function<F, JsResult<Functionnal.T10<A, B, C, D, E, F, G, H, I, J>>>() {
                                                    public JsResult<Functionnal.T10<A, B, C, D, E, F, G, H, I, J>> apply(final F f) {
                                                        return res7.flatMap(new Function<G, JsResult<Functionnal.T10<A, B, C, D, E, F, G, H, I, J>>>() {
                                                            public JsResult<Functionnal.T10<A, B, C, D, E, F, G, H, I, J>> apply(final G g) {
                                                                return res8.flatMap(new Function<H, JsResult<Functionnal.T10<A, B, C, D, E, F, G, H, I, J>>>() {
                                                                    public JsResult<Functionnal.T10<A, B, C, D, E, F, G, H, I, J>> apply(final H h) {
                                                                        return res9.flatMap(new Function<I, JsResult<Functionnal.T10<A, B, C, D, E, F, G, H, I, J>>>() {
                                                                            public JsResult<Functionnal.T10<A, B, C, D, E, F, G, H, I, J>> apply(final I i) {
                                                                                return res10.map(new Function<J, Functionnal.T10<A, B, C, D, E, F, G, H, I, J>>() {
                                                                                    public Functionnal.T10<A, B, C, D, E, F, G, H, I, J> apply(final J j) {
                                                                                        return new Functionnal.T10<A, B, C, D, E, F, G, H, I, J>(a, b, c, d, e, f, g, h, i, j);
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