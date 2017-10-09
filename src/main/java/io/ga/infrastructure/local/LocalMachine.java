package io.ga.infrastructure.local;

import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author gazarenkov
 */
public class LocalMachine implements Machine {

  private static final Logger LOG = getLogger(LocalMachine.class);

  private Map<String, LocalServer> servers;

  public LocalMachine(String localhostName, Map <String, ServerConfig> servers) {
    this.servers = new HashMap<>();
    for(Map.Entry<String, ServerConfig> entry : servers.entrySet()) {
      LocalServer server = new LocalServer(localhostName, entry.getValue());
      this.servers.put(entry.getKey(), server);

      LOG.debug("Server added " + entry.getKey() + " = " + server.getUrl());
    }

//    this.servers.put(Constants.SERVER_WS_AGENT_HTTP_REFERENCE, new LocalServer());


  }

  @Override
  public Map<String, String> getProperties() {
    Map<String, String> props = new HashMap<>();
    props.put("projects", "/projects");
    return props;
  }

  @Override
  public Map<String, LocalServer> getServers() {
    return servers;
  }



  public class LocalServer implements Server {

    private ServerStatus status;

    private URI uri;

    public LocalServer() {
      try {
        this.uri = new URI("http://localhost:8081/api");
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }

    public LocalServer(String localhostName, ServerConfig config) {
      this.status = ServerStatus.STOPPED;

      try {
        this.uri = new URI(config.getProtocol(), null, localhostName, new Integer(config.getPort()).intValue(),
                           config.getPath(), null, null);
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }

    }

    @Override
    public String getUrl() {
      //"http://localhost:8081/api"
      return uri.toString();
    }

    @Override
    public ServerStatus getStatus() {
      return status;
    }

    public void setStatus(ServerStatus status) {
      this.status = status;

    }
  }
}
