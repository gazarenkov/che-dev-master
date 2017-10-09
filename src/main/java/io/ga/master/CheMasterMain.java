package io.ga.master;

import com.google.inject.servlet.GuiceFilter;
import io.ga.master.doubtful.AutoAuthFilter;
import java.io.File;
import javax.servlet.ServletContextEvent;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.websocket.server.Constants;
import org.apache.tomcat.websocket.server.WsContextListener;
import org.eclipse.che.api.core.cors.CheCorsFilter;
import org.everrest.guice.servlet.GuiceEverrestServlet;
import org.everrest.websockets.WSConnectionTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author gazarenkov
 */
public class CheMasterMain {

  private static final Logger LOG = LoggerFactory.getLogger(CheMasterMain.class);

  public CheMasterMain() {

    ClassLoader cl = this.getClass().getClassLoader();
    System.out.println("Classloader : " + cl);

//    try {
//      Enumeration i = cl.getResources("");
//      while(i.hasMoreElements()) {
//        System.out.println(" >>>>>>>>>>>>>>> RES : " + i.nextElement());
//      }
//
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
  }

  public static void main(String[] args) throws Exception {

    new CheMasterMain();


    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "TRACE");

    Tomcat tomcat = new Tomcat();
    tomcat.setPort(8080);

    String f = new File("webapp").getAbsolutePath();

    Context ctx = tomcat.addContext("", f);


    //Tomcat.initWebappDefaults(ctx);

    ///////////////

    Wrapper servlet = Tomcat.addServlet(
        ctx, "default", "org.apache.catalina.servlets.DefaultServlet");
    servlet.setLoadOnStartup(1);
    servlet.setOverridable(true);


    // Servlet mappings
    ctx.addServletMapping("/", "default");
    ///////////////


    LOG.info("Context DocBase : " + f + " " + ctx.getLoader() + " " + LOG.getClass());


    ctx.addParameter("org.everrest.websocket.context", "/api");
    ctx.addParameter("org.eclipse.che.websocket.endpoint", "/ws");
    ctx.addParameter("org.eclipse.che.eventbus.endpoint", "/eventbus");


    //////
    // sets "javax.websocket.server.ServerContainer" attribute
    //ctx.addServletContainerInitializer(new WsSci(), null);


    ctx.addApplicationListener(WsConfig.class.getName());

    ///////


    ctx.addApplicationListener(MasterBootstrap.class.getName());

    //ctx.addApplicationListener(ServerContainerInitializeListener.class.getName());

    ctx.addApplicationListener(WSConnectionTracker.class.getName());


    addFilter(ctx, CheCorsFilter.class.getName(), "/*", "cors");
    addFilter(ctx, GuiceFilter.class.getName(), "/api/*", "guice");
    addFilter(ctx, AutoAuthFilter.class.getName(), "/api/*", "auth");

    Wrapper w = Tomcat.addServlet(ctx, "everrest", GuiceEverrestServlet.class.getName());
    w.addMapping("/api/*");


    tomcat.enableNaming();
    ContextResource db = addDb();
    ctx.getNamingResources().addResource(db);

    tomcat.start();

    tomcat.getServer().await();

  }

  private static ContextResource addDb() {

    ContextResource resource = new ContextResource();
    resource.setName("jdbc/che");
    resource.setAuth("Container");
    resource.setType("javax.sql.DataSource");
    resource.setScope("Sharable");
    resource.setProperty("driverClassName", "org.h2.Driver");
    resource.setProperty("url", "jdbc:h2:che");
    resource.setProperty("username", "");
    resource.setProperty("password", "");
    resource.setProperty("maxTotal", "8");
    resource.setProperty("maxIdle", "4");
    //resource.setProperty("factory", "org.apache.tomcat.jdbc.pool.DataSourceFactory");


    //tomcat.getServer().getGlobalNamingResources().addResource(resource);

    return resource;

  }


  private static void addFilter(Context ctx, String clazz, String mapping, String name) {

    FilterDef filterDef = new FilterDef();
    filterDef.setFilterName(name);
    filterDef.setFilterClass(clazz);
    FilterMap filterMap = new FilterMap();
    filterMap.setFilterName(name);
    filterMap.addURLPattern(mapping);
    ctx.addFilterDef(filterDef);
    ctx.addFilterMap(filterMap);


  }

  public static class WsConfig extends WsContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
      super.contextInitialized(sce);
      ServerContainer sc =
          (ServerContainer)sce.getServletContext().getAttribute(
              Constants.SERVER_CONTAINER_SERVLET_CONTEXT_ATTRIBUTE);

      try {
        sc.addEndpoint(CheWebSocketEndpoint.class);
      } catch (DeploymentException e) {
        throw new IllegalStateException(e);
      }

      LOG.debug("ServerContainer: " + sc);

    }

  }



}
