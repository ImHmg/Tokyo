package co.mahmm.tokyo;

import co.mahmm.tokyo.commons.FileReader;
import co.mahmm.tokyo.commons.Log;
import co.mahmm.tokyo.commons.YamlParser;
import co.mahmm.tokyo.commons.spec.DataSpec;
import co.mahmm.tokyo.commons.spec.ScenarioSpec;
import co.mahmm.tokyo.core.Scenario;
import co.mahmm.tokyo.core.Step;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DynamicContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Getter
@Setter
public class SpecRunner {

    private String specFile;
    private List<String> configFiles;
    private ScenarioSpec spec;
    private List<Step> steps = new ArrayList<>();

    public SpecRunner(String specFile, List<String> configFiles) {
        this.specFile = specFile;
        this.configFiles = configFiles;
        this.parseFiles();
    }

    public Stream<DynamicContainer> run() {
        List<DataSpec> i = new ArrayList<>();
        if (this.spec.getInputs().size() == 0) {
            Log.debug("Scenario inputs not found. Running one round");
            i.add(new DataSpec());
        }else{
            i = this.spec.getInputs();
        }
        Scenario scenario = new Scenario();
        scenario.initialize(this.spec, i);
        return scenario.run();
    }


    private void parseFiles() {
        Log.debug("Parse scenario file = {}", this.specFile);
        String content = FileReader.readFile(this.specFile);
        this.spec = YamlParser.parse(content, ScenarioSpec.class);
        Map<String, String> configs = new HashMap<>();
        configs.putAll(this.spec.getConfigs());
        if(this.configFiles != null) {
            for (String configFile : this.configFiles) {
                Log.debug("Parse config file = {}", configFile);
                Map<String, String> env = YamlParser.parse(FileReader.readFile(configFile), Map.class);
                configs.putAll(env);
            }
        }
        Log.debug("All loaded config for scenario = {}", configs);
        this.spec.setConfigs(configs);

    }


}
