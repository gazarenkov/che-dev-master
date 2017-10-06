package io.ga.master;

import com.google.inject.Module;

import org.eclipse.che.inject.CheBootstrap;

import java.util.List;

/**
 * @author gazarenkov
 */
public class MasterBootstrap extends CheBootstrap {

  @Override
  protected List<Module> getModules() {

    List<Module> m = super.getModules();

    m.add(new MasterModule());

    return m;

  }


}
