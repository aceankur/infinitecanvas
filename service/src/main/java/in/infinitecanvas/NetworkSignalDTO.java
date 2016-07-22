package in.infinitecanvas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NetworkSignalDTO {
    private Double latitude;
    private Double longitude;
    private String carrier;
    private String network;
    private Integer strength;
}
