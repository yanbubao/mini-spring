package april.spring.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author yanzx
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MiniRequestMapping {
    String value() default "";
}
