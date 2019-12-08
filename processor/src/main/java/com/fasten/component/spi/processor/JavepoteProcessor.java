package com.fasten.component.spi.processor;

import com.fasten.component.spi.annotations.ServiceProvider;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * 作者:created by storm on 2019-12-08
 */

public class JavepoteProcessor extends AbstractProcessor {

    private Elements elementUtil;//Element帮助类

    private Messager mMessager;//日志输出类


    private static final String BINDER_SUFFIX = "Binder";
    private static final String SEPARATE_SYMBOL = "$$";

    private static final String MODULE_NAME = "moduleName";

    private String moduleNameValue;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return super.getSupportedSourceVersion();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ServiceProvider.class.getCanonicalName());
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtil = processingEnvironment.getElementUtils();
        mMessager = processingEnvironment.getMessager();
        Map<String, String> optionsMap = processingEnvironment.getOptions();
        moduleNameValue = optionsMap.get(MODULE_NAME);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(ServiceProvider.class)) {
            if (element instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) element;
                ClassName generateClazzName = ClassName.get(getPackageName(typeElement), generateClassName(typeElement));
                ClassName superClassName = ClassName.get("android.app", "Activity");
                TypeSpec.Builder tsb = TypeSpec.classBuilder(generateClazzName)
                        .addModifiers(Modifier.PUBLIC)
                        .superclass(superClassName)
                        .addJavadoc("Represent the class $T of {@link $T}\n",generateClazzName,ClassName.get(typeElement));
                ClassName overrideClassName = ClassName.get("java.lang", "Override");
                ClassName nullableClassName = ClassName.get("android.support.annotation", "Nullable");
                ClassName bundleClassName = ClassName.get("android.os", "Bundle");
                MethodSpec methodSpec = MethodSpec.methodBuilder("onCreate")
                        .addAnnotation(overrideClassName)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(bundleClassName, "savedInstanceState").addAnnotation(nullableClassName).build())
                        .addStatement("super.onCreate(savedInstanceState)")
                        .addStatement("setContentView(R.layout.activity_main)")
                        .returns(TypeName.VOID).build();
                tsb.addMethod(methodSpec);
                try {
                    JavaFile.builder(getPackageName(typeElement), tsb.build())
                            .skipJavaLangImports(true)
                            .addFileComment("Automatically Generate file,DO NOT MODIFY\n")
                            .build().writeTo(processingEnv.getFiler());
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }


        return false;
    }

    private String getPackageName(TypeElement typeElement) {
        return elementUtil.getPackageOf(typeElement).getQualifiedName().toString();
    }

    //ModuleName$$sourceClassName$$SUFFIX
    private String generateClassName(TypeElement element) {
        String sourceClassName = element.getSimpleName().toString();
        StringBuilder builder = new StringBuilder();
        if (moduleNameValue != null) {
            builder.append(moduleNameValue).append(SEPARATE_SYMBOL);
        }
        builder.append(sourceClassName).append(SEPARATE_SYMBOL).append(BINDER_SUFFIX);
        return builder.toString();
    }
}
