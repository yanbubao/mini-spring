package april.spring.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author yanzx
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MiniController {
    String value() default "";
}
