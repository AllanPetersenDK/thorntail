package org.jboss.unimbus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.unimbus.util.TypeUtils;

/**
 * Created by bob on 2/21/18.
 */
class InstanceBean<T> implements Bean<T> {
    InstanceBean(InjectionTarget<T> injectionTarget, Class<T> instanceType, T base) {
        this.injectionTarget = injectionTarget;
        this.instanceType = instanceType;
        this.base = base;
    }

    @Override
    public Class<?> getBeanClass() {
        return this.instanceType;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return this.injectionTarget.getInjectionPoints();
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public T create(CreationalContext<T> creationalContext) {
        T instance = this.injectionTarget.produce(creationalContext);
        this.injectionTarget.inject(instance, creationalContext);
        this.injectionTarget.postConstruct(instance);
        return instance;
    }


    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext) {
        this.injectionTarget.preDestroy(instance);
        this.injectionTarget.dispose(instance);
    }

    @Override
    public Set<Type> getTypes() {
        return TypeUtils.getTypeClosure(this.instanceType);
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return Collections.emptySet();
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    private final Class<T> instanceType;

    private final InjectionTarget<T> injectionTarget;

    private final T base;
}
