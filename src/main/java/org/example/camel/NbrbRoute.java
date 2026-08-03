package org.example.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.example.dto.NbrbRateDto;
import org.springframework.stereotype.Component;

@Component
public class NbrbRoute extends RouteBuilder {
    @Override
    public void configure() {

        JacksonDataFormat format = new JacksonDataFormat ();
        format.setUnmarshalType(NbrbRateDto.class);
        format.setUseList(true);
        from("direct:startNbrbRoute")
                .routeId("NBRB")
                .toD("${properties:convector.nbrb.url}/exrates/rates?periodicity=0&date=${date:now:yyyy-MM-dd}")
                .unmarshal(format)
                .to("bean:dataLoadingTransaction?method=dataLoadingTransaction")
                .end();
    }
}
