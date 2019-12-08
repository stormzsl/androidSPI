package com.fasten.component.spi.processor;

import com.fasten.component.spi.annotations.ServiceProvider;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
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
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * 作者:created by storm on 2019-12-08
 */

public class GenerateMatchClassProcessor extends AbstractProcessor {

    private Filer mFiler;

    private Elements mElementsUtil;

    private Types mTypeUtil;

    private Messager mMessager;

    private static final String SEPARATE_SYMBOL = "$$";

    private static final String BINNDER_SUFFIEX = "binder";

    private static final String MODULE_NAME_KEY = "moduleName";

    private String mModuleName;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ServiceProvider.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mElementsUtil = processingEnvironment.getElementUtils();
        mModuleName = processingEnvironment.getOptions().get(MODULE_NAME_KEY);
        mTypeUtil = processingEnvironment.getTypeUtils();
        mMessager = processingEnvironment.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(ServiceProvider.class)) {
            if (element instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) element;
                ClassName generateClassNme = ClassName.get(getPackageName(typeElement), generateClassName(typeElement));
                TypeSpec.Builder tsb = TypeSpec.classBuilder(generateClassNme)
                        .addJavadoc("Represent the class $T of {@link $T}\n",generateClassNme,ClassName.get(typeElement));

                TypeMirror typeMirror = typeElement.getSuperclass();
                List<? extends TypeMirror> typeInterfaceMirrors = typeElement.getInterfaces();
                tsb.superclass(TypeName.get(typeMirror));
                for (int i = 0; i < typeInterfaceMirrors.size(); i++) {
                    TypeMirror interfaceMirror = typeInterfaceMirrors.get(i);
                    tsb.addSuperinterface(TypeName.get(interfaceMirror));
                }

                for (Element enclosedElement : typeElement.getEnclosedElements()) {
                    if (enclosedElement instanceof VariableElement) {
                        VariableElement variableElement = (VariableElement) enclosedElement;
                        TypeMirror variableTypeMirror = variableElement.asType();
                        printMessage(">>>>>> process variableElement:"+variableElement.getSimpleName().toString());
                        TypeName typeName = TypeName.get(variableTypeMirror);
                        String fieldName = variableElement.getSimpleName().toString();
                        Set<Modifier> modifierSet=variableElement.getModifiers();
                        Modifier[] modifiers=new Modifier[modifierSet.size()];
                        FieldSpec fieldSpec = FieldSpec.builder(typeName, fieldName, modifierSet.toArray(modifiers)).build();
                        tsb.addField(fieldSpec);
                    }

                    if (enclosedElement instanceof ExecutableElement&&ElementFilter.methodsIn(typeElement.getEnclosedElements()).contains(enclosedElement)) {
                        ExecutableElement executableElement = (ExecutableElement) enclosedElement;
                        TypeMirror executeTypeMirror = executableElement.getReturnType();
                        printMessage(">>>>>> process executableElement:"+executableElement.getSimpleName().toString());
                        final String methodName = executableElement.getSimpleName().toString();
                        final TypeMirror returnType = executableElement.getReturnType();
                        final MethodSpec.Builder msb = MethodSpec.methodBuilder(methodName)
                                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                                .addAnnotation(Override.class)
                                .returns(TypeName.get(returnType));

                        for (final TypeMirror thrownType : executableElement.getThrownTypes()) {
                            msb.addException(ClassName.get(thrownType));
                        }

                        final StringBuilder args = new StringBuilder();
                        final List<? extends VariableElement> parameterTypes = executableElement.getParameters();
                        for (int i = 0, n = parameterTypes.size(); i < n; i++) {
                            final String argName = "arg" + i;
                            msb.addParameter(TypeName.get(parameterTypes.get(i).asType()), argName, Modifier.FINAL);
                            args.append(argName).append(i < n - 1 ? ", " : "");
                        }

                        tsb.addMethod(msb.build());

                    }
                }

                try {
                    JavaFile.builder(getPackageName(typeElement), tsb.build())
                            .skipJavaLangImports(true)
                            .addFileComment("Generate file $T,DO NOT MODIFY\n", generateClassNme)
                            .build().writeTo(mFiler);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
        return false;
    }

    private String getPackageName(TypeElement typeElement) {
        return mElementsUtil.getPackageOf(typeElement).getQualifiedName().toString();
    }

    private String generateClassName(TypeElement typeElement) {
        String sourceName = typeElement.getSimpleName().toString();
        StringBuilder builder = new StringBuilder();
        if (mModuleName != null) {
            builder.append(mModuleName).append(SEPARATE_SYMBOL);
        }

        builder.append(sourceName).append(SEPARATE_SYMBOL)
                .append(BINNDER_SUFFIEX).append("match");
        return builder.toString();
    }

    private void printMessage(String msg) {
        if (mMessager != null) {
            mMessager.printMessage(Diagnostic.Kind.WARNING, msg);
        }
    }
}
