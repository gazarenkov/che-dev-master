/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package io.ga.master;

import io.ga.master.doubtful.DummyInfrastructure;
import io.ga.master.doubtful.DummyTokenValidator;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.core.jsonrpc.BuildingRequestTransmitter;
import org.eclipse.che.api.core.jsonrpc.JsonRpcFactory;
import org.eclipse.che.api.core.jsonrpc.JsonRpcMessageReceiver;
import org.eclipse.che.api.core.jsonrpc.RequestHandlerConfigurator;
import org.eclipse.che.api.core.rest.ApiInfoService;
import org.eclipse.che.api.core.rest.CoreRestModule;
import org.eclipse.che.api.core.websocket.WebSocketMessageReceiver;
import org.eclipse.che.api.core.websocket.WebSocketMessageTransmitter;
import org.eclipse.che.api.core.websocket.impl.BasicWebSocketMessageTransmitter;
import org.eclipse.che.api.core.websocket.impl.GuiceInjectorEndpointConfigurator;
import org.eclipse.che.api.user.server.TokenValidator;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.flywaydb.core.internal.util.PlaceholderReplacer;

import javax.sql.DataSource;


public class MasterModule extends AbstractModule {
    @Override
    protected void configure() {

        bind(ApiInfoService.class);
        bind(org.eclipse.che.api.core.notification.WSocketEventBusServer.class);
        install(new CoreRestModule());

        configureWebSocket();
        configureJsonRpc();


        // DUMMY
//        MapBinder<String, TypeSpecificEnvironmentParser> envParserMapBinder = MapBinder.newMapBinder(binder(),
//                                                                                                     String.class,
//                                                                                                     TypeSpecificEnvironmentParser.class);
//        envParserMapBinder.addBinding("dummy").to(DummyEnvParser.class);

        Multibinder<RuntimeInfrastructure> infras =
                Multibinder.newSetBinder(binder(), RuntimeInfrastructure.class);
        infras.addBinding().to(DummyInfrastructure.class);


        // db related components modules
        install(new com.google.inject.persist.jpa.JpaPersistModule("main"));
        install(new org.eclipse.che.account.api.AccountModule());
        install(new org.eclipse.che.api.user.server.jpa.UserJpaModule());
        install(new org.eclipse.che.api.ssh.server.jpa.SshJpaModule());
        install(new org.eclipse.che.api.machine.server.jpa.MachineJpaModule());
        install(new org.eclipse.che.api.workspace.server.jpa.WorkspaceJpaModule());

        // db configuration
        bind(DataSource.class).toProvider(org.eclipse.che.core.db.h2.H2DataSourceProvider.class);
        bind(SchemaInitializer.class).to(org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer.class);
        bind(org.eclipse.che.core.db.DBInitializer.class).asEagerSingleton();
        bind(PlaceholderReplacer.class).toProvider(org.eclipse.che.core.db.schema.impl.flyway.PlaceholderReplacerProvider.class);

        // User
        bind(org.eclipse.che.api.user.server.CheUserCreator.class);
        bind(org.eclipse.che.api.user.server.UserService.class);
        bind(org.eclipse.che.api.user.server.ProfileService.class);
        bind(org.eclipse.che.api.user.server.PreferencesService.class);

        // Ssh
        bind(org.eclipse.che.api.ssh.server.SshService.class);

        // Workspace
        bind(org.eclipse.che.api.workspace.server.WorkspaceService.class);
        bind(org.eclipse.che.api.workspace.server.event.WorkspaceMessenger.class).asEagerSingleton();
//        bind(org.eclipse.che.api.workspace.server.WorkspaceValidator.class)
//                .to(org.eclipse.che.api.workspace.server.DefaultWorkspaceValidator.class);

//        bind(org.eclipse.che.api.workspace.server.WorkspaceFilesCleaner.class)
//                .to(org.eclipse.che.plugin.docker.machine.cleaner.LocalWorkspaceFilesCleaner.class);


        // Machine
//        bind(org.eclipse.che.api.environment.server.MachineInstanceProvider.class)
//                .to(org.eclipse.che.plugin.docker.machine.MachineProviderImpl.class);
//        bind(org.eclipse.che.api.environment.server.InfrastructureProvisioner.class)
//                .to(org.eclipse.che.plugin.docker.machine.local.LocalCheInfrastructureProvisioner.class);



        // Docker
//        install(new org.eclipse.che.plugin.docker.machine.local.LocalDockerModule());
//        Multibinder<InstanceProvider> machineImageProviderMultibinder =
//                Multibinder.newSetBinder(binder(), org.eclipse.che.api.machine.server.spi.InstanceProvider.class);
//        machineImageProviderMultibinder.addBinding().to(org.eclipse.che.plugin.docker.machine.DockerInstanceProvider.class);


        // Agent
//        install(new org.eclipse.che.api.agent.server.AgentModule());
//        //bind(AgentRegistryService.class);
//        bind(org.eclipse.che.api.agent.server.AgentRegistry.class)
//                .to(org.eclipse.che.api.agent.server.impl.LocalAgentRegistryImpl.class);
//        Multibinder<AgentLauncher> agentLaunchers = Multibinder.newSetBinder(binder(), AgentLauncher.class);
//        agentLaunchers.addBinding().to(org.eclipse.che.api.workspace.server.launcher.WsAgentLauncherImpl.class);
//        agentLaunchers.addBinding().to(org.eclipse.che.api.workspace.server.launcher.TerminalAgentLauncherImpl.class);
//        agentLaunchers.addBinding().to(org.eclipse.che.api.workspace.server.launcher.SshAgentLauncherImpl.class);
//
        bind(org.eclipse.che.api.agent.server.WsAgentHealthChecker.class)
                .to(org.eclipse.che.api.agent.server.WsAgentHealthCheckerImpl.class);



        // Auth
        bind(TokenValidator.class).to(DummyTokenValidator.class);

        // Templates
        bind(org.eclipse.che.api.project.server.template.ProjectTemplateDescriptionLoader.class).asEagerSingleton();
        bind(org.eclipse.che.api.project.server.template.ProjectTemplateRegistry.class);
        bind(org.eclipse.che.api.project.server.template.ProjectTemplateService.class);

        // Workspace
        bindConstant().annotatedWith(Names.named("che.workspace.auto_restore")).to("false");
        bindConstant().annotatedWith(Names.named("che.workspace.auto_snapshot")).to("false");
        bindConstant().annotatedWith(Names.named("che.workspace.default_memory_mb")).to(0);
        bindConstant().annotatedWith(Names.named("che.workspace.logs")).to("?");

        bindConstant().annotatedWith(Names.named("che.workspace.hosts")).to("NULL");
        bindConstant().annotatedWith(Names.named("che.workspace.projects.storage")).to("~/Projects/che-projects");
        bindConstant().annotatedWith(Names.named("che.workspace.storage")).to("${che.home}/workspaces");

        bindConstant().annotatedWith(Names.named("che.workspace.pool.cores_multiplier")).to("2");
        bindConstant().annotatedWith(Names.named("che.workspace.pool.exact_size")).to("NULL");
        bindConstant().annotatedWith(Names.named("che.workspace.pool.type")).to("fixed");

        bindConstant().annotatedWith(Names.named("che.auth.user_self_creation")).to("false");
        bindConstant().annotatedWith(Names.named("che.auth.reserved_user_names")).to("");

        bindConstant().annotatedWith(Names.named("che.database")).to("~/Projects/che-storage");
        bindConstant().annotatedWith(Names.named("che.api")).to("http://localhost:8080/api");

        // db
        bindConstant().annotatedWith(Names.named("db.schema.flyway.baseline.enabled")).to("true");
        bindConstant().annotatedWith(Names.named("db.schema.flyway.baseline.version")).to("5.0.0.8.1");
        bindConstant().annotatedWith(Names.named("db.schema.flyway.scripts.prefix")).to("");
        bindConstant().annotatedWith(Names.named("db.schema.flyway.scripts.suffix")).to(".sql");
        bindConstant().annotatedWith(Names.named("db.schema.flyway.scripts.version_separator")).to("__");
        bindConstant().annotatedWith(Names.named("db.schema.flyway.scripts.locations")).to("classpath:che-schema");
        bindConstant().annotatedWith(Names.named("db.jndi.datasource.name")).to("java:/comp/env/jdbc/che");

        // Core
        bindConstant().annotatedWith(Names.named("notification.server.propagate_events")).to("");

        // Docker
        bindConstant().annotatedWith(Names.named("che.docker.always_pull_image")).to("true");
        bindConstant().annotatedWith(Names.named("che.docker.privileged")).to("false");
        bindConstant().annotatedWith(Names.named("che.docker.registry_for_snapshots")).to(false);
        bindConstant().annotatedWith(Names.named("che.docker.swap")).to(-1.);
        bindConstant().annotatedWith(Names.named("che.docker.pids_limit")).to(-1);
        bindConstant().annotatedWith(Names.named("che.docker.cpu_period")).to(new Long(0));
        bindConstant().annotatedWith(Names.named("che.docker.cpu_quota")).to(new Long(0));
        bindConstant().annotatedWith(Names.named("che.docker.api")).to("1.20");
        bindConstant().annotatedWith(Names.named("che.docker.cpuset_cpus")).to("NULL");
        bindConstant().annotatedWith(Names.named("che.docker.network")).to("NULL");
        bindConstant().annotatedWith(Names.named("che.docker.network_driver")).to("NULL");
        bindConstant().annotatedWith(Names.named("che.docker.parent_cgroup")).to("NULL");
        bindConstant().annotatedWith(Names.named("che.docker.volumes_projects_options")).to("Z");
        bindConstant().annotatedWith(Names.named("che.docker.registry")).to("${CHE_REGISTRY_HOST}:5000");
        bindConstant().annotatedWith(Names.named("che.docker.namespace")).to("NULL");
        bindConstant().annotatedWith(Names.named("che.docker.ip")).to("NULL");
        bindConstant().annotatedWith(Names.named("che.docker.ip.external")).to("NULL");
        bindConstant().annotatedWith(Names.named("che.docker.volumes_agent_options")).to("ro,Z");
        bindConstant().annotatedWith(Names.named("che.docker.server_evaluation_strategy")).to("default");


        // Agent
        bindConstant().annotatedWith(Names.named("che.workspace.agent.dev.ping_conn_timeout_ms")).to(2000);
        bindConstant().annotatedWith(Names.named("che.workspace.agent.dev.ping_timeout_error_msg")).to("Timeout. The Che server is unable to ping your workspace. This implies a network configuration issue, workspace boot failure, or an unusually slow workspace boot.\n");
        bindConstant().annotatedWith(Names.named("che.workspace.agent.dev.ping_delay_ms")).to(new Long(2000));
        bindConstant().annotatedWith(Names.named("che.workspace.agent.dev.max_start_time_ms")).to(new Long(180000));
        bindConstant().annotatedWith(Names.named("che.workspace.agent.dev")).to("${che.home}/lib/ws-agent.tar.gz");
        bindConstant().annotatedWith(Names.named("che.workspace.terminal_linux_amd64")).to("${che.home}/lib/linux_amd64/terminal");



        bindConstant().annotatedWith(Names.named("che.agent.dev.max_start_time_ms")).to(new Long(120000));
        bindConstant().annotatedWith(Names.named("che.agent.dev.ping_delay_ms")).to(new Long(2000));

        bindConstant().annotatedWith(Names.named("machine.ws_agent.run_command"))
                      .to("export JPDA_ADDRESS=\"4403\" && ~/che/ws-agent/bin/catalina.sh jpda run");
        bindConstant().annotatedWith(Names.named("machine.terminal_agent.run_command"))
                      .to("$HOME/che/terminal/che-websocket-terminal " +
                          "-addr :4411 " +
                          "-cmd ${SHELL_INTERPRETER} " +
                          "-static $HOME/che/terminal/ " +
                          "-logs-dir $HOME/che/exec-agent/logs");


        // Templates
        bindConstant().annotatedWith(Names.named("che.template.storage")).to("null");


    }


    private void configureWebSocket() {
        requestStaticInjection(GuiceInjectorEndpointConfigurator.class);
        bind(WebSocketMessageTransmitter.class).to(BasicWebSocketMessageTransmitter.class);

        bind(WebSocketMessageReceiver.class).to(JsonRpcMessageReceiver.class);
    }

    private void configureJsonRpc() {
        install(new FactoryModuleBuilder().build(JsonRpcFactory.class));
        install(new FactoryModuleBuilder().build(RequestHandlerConfigurator.class));
        install(new FactoryModuleBuilder().build(BuildingRequestTransmitter.class));
    }

}
