package com.just.sdcardProcessor;



import com.just.annotations.SDCardRootFile;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;


/**
 * Created by yeungeek on 2016/4/27.
 */
class SDcardAnnotatedClass {
    public final VariableElement typeElement;

    private String qualifiedSuperClassName;//规范命名
    private String annotatedClassName; //简单命名
    private String simpleTypeName; //简单命名

    private String[] fileNames;//属性
    private String appRootPathName; //路径命名

    /**
     *
     * @param typeElement  代表一个字段,枚举常数,方法或者构造函数参数,局部变量,资源变量,或异常参数。
     */
    public SDcardAnnotatedClass(VariableElement typeElement) {
        //返回这个变量的值,如果这是最后一个字段初始化为一个编译时常量。
        this.appRootPathName = (String) typeElement.getConstantValue();
        //返回这个变量元素的简单的名称。
        this.annotatedClassName = typeElement.getSimpleName().toString();
        this.typeElement = typeElement;
        //返回这个构造指定类型的注释如果存在这样一个注释,其他null。
        SDCardRootFile annotation = typeElement.getAnnotation(SDCardRootFile.class);
        fileNames = annotation.fileNames();

        // Get the full QualifiedTypeName
        try {
            Class<?> clazz = annotation.annotationType();
            qualifiedSuperClassName = clazz.getCanonicalName();
            simpleTypeName = clazz.getSimpleName();
        } catch (MirroredTypeException mte) {
            DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
            TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
            qualifiedSuperClassName = classTypeElement.getQualifiedName().toString();
            simpleTypeName = classTypeElement.getSimpleName().toString();
        }
    }

    public String getQualifiedSuperClassName() {
        return qualifiedSuperClassName;
    }

    public String getSimpleTypeName() {
        return simpleTypeName;
    }

    public VariableElement getTypeElement() {
        return typeElement;
    }

    public String getAnnotatedClassName() {
        return annotatedClassName;
    }

    public String[] getFileNames() {
        return fileNames;
    }

    public String getAppRootPathName() {
        return appRootPathName;
    }
}
