package xdean.auto.message;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target({ TYPE, PACKAGE })
public @interface AutoMessage {
  String path();

  String generatedName() default "Messages";

  String charset() default "UTF-8";
}
