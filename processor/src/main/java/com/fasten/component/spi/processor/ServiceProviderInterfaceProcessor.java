package com.fasten.component.spi.processor;

import com.fasten.component.spi.annotations.ServiceProviderInterface;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * 作者:created by storm on 2019-12-07
 */
public class ServiceProviderInterfaceProcessor extends AbstractProcessor {

    private Elements mElementUtils;

    private Messager mMessager;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ServiceProviderInterface.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mElementUtils = processingEnvironment.getElementUtils();
        mMessager = processingEnvironment.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //永远不要设置Kind.ERROR，不然会编译不过去，这里被坑了好久
        for (Element element:roundEnvironment.getElementsAnnotatedWith(ServiceProviderInterface.class)){
            if (element instanceof TypeElement) {
                final TypeElement typeElement = (TypeElement) element;
                final String packageName = getPackageName(typeElement);
                mMessager.printMessage(Diagnostic.Kind.WARNING,"packageName>>>>>");
                final ClassName clazzServiceProviderInterface = ClassName.get(typeElement);
                final ClassName clazzServiceLoader = ClassName.get(getServiceLoaderPackageName(), "ServiceLoader");

                mMessager.printMessage(Diagnostic.Kind.WARNING,"clazzServiceLoader>>>>>"+clazzServiceLoader.packageName()+"::"+clazzServiceLoader.simpleName());

                final ClassName clazzService = ClassName.get(packageName, getServiceName(typeElement));

                final ClassName clazzSingleton = ClassName.get(packageName, clazzService.simpleName(), "Singleton");


                final TypeSpec.Builder tsb = TypeSpec.classBuilder(clazzService)
                        .addJavadoc("Represents the service of {@link $T}\n", clazzServiceProviderInterface)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addSuperinterface(clazzServiceProviderInterface)
                        .addType(TypeSpec.classBuilder(clazzSingleton.simpleName())
                                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                                .addField(FieldSpec.builder(clazzService, "INSTANCE", Modifier.STATIC, Modifier.FINAL)
                                        .initializer("new $T()", clazzService)
                                        .build())
                                .build())
                        .addField(FieldSpec.builder(clazzServiceProviderInterface, "mDelegate", Modifier.PRIVATE, Modifier.FINAL)
                                .initializer("$T.load($T.class).get()", clazzServiceLoader, clazzServiceProviderInterface)
                                .build())
                        .addMethod(MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PRIVATE)
                                .build())
                        .addMethod(MethodSpec.methodBuilder("getInstance")
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                                .addStatement("return $T.INSTANCE", clazzSingleton)
                                .returns(clazzService)
                                .build());

                System.out.println(">>>>>>> Generate:" + clazzService.toString());

                /*
                 *typeElement.getEnclosedElements():
                 * 返回该元素直接包含的子元素,通常对一个PackageElement而言，它可以包含TypeElement；
                 * 对于一个TypeElement而言，它可能包含属性VariableElement，方法ExecutableElement
                 *
                 * getEnclosingElement():
                 * 返回包含该element的父element，与上一个方法相反，
                 * VariableElement，方法ExecutableElement的父级是TypeElement,
                 * 而TypeElement的父级是PackageElement
                 *
                 */
                for (final ExecutableElement method : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    System.out.println(">>>>>> ExecutableElement method: " + method);
                    final String methodName = method.getSimpleName().toString();
                    final TypeMirror returnType = method.getReturnType();
                    final MethodSpec.Builder msb = MethodSpec.methodBuilder(methodName)
                            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                            .addAnnotation(Override.class)
                            .returns(TypeName.get(returnType));

                    for (final TypeMirror thrownType : method.getThrownTypes()) {
                        msb.addException(ClassName.get(thrownType));
                    }

                    final StringBuilder args = new StringBuilder();
                    final List<? extends VariableElement> parameterTypes = method.getParameters();
                    for (int i = 0, n = parameterTypes.size(); i < n; i++) {
                        final String argName = "arg" + i;
                        msb.addParameter(TypeName.get(parameterTypes.get(i).asType()), argName, Modifier.FINAL);
                        args.append(argName).append(i < n - 1 ? ", " : "");
                    }

                    switch (returnType.getKind()) {
                        case BOOLEAN:
                            msb.addStatement("return null != this.mDelegate && this.mDelegate.$L($L)", methodName, args);
                            break;
                        case BYTE:
                            msb.addStatement("return null != this.mDelegate ? this.mDelegate.$L($L) : (byte) 0", methodName, args);
                            break;
                        case SHORT:
                            msb.addStatement("return null != this.mDelegate ? this.mDelegate.$L($L) : (short) 0", methodName, args);
                            break;
                        case INT:
                        case FLOAT:
                        case LONG:
                        case DOUBLE:
                            msb.addStatement("return null != this.mDelegate ? this.mDelegate.$L($L) : 0", methodName, args);
                            break;
                        case CHAR:
                            msb.addStatement("return null != this.mDelegate ? this.mDelegate.$L($L) : '\0'", methodName, args);
                            break;
                        case VOID:
                            msb.beginControlFlow("if (null != this.mDelegate)")
                                    .addStatement("this.mDelegate.$L($L)", methodName, args)
                                    .endControlFlow();
                            break;
                        default:
                            msb.addStatement("return null != this.mDelegate ? this.mDelegate.$L($L) : null", methodName, args);
                            break;
                    }

                    tsb.addMethod(msb.build());
                }

                try {
                    JavaFile.builder(getPackageName(typeElement), tsb.build())
                            .indent("    ")
                            .addFileComment("\nAutomatically generated file. DO NOT MODIFY\n")
                            .skipJavaLangImports(true)
                            .build()
                            .writeTo(processingEnv.getFiler());
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return true;
    }

    private String getServiceName(final TypeElement typeElement) {
        final String simpleName = typeElement.getSimpleName().toString();
        if (simpleName.endsWith("ServiceProvider")) {
            return simpleName.substring(0, simpleName.length() - 8);
        }

        return simpleName + "Service";
    }

    private String getPackageName(TypeElement typeElement){
        return this.mElementUtils.getPackageOf(typeElement).getQualifiedName().toString();
    }

    private String getServiceLoaderPackageName(){
        String currentPackageName=getClass().getPackage().getName();
        StringBuilder builder=new StringBuilder(currentPackageName);
        if(currentPackageName!=null&&currentPackageName.lastIndexOf(".processor")!=-1){
            return builder.substring(0,currentPackageName.length()-9).concat("loader");
        }

        return currentPackageName;
    }
}
