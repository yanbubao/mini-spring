package april.spring.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author yanzx
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MiniRequestParam {
    String value() default "";
}
