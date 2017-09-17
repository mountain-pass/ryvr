package au.com.mountainpass.ryvr.tests.common.steps;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class CommonState {

  private String scenarioId;

  public void setScenarioId(String scenarioId) {
    this.scenarioId = scenarioId;
  }

  public Set<String> uniquifyRyvrNames(List<String> names) {
    return names.stream().map(name -> uniquifyRyvrName(name)).collect(Collectors.toSet());
  }

  public String uniquifyRyvrName(String string) {
    return string + "-" + scenarioId.replace(";", "-");
  }

}
