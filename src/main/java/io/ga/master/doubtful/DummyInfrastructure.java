package io.ga.master.doubtful;

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.Collections;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author gazarenkov
 */
public class DummyInfrastructure extends RuntimeInfrastructure {

    private static final Logger LOG = getLogger(DummyInfrastructure.class);

    @Inject
    public DummyInfrastructure() {
        super("dummy", Collections.singletonList("dummy"));
    }

    @Override
    public Environment estimate(Environment environment) throws ValidationException, InfrastructureException {
        return environment;
    }

    @Override
    public RuntimeContext prepare(RuntimeIdentity id, Environment environment) throws ValidationException, InfrastructureException {
        return new DummyRuntimeContext(environment, id, this, null);
    }



//    @Override
//    public InternalRuntime start(String workspaceId, Environment environment, MessageConsumer<MachineLogMessage> logger,
//                                 Map<String, String> options) throws NotFoundException, ConflictException, ServerException {
//
//        Map<String, MachineRuntime> machines = new HashMap<>();
//
//        for(Map.Entry<String, ? extends ExtendedMachine> m : environment.getMachines().entrySet()) {
//
//            Map<String, ServerRuntime> servers = new HashMap<>();
//
//            for(Map.Entry<String, ? extends ServerConf2> s : m.getValue().getServers().entrySet()) {
//                servers.put(s.getKey(), new ServerRuntimeImpl(""));
//            }
//
//            Map<String, String> properties = new HashMap<>();
//
//            machines.put(m.getKey(), new MachineRuntimeImpl(properties, servers));
//        }
//
//        return new InternalRuntime(machines);
//    }
//
//    @Override
//    public void stop(String workspaceId, Map<String, String> options) throws NotFoundException, ServerException {
//
//    }
//
//    @Override
//    public void validate(Environment environment) throws ServerException {
//
//        LOG.info("Validating " + environment);
//    }
}
