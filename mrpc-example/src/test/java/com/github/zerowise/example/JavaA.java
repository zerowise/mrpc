package com.github.zerowise.example;

/**
 ** @createtime : 2018/10/12下午4:42
 **/
public class JavaA extends Parent implements InterB {

    public static void main(String[] args) {

        Class c1 = JavaA.class;
        while (!c1.equals(Object.class)) {
            for (Class cls : c1.getInterfaces()) {
                System.out.println(cls.getName());
            }
            c1 = c1.getSuperclass();
        }

    }
}
