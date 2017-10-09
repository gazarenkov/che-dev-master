package io.ga.infrastructure.localrecipe;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;
import org.eclipse.che.api.workspace.server.spi.RecipeContentParser;

/**
 * @author gazarenkov
 */
public class PropertiesParser implements RecipeContentParser<Properties>{
  @Override
  public Properties parse(String recipeContent, List<String> machineNames)  {

    Properties prop = new Properties();
    StringReader reader = new StringReader(recipeContent);
    try {
      prop.load(reader);
    } catch (IOException e) {
      // TODO other throwable exception
      throw new RuntimeException("Error " + e);
    }

    return prop;

  }
}
