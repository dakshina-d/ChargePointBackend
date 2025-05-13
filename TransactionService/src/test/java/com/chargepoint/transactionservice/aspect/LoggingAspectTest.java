package com.chargepoint.transactionservice.aspect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class LoggingAspectTest {
    private DummyService proxy;

    @BeforeEach
    void setup() {
        // create aspect and proxy
        LoggingAspect aspect = new LoggingAspect();
        AspectJProxyFactory factory = new AspectJProxyFactory(new DummyService());
        factory.addAspect(aspect);
        proxy = factory.getProxy();
    }

    @Test
    void logBeforeAndAfterReturningShouldRun() {
        String out = proxy.successful("input");
        assertEquals("processed:input", out);
    }

    @Test
    void logAfterExceptionShouldRun() {
        assertThrows(RuntimeException.class, () -> proxy.failing());
    }

    // Dummy target to test aspect weaving
    static class DummyService {
        public String successful(String x) {
            return "processed:" + x;
        }
        public void failing() {
            throw new RuntimeException("oops");
        }
    }
}
