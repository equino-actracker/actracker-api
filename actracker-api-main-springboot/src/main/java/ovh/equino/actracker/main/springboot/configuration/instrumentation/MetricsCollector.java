package ovh.equino.actracker.main.springboot.configuration.instrumentation;

import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Component
public class MetricsCollector {

    @Value("${actracker-api.environment:local}")
    private String applicationEnvironment;

    @Autowired
    private MeterRegistry meterRegistry;

    public Object measureAndExecute(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String metricName = "actracker-api.%s.%s.%s".formatted(applicationEnvironment, className, methodName);

        Callable<Object> procedure = () -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };

        try {
            return meterRegistry.timer(metricName).recordCallable(procedure);
        } catch (RuntimeException e) {
            throw e.getCause();
        }
    }
}
