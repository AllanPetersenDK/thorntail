package org.jboss.unimbus.opentracing.tck;

import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerResolver;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.ThreadLocalActiveSpanSource;

/**
 * Created by bob on 2/19/18.
 */
public class MockTracerResolver extends TracerResolver {

    @Override
    protected Tracer resolve() {
        return new MockTracer(new ThreadLocalActiveSpanSource(), MockTracer.Propagator.TEXT_MAP);
    }
}
