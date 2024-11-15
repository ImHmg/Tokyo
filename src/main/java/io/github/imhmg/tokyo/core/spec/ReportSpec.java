package io.github.imhmg.tokyo.core.spec;

import io.github.imhmg.tokyo.core.Completion;
import lombok.*;

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
