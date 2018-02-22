package org.jboss.unimbus.opentracing.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Priority;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerResolver;
import org.jboss.unimbus.ActiveInstance;
import org.jboss.unimbus.UNimbus;
import org.jboss.unimbus.opentracing.TracerProvider;

import static java.lang.Math.abs;

/**
 * Created by bob on 2/21/18.
 */
public class Resolver extends TracerResolver {

    @Override
    protected Tracer resolve() {
        /*
        ActiveInstance<Resolver> instance = UNimbus.current().activate(this);
        try {
            return instance.get().doResolve();
        } finally {
            instance.release();
        }
        */
        return UNimbus.current().withActivated(this, (resolver) -> resolver.doResolve());
    }

    private Tracer doResolve() {
        for (TracerProvider provider : providers()) {
            Tracer tracer = provider.get();
            if (tracer != null) {
                return tracer;
            }
        }
        return null;
    }

    protected Iterable<TracerProvider> providers() {
        List<TracerProvider> providers = new ArrayList<>();

        for (TracerProvider provider : this.providers) {
            providers.add(provider);
        }

        providers.sort((l, r) -> {
            int leftPrio = priorityOf(l);
            int rightPrio = priorityOf(r);

            if (leftPrio == rightPrio) {
                return 0;
            }

            if (leftPrio > 0) {
                if (rightPrio > 0) {
                    return Integer.compare(abs(leftPrio), abs(rightPrio));
                } else {
                    return 1;
                }
            }

            if (leftPrio < 0) {
                if (rightPrio < 0) {
                    return Integer.compare(abs(leftPrio), abs(rightPrio));
                } else {
                    return 1;
                }
            }

            // we won't actually get here, in theory.
            return Integer.compare(rightPrio, leftPrio);
        });

        return providers;
    }

    protected int priorityOf(TracerProvider provider) {
        Priority priorityAnno = annotationOf(provider);
        if (priorityAnno == null) {
            return Integer.MAX_VALUE;
        }

        return priorityAnno.value();
    }

    protected Priority annotationOf(TracerProvider provider) {
        Class<?> cur = provider.getClass();
        while (cur != null) {
            Priority anno = cur.getAnnotation(Priority.class);
            if (anno != null) {
                return anno;
            }
            cur = cur.getSuperclass();
        }

        return null;
    }

    @Inject
    @Any
    Instance<TracerProvider> providers;
}
