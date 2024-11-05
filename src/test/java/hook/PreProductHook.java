package hook;

import co.mahmm.tokyo.core.Hook;
import co.mahmm.tokyo.core.Step;

public class PreProductHook implements Hook {

    @Override
    public void execute(Step step) {
        System.out.println("Configs product name " + step.getVar("productName"));
    }
}