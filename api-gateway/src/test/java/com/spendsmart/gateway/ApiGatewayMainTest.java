package com.spendsmart.gateway;

import org.junit.jupiter.api.Test;

class ApiGatewayMainTest {
    @Test
    void mainStartsWithRandomPortAndDiscoveryDisabled() {
        ApiGatewayApplication.main(new String[]{
                "--server.port=0",
                "--eureka.client.enabled=false",
                "--spring.cloud.discovery.enabled=false"
        });
    }
}