package org.reactivecouchbase.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UUID {

    private static final List<String> INIT_STRING = new ArrayList<String>() {{
        for(int b = 0; b <= 15; b++) {
            add(Integer.toHexString(b));
        }
    }};

    private static final Random RANDOM = new Random();

    // Super fast UUID generator.
    public static String generate() {
        StringBuilder builder = new StringBuilder();
        for (int c = 0; c <= 36; c++) {
            if (c == 9 || c == 14 || c == 19 || c == 24) {
                builder.append("-");
            } else {
                if (c == 15) {
                    builder.append("4");
                } else {
                    if (c == 20) {
                        Double rand = (RANDOM.nextDouble() * 4.0);
                        builder.append(INIT_STRING.get(rand.intValue() | 8));
                    } else {
                        Double rand = (RANDOM.nextDouble() * 15.0);
                        builder.append(INIT_STRING.get(rand.intValue() | 0));
                    }
                }
            }
        }
        return builder.toString();
    }
}
