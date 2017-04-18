package io.ga.master.doubtful;

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;

import java.net.URL;
import java.util.Map;

/**
 * @author gazarenkov
 */
public class DummyRuntimeContext extends RuntimeContext {


    public DummyRuntimeContext(Environment environment,
                               RuntimeIdentity identity,
                               RuntimeInfrastructure infrastructure, URL registryEndpoint)
            throws ValidationException, InfrastructureException {
        super(environment, identity, infrastructure, registryEndpoint);
    }

    @Override
    protected InternalRuntime internalStart(Map<String, String> startOptions) throws InfrastructureException {
        return new DummyInternalRuntime(this, null);
    }

    @Override
    protected void internalStop(Map<String, String> stopOptions) throws InfrastructureException {

    }

    @Override
    public URL getOutputChannel() throws InfrastructureException, UnsupportedOperationException {
        return null;
    }
}
