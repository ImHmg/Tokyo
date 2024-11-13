package co.mahmm.tokyo;

import co.mahmm.tokyo.commons.FileReader;
import co.mahmm.tokyo.commons.Log;
import co.mahmm.tokyo.commons.YamlParser;
import co.mahmm.tokyo.commons.spec.DataSpec;
import co.mahmm.tokyo.commons.spec.RunSpec;
import co.mahmm.tokyo.commons.spec.ScenarioSpec;
import co.mahmm.tokyo.commons.spec.StepSpec;
import co.mahmm.tokyo.core.Scenario;
import com.opencsv.CSVReader;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DynamicContainer;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Stream;

@Getter
@Setter
public class SpecRunner {
    private ScenarioSpec spec;
    private RunSpec runSpec;
    private Scenario scenario;

    public SpecRunner(RunSpec runSpec) {

        this.runSpec = runSpec;
        this.parseFiles();
        this.validateFiles();
    }

    public Stream<DynamicContainer> run() {
        List<DataSpec> i = new ArrayList<>();
        if (this.spec.getInputs().size() == 0) {
            Log.debug("Scenario inputs not found. Running one round");
            i.add(new DataSpec());
        }else{
            i = this.spec.getInputs();
        }
        scenario = new Scenario();
        scenario.initialize(this.spec, i);
        return scenario.run();
    }


    private void parseFiles() {
        Log.debug("Parse scenario file = {}", this.runSpec.getScenarioSpecFile());
        String content = FileReader.readFile(this.runSpec.getScenarioSpecFile());
        this.spec = YamlParser.parse(content, ScenarioSpec.class);
        Map<String, Object> configs = new HashMap<>();
        configs.putAll(this.spec.getConfigs());
        if(this.runSpec.getConfigFiles() != null) {
            for (String configFile : this.runSpec.getConfigFiles()) {
                Log.debug("Parse config file = {}", configFile);
                Map<String, Object> env = YamlParser.parse(FileReader.readFile(configFile), Map.class);
                configs.putAll(env);
            }
        }
        parseInputFile();
        Log.debug("All loaded config for scenario = {}", configs);
        this.spec.setConfigs(configs);
    }

    private void parseInputFile() {
        if(this.runSpec.getInputFile() == null) {
            return;
        }
        try {
            CSVReader reader = new CSVReader(new StringReader(FileReader.readFile(this.runSpec.getInputFile())));
            List<String[]> lines = reader.readAll();
            if(lines.size() <= 1) {
                throw new RuntimeException("Input files must have rows");
            }
            String[] header = lines.get(0);
            if(header.length == 0) {
                throw new RuntimeException("Input files invalid header, header should be in #Name#,col1,col2,col3....");
            }
            if(!Objects.equals(header[0], "#Name#")) {
                throw new RuntimeException("Input files invalid header, header should be in #Name#,col1,col2,col3....");
            }
            Map<Integer, String> format = new HashMap<>();
            for (int i = 0; i < header.length; i++) {
                format.put(i, header[i]);
            }
            List<DataSpec> inputs = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                if(lines.get(i).length != format.size()) {
                    throw new RuntimeException("Input files invalid row (column count doesnt match) " + i);
                }
                DataSpec dataSpec = new DataSpec();
                dataSpec.setName(lines.get(i)[0]);
                Map<String, Object> data = new HashMap<>();
                for (Map.Entry<Integer, String> entry : format.entrySet()) {
                    if(Objects.equals(entry.getValue(), "#Name#")) {
                        continue;
                    }
                    data.put(entry.getValue(), lines.get(i)[entry.getKey()]);
                }
                dataSpec.setData(data);
                inputs.add(dataSpec);
            }
            this.spec.setInputs(inputs);
        }catch (Exception e) {

            throw new RuntimeException("Error while parsing input file", e);
        }
    }
    private void validateFiles() {
        Map<String, String> inputs = new HashMap<>();
        if(this.getSpec().getInputs() != null) {
            for (DataSpec input : this.getSpec().getInputs()) {
                if(inputs.containsKey(input.getName())) {
                    throw new IllegalArgumentException("Input name cannot be duplicate. Duplicate input name " + input.getName());
                }
                inputs.put(input.getName(), input.getName());
            }
        }
        Map<String, String> stepIds = new HashMap<>();
        Map<String, String> stepNames = new HashMap<>();

        for (StepSpec step : this.getSpec().getPreSteps()) {
            if(stepIds.containsKey(step.getId())) {
                throw new IllegalArgumentException("Step id cannot be duplicated. Duplicate step id " + step.getId());
            }
            stepIds.put(step.getId(), step.getId());

            if(stepNames.containsKey(step.getName())) {
                throw new IllegalArgumentException("Step name cannot be duplicated. Duplicate step name " + step.getName());
            }
            stepNames.put(step.getName(), step.getName());
            validateStep(step);
        }
        for (StepSpec step : this.getSpec().getSteps()) {
            if(stepIds.containsKey(step.getId())) {
                throw new IllegalArgumentException("Step id cannot be duplicated. Duplicate step id " + step.getId());
            }
            stepIds.put(step.getId(), step.getId());

            if(stepNames.containsKey(step.getName())) {
                throw new IllegalArgumentException("Step name cannot be duplicated. Duplicate step name " + step.getName());
            }
            stepNames.put(step.getName(), step.getName());
            validateStep(step);
        }
        for (StepSpec step : this.getSpec().getPostSteps()) {
            if(stepIds.containsKey(step.getId())) {
                throw new IllegalArgumentException("Step id cannot be duplicated. Duplicate step id " + step.getId());
            }
            stepIds.put(step.getId(), step.getId());

            if(stepNames.containsKey(step.getName())) {
                throw new IllegalArgumentException("Step name cannot be duplicated. Duplicate step name " + step.getName());
            }
            stepNames.put(step.getName(), step.getName());
            validateStep(step);
        }
    }

    private void validateStep(StepSpec stepSpec) {
        if(StringUtils.isEmpty(stepSpec.getId())) {
            throw new IllegalArgumentException("Step id cannot be null");
        }

        if(StringUtils.isEmpty(stepSpec.getName())) {
            throw new IllegalArgumentException("Step name cannot be null");
        }

        if(StringUtils.isEmpty(stepSpec.getRef())) {
            throw new IllegalArgumentException("Step ref cannot be null");
        }

    }


}
