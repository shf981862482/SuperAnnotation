package com.just.utils;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * Created by zhai on 16/6/22.
 */
public class ProcessorUtil {

    public static String getPackageName(Elements elementUtils, String qualifiedSuperClassName ) {
        TypeElement superClassname = elementUtils.getTypeElement(qualifiedSuperClassName);
        PackageElement pkg = elementUtils.getPackageOf(superClassname);
        if (pkg.isUnnamed()) {
            return null;
        }
        return pkg.getQualifiedName().toString();
    }

    public static boolean isValidClass(TypeElement element, Messager messager, String annotation) {
        if (!isPublic(element)) {
            String message = String.format("Classes annotated with %s must be public.",
                    annotation);
            messager.printMessage(Diagnostic.Kind.ERROR, message, element);
            return false;
        }

        if (isAbstract(element)) {
            String message = String.format("Classes annotated with %s must not be abstract.",
                    annotation);
            messager.printMessage(Diagnostic.Kind.ERROR, message, element);
            return false;
        }
        return true;
    }

    public static boolean isFinalValidField(Element element, Messager messager, String annotation) {
        if (!isPublic(element)) {
            String message = String.format("Classes annotated with %s must be public.",
                    annotation);
            messager.printMessage(Diagnostic.Kind.ERROR, message, element);
            return false;
        }
        if (!isField(element)) {
            String message = String.format("must be file.",
                    annotation);
            messager.printMessage(Diagnostic.Kind.ERROR, message, element);
            return false;
        }
        if (!isFinal(element)) {
            String message = String.format("must be final.",
                    annotation);
            messager.printMessage(Diagnostic.Kind.ERROR, message, element);
            return false;
        }
        return true;
    }

    public static boolean isField(Element annotatedClass) {
        return annotatedClass.getKind() == ElementKind.FIELD;
    }

    public static boolean isFinal(Element annotatedClass) {
        return annotatedClass.getModifiers().contains(Modifier.FINAL);
    }

    public static boolean isPublic(Element annotatedClass) {
        return annotatedClass.getModifiers().contains(Modifier.PUBLIC);
    }

    public static boolean isAbstract(Element annotatedClass) {
        return annotatedClass.getModifiers().contains(Modifier.ABSTRACT);
    }
}
