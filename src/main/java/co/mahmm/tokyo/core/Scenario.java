package co.mahmm.tokyo.core;

import co.mahmm.tokyo.commons.Console;
import co.mahmm.tokyo.commons.Log;
import co.mahmm.tokyo.commons.spec.DataSpec;
import co.mahmm.tokyo.commons.spec.ScenarioSpec;
import co.mahmm.tokyo.commons.spec.StepSpec;
import co.mahmm.tokyo.http.HttpRequestStep;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import static com.diogonunes.jcolor.Ansi.*;
import static com.diogonunes.jcolor.Attribute.*;

@Getter
@Setter
public class Scenario {

    private Context context = new Context();
    private List<Step> steps = new ArrayList<>();
    private List<DataSpec> inputs = new ArrayList<>();
    private ScenarioSpec spec;

    public void initialize(ScenarioSpec spec, List<DataSpec> inputs) {
        this.inputs = inputs;
        this.spec = spec;
        context.setConfigs(spec.getConfigs());
    }


    public Stream<DynamicContainer> run() {
        Stream<DynamicContainer> preSteps = Stream.empty();
        Stream<DynamicContainer> postSteps = Stream.empty();
        Stream<DynamicContainer> scenarioSteps = Stream.empty();

        if(this.spec.getPreSteps() != null || !this.spec.getPreSteps().isEmpty()) {
            Log.debug("Adding pre steps");
             preSteps = Stream.of(DynamicContainer.dynamicContainer("Pre Steps", runSteps(this.spec.getPreSteps())));
        }

        scenarioSteps = this.inputs.stream().map(i -> {
            context.setInputs(i);
            String name = i.getName() == null ? "" : " : " + i.getName();
            return DynamicContainer.dynamicContainer("Scenario" + name, runSteps(this.spec.getSteps()));
        });

        if(this.spec.getPostSteps() != null || !this.spec.getPostSteps().isEmpty()) {
            Log.debug("Adding post steps");
            postSteps = Stream.of(DynamicContainer.dynamicContainer("Post Steps", runSteps(this.spec.getPreSteps())));
        }
        return Stream.concat(Stream.concat(preSteps, scenarioSteps), postSteps);
    }


    private Stream<DynamicTest> runSteps(List<StepSpec> stepSpecs) {
        Log.debug("Start running scenario");
        return stepSpecs.stream().map(spec -> {
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
                String inputName = "";
                if(context.getInputs().getName() != null) {
                    inputName = "["+ context.getInputs().getName() + "] ";
                }
                Console.print(colorize(" Start " + inputName + "[" + s.getSpec().getName() + "] ", BACK_COLOR(171, 142, 255), BOLD(), BLACK_TEXT()));
                Console.print("");
                Log.debug("Start pre processing {}", spec.getName());
                s.preProcess();
                Log.debug("Start processing {}", spec.getName());
                if(s.process()) {
                    Log.debug("Start post processing {}", spec.getName());
                    s.postProcess();
                }
                Console.print("");
                Console.print(colorize(" End [" + s.getSpec().getName() + "] ", BACK_COLOR(192, 192, 192), BOLD(), BLACK_TEXT()));
                Log.debug("End post processing {}", spec.getName());
                Console.print("\n");
            });
        });
    }

}
