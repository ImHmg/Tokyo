package co.mahmm.tokyo.core;

import co.mahmm.tokyo.commons.Log;
import co.mahmm.tokyo.commons.spec.StepSpec;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

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
    }

    public abstract boolean process();

    public void preProcess() {
        if (preHook != null) {
            preHook.execute(this.spec, this.context);
        }else{
            Log.debug("Pre hook is null");
        }
    }

    public void postProcess() {
        if (postHook != null) {
            postHook.execute(this.spec, this.context);
        }else{
            Log.debug("Post hook is null");
        }
    }

    public String getVar(String key) {
        String val = this.context.getStepVariable(key);
        if (val != null) {
            Log.debug("Var value found in steps variables. key = {}, val = {}", key, val);
            return val;
        }
        if (this.context.getVars().containsKey(key)) {
            val = this.context.getVars().get(key);
            Log.debug("Var value found in context variables. key = {}, val = {}", key, val);
        }else if (this.spec.getConfigs().containsKey(key)) {
            val = this.spec.getConfigs().get(key);
            Log.debug("Var value found in current step configs. key = {}, val = {}", key, val);
        }else if (this.context.getInputData().containsKey(key)) {
            val = this.context.getInputData().get(key);
            Log.debug("Var value found in current inputs. key = {}, val = {}", key, val);
        }else if(this.context.getConfigs().containsKey(key)){
            val = this.context.getConfigs().get(key);
            Log.debug("Var value found in scenario config. key = {}, val = {}", key, val);
        }else{
            Log.debug("Var value not found for key = {}", key);
        }
        return val;
    }

    public void setVar(String key, String value) {
        this.context.getVars().put(key, value);
    }

    public String log(String message) {
        String fm = "";
        if(StringUtils.isNotBlank(context.getInputs().getName())) {
            fm += "[" + context.getInputs().getName() + "] ";
        }
        return fm + "[" + this.spec.getName() + "] -- " + message;
    }

    public abstract boolean isDone();

    public abstract String getStepVariables(String key);

}
