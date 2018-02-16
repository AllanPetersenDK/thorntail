package org.jboss.unimbus.jca;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.unimbus.jca.impl.JCAMessages;

/**
 * Receiver for {@link ResourceAdapterDeployment} instances.
 *
 * <p>Deployments registered with this component prior to the {@link org.jboss.unimbus.events.LifecycleEvent.Initialize} event
 * will be deployed as active resource adapters.</p>
 *
 * <p>This component may be {@code @Inject}ed into application components.</p>
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
@ApplicationScoped
public class ResourceAdapterDeployments {

    /**
     * Add a resource-adapter deployment.
     *
     * <p>Deployments must be added prior to the firing of {@link org.jboss.unimbus.events.LifecycleEvent.Initialize}.
     * Usually {@link org.jboss.unimbus.events.LifecycleEvent.Scan} is an appropriate phase to add additional deployments.</p>
     *
     * @param deployment The deployment to add.
     */
    public void addDeployment(ResourceAdapterDeployment deployment) {
        JCAMessages.MESSAGES.registeredDeployment(deployment.getUniqueId());
        this.deployments.add(deployment);
    }

    /**
     * Retrieve all registered deployments.
     *
     * @return The list of all registered deployments.
     */
    public List<ResourceAdapterDeployment> getDeployments() {
        return this.deployments;
    }

    private List<ResourceAdapterDeployment> deployments = new ArrayList<>();
}
