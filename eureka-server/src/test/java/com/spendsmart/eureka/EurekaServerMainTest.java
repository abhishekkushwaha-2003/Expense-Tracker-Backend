package com.spendsmart.eureka;

import org.junit.jupiter.api.Test;

class EurekaServerMainTest {
    @Test
    void mainStartsWithRandomPort() {
        EurekaServerApplication.main(new String[]{
                "--server.port=0",
                "--eureka.client.register-with-eureka=false",
                "--eureka.client.fetch-registry=false"
        });
    }
}