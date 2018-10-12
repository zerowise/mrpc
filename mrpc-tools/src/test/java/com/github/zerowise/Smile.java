package com.github.zerowise;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.lang.reflect.Method;

/**
 ** @author : hanyuanliang(hanyuanliang@hulai.com)
 ** @createtime : 2018/10/12上午11:06
 **/
public class Smile {

    private String serviceName;
    private String methodName;
    private Class[] parameterTypes;
    private Object[] args;

    public Smile() {
    }

    public Smile(Method method, Object[] args) {
        serviceName= method.getDeclaringClass().getName();
        methodName = method.getName();
        parameterTypes = method.getParameterTypes();
        this.args = args;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Smile smile = (Smile) o;

        return new EqualsBuilder()
                .append(serviceName, smile.serviceName)
                .append(methodName, smile.methodName)
                .append(parameterTypes, smile.parameterTypes)
                .append(args, smile.args)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(serviceName)
                .append(methodName)
                .append(parameterTypes)
                .append(args)
                .toHashCode();
    }
}
