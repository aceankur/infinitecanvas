package in.infinitecanvas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MockNetworkSignalDTO {
    private Double latStart;
    private Double lngStart;
    private Double latEnd;
    private Double lngEnd;
}
