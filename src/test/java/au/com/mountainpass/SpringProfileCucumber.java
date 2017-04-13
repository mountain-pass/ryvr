package au.com.mountainpass;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

import cucumber.api.junit.Cucumber;

public class SpringProfileCucumber extends Cucumber {
    private Logger logger = LoggerFactory
            .getLogger(SpringProfileCucumber.class);

    private RyvrRunListener ryvrRunListener;

    public SpringProfileCucumber(Class clazz)
            throws InitializationError, IOException {
        super(clazz);
        ryvrRunListener = new RyvrRunListener();
        ActiveProfiles ap = (ActiveProfiles) clazz
                .getAnnotation(ActiveProfiles.class);
        if (ap != null) {
            logger.info("spring.profiles.active: {}",
                    System.getProperty("spring.profiles.active"));
            String newActiveProfiles = String.join(",", ap.value());
            String activeProfiles = String.join(",",
                    System.getProperty("spring.profiles.active"),
                    newActiveProfiles);
            System.setProperty("spring.profiles.active", activeProfiles);
            logger.info("spring.profiles.active: {}",
                    System.getProperty("spring.profiles.active"));
        }
    }

    private void runPredefinedMethods(Class<?> annotation, RunNotifier notifier)
            throws Exception {
        if (!annotation.isAnnotation()) {
            return;
        }
        Method[] methodList = super.getTestClass().getJavaClass().getMethods();
        for (Method method : methodList) {
            Annotation[] annotations = method.getAnnotations();
            for (Annotation item : annotations) {
                if (item.annotationType().equals(annotation)) {
                    method.invoke(super.getTestClass().getJavaClass(),
                            ryvrRunListener.getResult());
                    break;
                }
            }
        }
    }

    class RyvrRunListener extends RunListener {

        private Result result;

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.junit.runner.notification.RunListener#testRunFinished(org.junit.
         * runner.Result)
         */
        @Override
        public void testRunFinished(Result result) throws Exception {
            // TODO Auto-generated method stub
            super.testRunFinished(result);
            this.result = result;
        }

        /**
         * @return the result
         */
        public Result getResult() {
            return result;
        }

    }

    @Override
    public void run(RunNotifier notifier) {
        notifier.addListener(ryvrRunListener);
        try {
            runPredefinedMethods(BeforeSuite.class, notifier);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.run(notifier);
        try {
            runPredefinedMethods(AfterSuite.class, notifier);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
