package io.ga.infrastructure.local;

import static org.slf4j.LoggerFactory.getLogger;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.RecipeRetriever;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.RecipeContentParser;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.slf4j.Logger;

/**
 * @author gazarenkov
 */
public class LocalInfrastructure extends RuntimeInfrastructure {

  private static final Logger LOG = getLogger(LocalInfrastructure.class);

  private URI masterWebsocket;
  private URI masterRest;


  // TODO remove RecipeRetriever

  @Inject
  public LocalInfrastructure(EventService eventService, InstallerRegistry installerRegistry,
                             @Named("che.api") URI apiEndpoint, @Named("che.websocket") URI websocketEndpoint,
                             Map<String, RecipeContentParser> allContentParsers) {
    super("local", Collections.singletonList("local"), eventService, installerRegistry, allContentParsers, new RecipeRetriever(apiEndpoint));

    this.masterWebsocket = websocketEndpoint;
    this.masterRest = apiEndpoint;
  }


  @Override
  protected void internalEstimate(InternalEnvironment env) throws ValidationException, InfrastructureException {


//    Properties props = parseRecipe(env.getRecipe().getContent());

    LOG.debug("Estimating Local Environment: " + env.getRecipe().getContent() );


    //throw new ValidationException("TEST!!!!");


  }

  @Override
  public RuntimeContext prepare(RuntimeIdentity id, InternalEnvironment environment) throws ValidationException, InfrastructureException {
    LOG.debug(">>>>>>>> ");
    LOG.debug("Prepare Local RuntimeContext " + environment.getRecipe().getModel());
    LOG.debug(">>>>>>>> ");

    int machinesNum = environment.getMachines().size();
    if(machinesNum != 1)
      throw new InfrastructureException("Exactly one machine expected, found " + machinesNum);

    return new LocalRuntimeContext(environment, id, this, masterRest, masterWebsocket);
  }

//  private Properties parseRecipe(String str) throws ValidationException {
//    Properties prop = new Properties();
//    StringReader reader = new StringReader(str);
//    try {
//      prop.load(reader);
//    } catch (IOException e) {
//      throw new ValidationException("Error " + e);
//    }
//
//    return prop;
//
//  }

}
