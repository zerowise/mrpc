package com.github.zerowise;

import com.github.zerowise.tools.ProtostuffUtil;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 ** @createtime : 2018/10/12上午11:04
 **/
public class ProtostuffUtilTest {

    @Test
    public void testIsSame() {
        try {
            Method m1 = SmileJava.class.getMethod("add", int.class, int.class);
            Smile s1 = new Smile(m1, new Object[]{10000,200000});

            byte[] bytes = ProtostuffUtil.toByteArray(s1);

            Smile s2 = ProtostuffUtil.newInstance(Smile.class,bytes);

            Assert.assertTrue(s1.equals(s2));

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

    }
}
