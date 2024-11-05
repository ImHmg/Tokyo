
import static com.diogonunes.jcolor.Ansi.*;
import static com.diogonunes.jcolor.Attribute.*;
import org.junit.jupiter.api.Test;

public class ColorPrint {

    @Test
    public void te() {
        System.out.println(colorize("TEST", BLUE_TEXT(), YELLOW_BACK()));
    }
}
