package co.mahmm.tokyo.core;

import co.mahmm.tokyo.commons.Log;
import co.mahmm.tokyo.commons.spec.DataSpec;
import co.mahmm.tokyo.commons.spec.ScenarioSpec;
import co.mahmm.tokyo.commons.spec.StepSpec;
import co.mahmm.tokyo.http.HttpRequestStep;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DynamicTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Getter
@Setter
public class Scenario {

    private Context context = new Context();
    private List<Step> steps = new ArrayList<>();
    private ScenarioSpec spec;

    public void initialize(ScenarioSpec spec, DataSpec input) {
        this.spec = spec;
        context.setInputs(input);
        context.setConfigs(spec.getConfigs());
    }

    public Stream<DynamicTest> run() {
        Log.debug("Start running scenario");
        return this.spec.getSteps().stream().map(spec -> {
            Step step = null;
            if (spec.equals("custom")) {

            }else{
                Log.debug("Initialize http step = {}", spec.getName());
                step = new HttpRequestStep(spec, this.context);
            }
            this.steps.add(step);
            this.context.setSteps(this.steps);

            Step s = step;
            return DynamicTest.dynamicTest("Step : " + step.getSpec().getName(), () -> {
                Log.debug("Start pre processing {}", spec.getName());
                s.preProcess();
                Log.debug("Start processing {}", spec.getName());
                if(s.process()) {
                    Log.debug("Start post processing {}", spec.getName());
                    s.postProcess();
                }
                Log.debug("End post processing {}", spec.getName());
            });
        });
    }

}
