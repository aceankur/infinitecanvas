package in.infinitecanvas;

import lombok.extern.slf4j.Slf4j;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.DoubleMapper;
import org.skife.jdbi.v2.util.IntegerMapper;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Path("/")
@Slf4j
public class ServiceResource {
    private DBI dbi;

    public ServiceResource(DBI dbi) {
        this.dbi = dbi;
    }

    @POST
    @Path("/signal/network/mock")
    @Consumes(MediaType.APPLICATION_JSON)
    public void mockNetworkSignals(@NotNull MockNetworkSignalDTO data) {
        List<String> carriers = Arrays.asList("airtel", "vodafone");
        List<String> networks = Arrays.asList("2g", "3g", "4g");

        Random random = new Random();
        Double lat = new Double(data.getLatStart());
        Double lng;
        while(lat <= data.getLatEnd()) {
            lng = new Double(data.getLngStart());
            while(lng < data.getLngEnd()) {
                int strength = (int) Math.round(getBiasedRandom(4.0, 0.0, 4.0));
                String geo = "'POINT(" + lat + " " + lng + ")'";
                try (Handle h = dbi.open()) {
                    h.createStatement("insert into network_signals (latitude, longitude, carrier, network, strength, geo) " +
                            "values (:latitude, :longitude, :carrier, :network, :strength, " + geo + ")")
                            .bind("latitude", lat)
                            .bind("longitude", lng)
                            .bind("carrier", carriers.get(random.nextInt(carriers.size())))
                            .bind("network", networks.get(random.nextInt(networks.size())))
                            .bind("strength", strength)
                            .execute();
                }
                lng += 0.0005;
            }
            lat += 0.0005;
        }
    }

    @POST
    @Path("/signal/network")
    @Consumes(MediaType.APPLICATION_JSON)
    public void addNetworkSignal(@NotNull NetworkSignalDTO data) {
        String geo = "'POINT(" + data.getLatitude() + " " + data.getLongitude() + ")'";
        try (Handle h = dbi.open()) {
            h.createStatement("insert into network_signals (latitude, longitude, carrier, network, strength, geo) " +
                    "values (:latitude, :longitude, :carrier, :network, :strength, " + geo + ")")
                    .bind("latitude", data.getLatitude())
                    .bind("longitude", data.getLongitude())
                    .bind("carrier", data.getCarrier())
                    .bind("network", data.getNetwork())
                    .bind("strength", data.getStrength())
                    .execute();
        }
    }

    @GET
    @Path("/signal/network")
    public Double getAverageStrength(@NotNull @QueryParam("sw_lng") Double swLng,
                                     @NotNull @QueryParam("sw_lat") Double swLat,
                                     @NotNull @QueryParam("ne_lng") Double neLng,
                                     @NotNull @QueryParam("ne_lat") Double neLat,
                                     @QueryParam("carrier") String carrier,
                                     @QueryParam("network") String network) {
        try (Handle h = dbi.open()) {
            String query = "SELECT AVG(strength) FROM network_signals WHERE (geo && " +
                    "ST_MakeEnvelope(" + swLat + ", " + swLng + ", " + neLat + ", " + neLng + "))";
            query += " AND carrier = '" + carrier + "'";
            if(network != null && !network.isEmpty()) {
                query += " AND network = '" + network + "'";
            }
            Double avgStrength = h.createQuery(query)
                    .map(DoubleMapper.FIRST).first();
            return avgStrength;
        }
    }


    //http://stackoverflow.com/a/17990761
    private double getBiasedRandom(double bias, double min, double max) {
        double centered_depth_perc = 0.3;
        double centered_depth_abs = (max - min)*centered_depth_perc;
        double center = 0.5;

        Random tRandom = new Random();
        double rndCentered = center  + tRandom .nextGaussian() * centered_depth_abs; // generate centered random number.
        double rndBiased;

        if (rndCentered >= center)
            rndBiased = (rndCentered - center) * (max - bias) + bias;
        else
            rndBiased = bias - (center - rndCentered) * (bias - min);

        // the following two tests will be as more important as centered_depth_perc
        // get bigger.
        if (rndBiased > max)
            rndBiased = max;

        if (rndBiased < min)
            rndBiased = min;

        return rndBiased;
    }
}
