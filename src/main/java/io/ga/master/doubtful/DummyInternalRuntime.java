package io.ga.master.doubtful;

import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;

import java.util.Map;

/**
 * @author gazarenkov
 */
public class DummyInternalRuntime extends InternalRuntime {

    public DummyInternalRuntime(RuntimeContext context,
                                URLRewriter urlRewriter) {
        super(context, urlRewriter);
    }

    @Override
    public Map<String, ? extends Machine> getInternalMachines() {
        return null;
    }

    @Override
    public Map<String, String> getProperties() {
        return null;
    }
}
