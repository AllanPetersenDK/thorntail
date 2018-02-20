package org.jboss.unimbus.opentracing.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.Path;

import io.opentracing.Tracer;
import io.opentracing.ActiveSpan;
import org.eclipse.microprofile.opentracing.Traced;

/**
 * @author Pavol Loffay
 */
@Traced
@Interceptor
@Priority(value = Interceptor.Priority.LIBRARY_BEFORE + 1)
public class TracingInterceptor {

    private static final String JAXRS_PATH_ANNOTATION_NAME = "javax.ws.rs.Path";

    @Inject
    private Tracer tracer;

    @AroundInvoke
    public Object interceptTraced(InvocationContext ctx) throws Exception {
        ActiveSpan activeSpan = null;
        try {
            if (!isJaxRs(ctx.getMethod()) && isTraced(ctx.getMethod())) {
                activeSpan = this.tracer.buildSpan(getOperationName(ctx.getMethod()))
                        .startActive();
            }
            return ctx.proceed();
        } finally {
            if (activeSpan != null) {
                activeSpan.deactivate();
            }
        }
    }

    /**
     * Determines whether invoked method is jax-rs endpoint
     * @param method invoked method
     * @return true if invoked method is jax-rs endpoint
     */
    protected boolean isJaxRs(Method method) {
        if (method.getAnnotation(Path.class) != null ||
                method.getDeclaringClass().getAnnotation(Path.class) != null) {
            return true;
        }
        return false;
    }

    /**
     * Determines whether invoked method should be traced or not
     * @param method invoked method
     * @return true if {@link Traced} defined on method or class has value true
     */
    protected boolean isTraced(Method method) {
        Traced classTraced = method.getDeclaringClass().getAnnotation(Traced.class);
        Traced methodTraced = method.getAnnotation(Traced.class);
        if (methodTraced != null) {
            return methodTraced.value();
        }
        return classTraced.value();
    }

    /**
     * Returns operation name for given method
     *
     * @param method invoked method
     * @return operation name
     */
    protected String getOperationName(Method method) {
        Traced classTraced = method.getDeclaringClass().getAnnotation(Traced.class);
        Traced methodTraced = method.getAnnotation(Traced.class);
        if (methodTraced != null && methodTraced.operationName().length() > 0) {
            return methodTraced.operationName();
        } else if (classTraced != null && classTraced.operationName().length() > 0) {
            return classTraced.operationName();
        }
        return String.format("%s.%s", method.getDeclaringClass().getName(), method.getName());
    }
}
