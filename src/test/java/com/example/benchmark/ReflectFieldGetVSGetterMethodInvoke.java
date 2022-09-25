package com.example.benchmark;

import com.example.dto.base.ZhSearchReq;
import org.openjdk.jmh.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Benchmark                                                Mode  Cnt         Score        Error  Units
 * ReflectFieldGetVSGetterMethodInvoke.fieldGet            thrpt   25  15181666.543 ± 112999.822  ops/s
 * ReflectFieldGetVSGetterMethodInvoke.getterMethodInvoke  thrpt   25   6511663.013 ±   4912.844  ops/s
 */
@State(Scope.Benchmark)
public class ReflectFieldGetVSGetterMethodInvoke {

    ZhSearchReq req = new ZhSearchReq()
            .setStartTime("2011-09-09 12:23:43")
            .setEndTime("2012-09-09 12:23:43")
            .setCurrent(2)
            .setPageSize(10)
            .setSorter("{\"录入时间\"：\"descend\"}")
            .setExtras(Map.of("跟进状态", List.of("待跟进", "跟进中")));

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void fieldGet() throws IllegalAccessException {
        Field[] fields = req.getClass().getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            f.get(req);
        }
    }


    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void getterMethodInvoke() {
        Field[] fields = req.getClass().getDeclaredFields();
        for (Field f : fields) {
            String fName = f.getName().toLowerCase();
            Method method = methodInfoCache.get(fName);
            if (Objects.nonNull(method)) {
                try {
                    method.invoke(req, null);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    Map<String, Method> methodInfoCache = new HashMap<>();

    @Setup
    public void buildMethodInfoCache() {
        Method[] methods = req.getClass().getDeclaredMethods();
        for (Method m : methods) {
            String name = m.getName();
            if (name.startsWith("get")) {
                String lowerCaseFieldName = name.replace("get", "").toLowerCase();
                methodInfoCache.put(lowerCaseFieldName, m);
            }
        }
    }

}
