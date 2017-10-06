package io.ga.infrastructure.local;

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.slf4j.Logger;

import java.net.URI;
import java.util.Properties;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author gazarenkov
 */
public class LocalRuntimeContext extends RuntimeContext <Properties> {

  private static final Logger LOG = getLogger(LocalRuntimeContext.class);

  private URI masterWebsocket;
  private URI masterRest;

  private LocalInternalRuntime cachedRuntime;


  public LocalRuntimeContext(InternalEnvironment<Properties> environment,
                             RuntimeIdentity identity,
                             RuntimeInfrastructure infrastructure,
                             URI masterRest,
                             URI masterWebsocket)
      throws ValidationException, InfrastructureException {
    super(environment, identity, infrastructure);
    this.masterWebsocket = masterWebsocket;
    this.masterRest = masterRest;

  }

  @Override
  public InternalRuntime getRuntime() {
    if(cachedRuntime == null) {

      cachedRuntime = new LocalInternalRuntime(this);

    }

    return cachedRuntime;

  }

  @Override
  public URI getOutputChannel() throws InfrastructureException, UnsupportedOperationException {
    return masterWebsocket;
  }

  public URI getMasterRestEndpoint() {
    return masterRest;
  }

  public URI getMasterWebsocketEndpoint() {
    return masterWebsocket;
  }

}
