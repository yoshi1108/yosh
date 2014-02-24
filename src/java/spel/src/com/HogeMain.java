package com;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

public class HogeMain {
    public static void main(String[] args) throws InterruptedException {
        // 普通にJavaで乱数(0～2)を表示する場合
        int random = (int)(Math.random() * 3);
        System.out.println("java ramdom=" + random);

        // Spelで乱数(0～2)を生成する場合
        ExpressionParser parser = new SpelExpressionParser();
//        Expression exp = parser.parseExpression("T(java.lang.Math).round(T(java.lang.Math).floor((T(java.lang.Math).random() * 3)))");
        Expression exp = parser.parseExpression("T(java.util.Calendar).getInstance().toString()");
        String value = exp.getValue(String.class);
        System.out.println("spel ramdom=" + value);

        System.out.println(java.util.Calendar.getInstance().toString());
    }
}
