package io.ga.infrastructure.local;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.hc.ServerCheckerFactoryImpl;
import org.eclipse.che.api.workspace.server.hc.ServersReadinessChecker;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.shared.dto.event.MachineLogEvent;
import org.eclipse.che.api.workspace.shared.dto.event.ServerStatusEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;

/**
 * @author gazarenkov
 */
public class LocalInternalRuntime extends InternalRuntime<LocalRuntimeContext> {

  private static final Logger LOG = getLogger(LocalInternalRuntime.class);


  protected List<Process> processes;
  //protected List <ProcessBuilder>  builders;
  private Map<String, LocalMachine> machines = new HashMap<>();
  private LocalMachine machine;
  private String machineName;

  LocalInternalRuntime(LocalRuntimeContext context)  {
    super(context, new NullRewriter(), false);
    //this.builders = new ArrayList<>();
    this.processes = new ArrayList<>();

    String localhostName = "localhost";

    // it is valid assumption since it was checked that there is exactly one machine there
    machineName = context.getEnvironment().getMachines().keySet().iterator().next();
    machine = new LocalMachine(localhostName, context.getEnvironment().getMachines().get(machineName).getServers());
    machines.put(machineName, machine);

//    for(String agent : getContext().getEnvironment().getRecipe().getModel().stringPropertyNames()) {
//
//      String command = getContext().getEnvironment().getRecipe().getModel().getProperty(agent);
//
//      LOG.debug("Command: " + command);
//
//      ProcessBuilder builder = new ProcessBuilder(command);
//      builder.redirectErrorStream(true);
//      Map<String, String> env = builder.environment();
//      env.put("CHE_API", getContext().getMasterRestEndpoint().toString());
//      env.put("CHE_WORKSPACE_ID", getContext().getIdentity().getWorkspaceId());
//
//      builders.add(builder);
//
//      //  cachedRuntime.addBuilders(builder);
//    }

  }

  @Override
  public Map<String, ? extends Machine> getInternalMachines() {
    return machines;
  }

  @Override
  public Map<String, String> getProperties() {
    return new HashMap<>();
  }

  @Override
  protected void internalStart(Map<String, String> startOptions) throws InfrastructureException {

    Properties recipeProperties = getContext().getEnvironment().getRecipe().getModel();
    for(String agent : recipeProperties.stringPropertyNames()) {
      String commandStr = recipeProperties.getProperty(agent);

      List <String> command = new ArrayList<String>(Arrays.asList(commandStr.split("\\s+")));

      //new ArrayList<String>(command.split(" "));

      LOG.debug(">>>>>>>>>>>>>>>>> " + agent);
      LOG.debug("Command: " + command);

      // prepare process builder
      ProcessBuilder builder = new ProcessBuilder(command);
      builder.redirectErrorStream(true);
      Map<String, String> env = builder.environment();
      env.put("CHE_API", getContext().getMasterRestEndpoint().toString());
      env.put("CHE_WORKSPACE_ID", getContext().getIdentity().getWorkspaceId());

      // start the process per server
      Process process;
      try {
        process = builder.start();
        LOG.debug("Process: " + process + " pid: " + getPid(process));
      } catch (IOException e) {
        throw new InfrastructureException(e);
      }

      // start and output logs
      processes.add(process);
      new OutLogger(agent, process).start();

      // workaround creating Map per server for readiness checker
      Map<String, LocalMachine.LocalServer> servers = new HashMap<>();
      servers.put(agent, machine.getServers().get(agent));

//      // start servers check
      ServersReadinessChecker readinessChecker =
          new ServersReadinessChecker(getContext().getIdentity(), machineName, servers, new ServerCheckerFactoryImpl());
      readinessChecker.startAsync(new ServerReadinessHandler(machineName, machine));
      try {
        readinessChecker.await();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      LOG.debug("Checker for: " + agent);

    }

  }

//  void addBuilders(ProcessBuilder builder) {
//    this.builders.add(builder);
//  }

  @Override
  protected void internalStop(Map<String, String> stopOptions) throws InfrastructureException {
    for(Process process : processes) {
      process.destroy();
    }

  }


  private class OutLogger extends Thread {

    private Process process;
    private String agent;

    public OutLogger(String agent, Process process) {
      this.process = process;
      this.agent = agent;
    }

    public void run() {

      LOG.debug("Ran Out for: " + agent);
      InputStream is = process.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      EventService eventService = getContext().getInfrastructure().getEventService();

      try {

        String line;
        while ((line = reader.readLine()) != null) {

          eventService.publish(
              DtoFactory.newDto(MachineLogEvent.class)
                        .withRuntimeId(DtoConverter.asDto(getContext().getIdentity()))
                        //.withStream(stream)
                        .withText(line)
                        .withTime(ZonedDateTime.now().format(ISO_OFFSET_DATE_TIME))
                        .withMachineName(machineName));
        }

      } catch (IOException e) {
        System.out.println("PROBLEM: " + e);
      }

    }

  }

  private int getPid(Process child) {
    try {
      Class<?> cProcess = child.getClass();
      Field pid = cProcess.getDeclaredField("pid");
      if (!pid.isAccessible()) {
        pid.setAccessible(true);
      }
      return pid.getInt(child);
    } catch (Exception e) {
      return -1;
    }
  }

  private static class NullRewriter implements URLRewriter {
    @Override
    public String rewriteURL(@Nullable RuntimeIdentity identity, @Nullable String name, String url) throws InfrastructureException {
      return url;
    }
  }


  private class ServerReadinessHandler implements Consumer<String> {
    private String machineName;
    private LocalMachine machine;

    public ServerReadinessHandler(String machineName, LocalMachine machine) {
      this.machineName = machineName;
      this.machine = machine;
      LOG.debug("Readiness check: " + machineName);
    }

    @Override
    public void accept(String serverRef) {
      EventService eventService = getContext().getInfrastructure().getEventService();

      machine.getServers().get(serverRef).setStatus(ServerStatus.RUNNING);

      LOG.debug("Readiness check: " + machine.getServers().get(serverRef) + " " +serverRef);

      eventService.publish(
          DtoFactory.newDto(ServerStatusEvent.class)
                    .withIdentity(DtoConverter.asDto(getContext().getIdentity()))
                    .withMachineName(machineName)
                    .withServerName(serverRef)
                    .withStatus(ServerStatus.RUNNING)
                    .withServerUrl(machine.getServers().get(serverRef).getUrl()));
    }
  }
}
