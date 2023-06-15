package com.example.benchmark;

import com.example.dto.base.ZhSearchReq;
import org.openjdk.jmh.annotations.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hardware: CPU-Intel i5 12400 Memory:16G DDR4 2666
 * Benchmark                                              Mode  Cnt         Score        Error  Units
 * ReflectGetFieldsUsingCacheOrNot.fieldGetWithCache     thrpt   25  48755859.654 ± 424249.143  ops/s
 * ReflectGetFieldsUsingCacheOrNot.fieldGetWithoutCache  thrpt   25  18318158.634 ± 184489.127  ops/s
 *
 */
@State(Scope.Benchmark)
public class ReflectGetFieldsUsingCacheOrNot {

    ZhSearchReq req = new ZhSearchReq()
            .setStartTime("2011-09-09 12:23:43")
            .setEndTime("2012-09-09 12:23:43")
            .setCurrent(2)
            .setPageSize(10)
            .setSorter("{\"录入时间\"：\"descend\"}")
            .setExtras(Map.of("跟进状态", List.of("待跟进", "跟进中")));

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void fieldGetWithoutCache() throws IllegalAccessException {
        Field[] fields = req.getClass().getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            f.get(req);
        }
    }


    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void fieldGetWithCache() throws IllegalAccessException {
        Field[] fields = fieldsCache.get(req.getClass());
        for (Field f : fields) {
            f.setAccessible(true);
            f.get(req);
        }
    }

    Map<Class, Field[]> fieldsCache = new HashMap<>();

    @Setup
    public void buildFieldsCache() {
        Class<? extends ZhSearchReq> reqClass = req.getClass();
        fieldsCache.put(reqClass, reqClass.getDeclaredFields());
    }
}
