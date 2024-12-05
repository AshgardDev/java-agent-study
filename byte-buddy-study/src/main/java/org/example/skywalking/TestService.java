package org.example.skywalking;

import java.util.List;

/**
 * 被增强类。模拟一个业务类，有report和compute两个方法。
 */
public class TestService {

    public String report(String name, int value) {
        return String.format("name: %s, value: %s", name, value);
    }

    public void compute(List<Integer> values) {
        System.out.println("compute result:" + values.stream().mapToInt(v -> v.intValue()).sum());
    }
}