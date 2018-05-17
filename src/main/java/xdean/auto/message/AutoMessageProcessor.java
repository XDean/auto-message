package xdean.auto.message;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Generated;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

import xdean.annotation.processor.toolkit.AssertException;
import xdean.annotation.processor.toolkit.CommonUtil;
import xdean.annotation.processor.toolkit.XAbstractProcessor;
import xdean.annotation.processor.toolkit.annotation.SupportedAnnotation;

@AutoService(Processor.class)
@SupportedAnnotation(AutoMessage.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AutoMessageProcessor extends XAbstractProcessor {

  @Override
  public boolean processActual(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws AssertException {
    if (roundEnv.processingOver()) {
      return true;
    }
    roundEnv.getElementsAnnotatedWith(AutoMessage.class).forEach(e -> handle(e));
    return true;
  }

  private void handle(Element type) {
    assertThat((type.getKind() == ElementKind.CLASS && ((TypeElement) type).getNestingKind() == NestingKind.TOP_LEVEL) ||
        type.getKind() == ElementKind.PACKAGE)
            .todo(() -> error().log("Can only annotated on top-level class or package.", type));
    String packageName = (type.getKind() == ElementKind.PACKAGE ? type : type.getEnclosingElement()).toString();
    // eclipse apt return packageName as 'package a.b.c'
    if (packageName.startsWith("package ")) {
      packageName = packageName.substring(8);
    }
    AutoMessage am = type.getAnnotation(AutoMessage.class);
    String originFile = am.path();
    boolean root = originFile.startsWith("/");
    String file = root ? originFile.substring(1) : originFile;
    try {
      FileObject resource;
      try {
        resource = processingEnv.getFiler()
            .getResource(StandardLocation.CLASS_PATH, root ? "" : packageName, file);
      } catch (Exception e) {
        debug().log("Fail to get file from CLASS_PATH, try CLASS_OUTPUT.");
        try {
          resource = assertNonNull(processingEnv.getFiler()
              .getResource(StandardLocation.CLASS_OUTPUT, root ? "" : packageName, file))
                  .log("Can't find file " + file, type);
        } catch (Exception e1) {
          error().log("Fail to get file from CLASS_OUTPUT");
          throw e1;
        }
      }

      BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openInputStream(), am.charset()));
      Builder builder = TypeSpec.interfaceBuilder(am.generatedName())
          .addAnnotation(
              AnnotationSpec.builder(Generated.class).addMember("value", "$S", AutoMessageProcessor.class.getName()).build())
          .addModifiers(Modifier.PUBLIC);
      AtomicInteger lineNumber = new AtomicInteger(0);
      reader.lines()
          .filter(s -> !s.startsWith("#"))
          .filter(s -> s.trim().length() != 0)
          .map(s -> extractKey(s, file, lineNumber.incrementAndGet(), type))
          .map(s -> {
            String name = dotToUnder(s, type);
            return FieldSpec.builder(String.class, name)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", s)
                .build();
          })
          .forEach(builder::addField);
      JavaFile build = JavaFile.builder(packageName, builder.build())
          .build();
      build.writeTo(processingEnv.getFiler());
    } catch (Exception e) {
      error().log("Fail to read " + file + " because " + CommonUtil.getStackTraceString(e), type);
      return;
    }
  }

  private String extractKey(String line, String fileName, int lineNumber, Element type) {
    int index = line.indexOf("=");
    assertThat(index != -1).todo(() -> error().log(String.format("Bad define at line %d of %s", lineNumber, fileName), type));
    return line.substring(0, index).trim();
  }

  private String dotToUnder(String key, Element type) {
    return key.replace('.', '_').replace('-', '_').toUpperCase();
  }
}
