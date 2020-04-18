package april.spring.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author yanzx
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MiniAutowired {
    String value() default "";
}
