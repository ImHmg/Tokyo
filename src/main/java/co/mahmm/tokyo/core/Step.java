package co.mahmm.tokyo.core;

import co.mahmm.tokyo.commons.AssertResult;
import co.mahmm.tokyo.commons.Console;
import co.mahmm.tokyo.commons.Log;
import co.mahmm.tokyo.commons.spec.StepSpec;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.List;

import static com.diogonunes.jcolor.Ansi.*;
import static com.diogonunes.jcolor.Attribute.*;

@Getter
@Setter
public abstract class Step {

    private StepSpec spec;
    private Context context;
    private Hook preHook;
    private Hook postHook;

    public Step(StepSpec spec, Context context) {
        this.spec = spec;
        this.context = context;
        this.preHook = getHook(this.spec.getPreHook());
        this.postHook = getHook(this.spec.getPostHook());
    }

    public abstract boolean process();

    public void preProcess() {
        if (preHook != null) {
            Console.print(colorize("Executing pre hook [" + this.spec.getPreHook() + "]", BOLD(), UNDERLINE(), TEXT_COLOR(152)));
            preHook.execute(this);
            Console.print("");
        } else {
            Log.debug("Pre hook is null");
        }
    }

    public void postProcess() {
        if (postHook != null) {
            Console.print("");
            Console.print(colorize("Executing post hook [" + this.spec.getPostHook() + "]", BOLD(), UNDERLINE(), TEXT_COLOR(152)));
            postHook.execute(this);
            Console.print("");
        } else {
            Log.debug("Post hook is null");
        }
    }

    public String getVar(String key) {
        String val = this.context.getStepVariable(key);
        if (val != null) {
            Log.debug("Var value found in steps variables. key = {}, val = {}", key, val);
            return val;
        }
        if (this.getContext().getVars().containsKey(key)) {
            val = String.valueOf(this.getContext().getVars().get(key));
            Log.debug("Var value found in context variables. key = {}, val = {}", key, val);
        } else if (this.spec.getConfigs().containsKey(key)) {
            val = this.spec.getConfigs().get(key);
            Log.debug("Var value found in current step configs. key = {}, val = {}", key, val);
        } else if (this.context.getInputData().containsKey(key)) {
            val = String.valueOf(this.context.getInputData().get(key));
            Log.debug("Var value found in current inputs. key = {}, val = {}", key, val);
        } else if (this.context.getConfigs().containsKey(key)) {
            val = String.valueOf(this.context.getConfigs().get(key));
            Log.debug("Var value found in scenario config. key = {}, val = {}", key, val);
        } else {
            Log.debug("Var value not found for key = {}", key);
        }
        return val;
    }

    public void setVar(String key, String value) {
        this.getContext().getVars().put(key, value);
    }

    public abstract boolean isDone();

    public abstract String getStepVariables(String key);

    private Hook getHook(String hookRef) {
        if (hookRef == null) {
            return null;
        }
        Log.debug("Initialize hook = {}", hookRef);
        Class<?> clazz = null;
        try {
            clazz = Class.forName(hookRef);
            Hook instance = (Hook) clazz.getDeclaredConstructor().newInstance();
            return instance;
        } catch (Exception e) {
            Log.error("Error while initializing hook : {}", hookRef);
            throw new RuntimeException(e);
        }

        // Initialize a new instance of the class
    }

    public String getPrompt(String key) {
        if (key == null) {
            return null;
        }
        if (!key.startsWith("prompt ")) {
            return null;
        }
        String text = key.replace("prompt ", "");
        String name = JOptionPane.showInputDialog(null, text, this.spec.getName(), JOptionPane.QUESTION_MESSAGE);
        return name;
    }

    public abstract boolean isPassed();

    public abstract long getTime();

    public abstract List<AssertResult> getAssertsResults();

    public abstract String getAdditionalDetails();


}
