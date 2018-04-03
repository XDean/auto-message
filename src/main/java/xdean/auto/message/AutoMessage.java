package xdean.auto.message;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Properties;

/**
 * Auto generate {@link Properties} file's keys.
 *
 * @author Dean Xu (XDean@github.com)
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, PACKAGE })
public @interface AutoMessage {
  /**
   * The properties path. The path has same rule with
   * {@link Class#getResource(String)}.
   *
   * @see Class#getResource(String)
   */
  String path();

  /**
   * Generated java file's name.
   */
  String generatedName() default "Messages";

  /**
   * The properties file charset.
   */
  String charset() default "UTF-8";
}
