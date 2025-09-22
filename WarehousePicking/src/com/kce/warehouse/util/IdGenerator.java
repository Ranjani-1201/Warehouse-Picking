package com.kce.warehouse.util;
import java.util.concurrent.atomic.AtomicInteger;
public class IdGenerator {
    private static final AtomicInteger COUNTER = new AtomicInteger(1000);
    public static String next(String prefix) {
        return prefix + "-" + COUNTER.getAndIncrement();
    }
}
