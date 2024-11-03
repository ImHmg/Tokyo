package co.mahmm.tokyo.hook;

import co.mahmm.tokyo.core.Hook;
import co.mahmm.tokyo.core.Step;

public class PostProductHook implements Hook {

    @Override
    public void execute(Step step) {
        System.out.println("Configs product id " + step.getVar("productId"));
    }
}
