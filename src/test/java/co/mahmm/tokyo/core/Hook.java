package co.mahmm.tokyo.core;

import co.mahmm.tokyo.commons.spec.StepSpec;

public interface Hook {

    public void execute(StepSpec spec, Context context);

}
