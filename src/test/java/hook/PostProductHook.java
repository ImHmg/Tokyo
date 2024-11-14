package hook;

import io.github.imhmg.tokyo.core.Hook;
import io.github.imhmg.tokyo.core.Step;

public class PostProductHook implements Hook {

    @Override
    public void execute(Step step) {
        System.out.println("Configs product id " + step.getVar("productId"));

    }
}
