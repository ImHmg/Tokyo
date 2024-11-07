package co.mahmm.tokyo.commons.spec;

import co.mahmm.tokyo.core.Completion;
import lombok.*;
import org.junit.jupiter.api.function.Executable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportSpec {
    private String file;
    private String reportTitle;
    private String user;
    private Completion completion;
}
