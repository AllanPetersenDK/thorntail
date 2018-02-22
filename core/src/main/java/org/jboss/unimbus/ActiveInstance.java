package org.jboss.unimbus;

import java.util.List;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Decorator;

import org.jboss.weld.injection.producer.BeanInjectionTarget;
import org.jboss.weld.injection.producer.Instantiator;
import org.jboss.weld.injection.producer.SubclassDecoratorApplyingInstantiator;

/**
 * An activated but unmanaged instance.
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 * @see UNimbus#activate(Object)
 */
public class ActiveInstance<T> {

    ActiveInstance(String containerId, T object) {
        this(containerId, (Class<T>) object.getClass(), object);
    }

    ActiveInstance(String containerId, Class<T> type, T object) {
        BeanManager manager = CDI.current().getBeanManager();

        AnnotatedType annotatedType = manager.createAnnotatedType(type);
        BeanInjectionTarget injectionTarget = (BeanInjectionTarget) manager.getInjectionTargetFactory(annotatedType).createInjectionTarget(null);
        this.bean = new InstanceBean<T>(injectionTarget, type, object);

        if (object != null) {
            injectionTarget.setInstantiator(new InstanceInstantiator(object));
        }

        List<Decorator<?>> decorators = manager.resolveDecorators(bean.getTypes(), Any.Literal.INSTANCE);

        if (!decorators.isEmpty()) {
            Instantiator instantiator = injectionTarget.getInstantiator();
            injectionTarget.setInstantiator(new SubclassDecoratorApplyingInstantiator(containerId, instantiator, bean, decorators));
        }

        this.context = manager.createCreationalContext(null);
        this.instance = bean.create(context);

    }

    /**
     * Retrieve the activated object.
     *
     * @return The activated object.
     */
    public T get() {
        return this.instance;
    }

    /**
     * Release, destroy and dispose of the activated object.
     */
    public void release() {
        this.bean.destroy(this.instance, this.context);
    }

    private final T instance;

    private final CreationalContext<T> context;

    private final InstanceBean<T> bean;
}
