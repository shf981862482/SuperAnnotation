package com.just.sdcardProcessor;

import com.google.auto.service.AutoService;
import com.just.SDCardRootFile;
import com.just.utils.ProcessorUtil;
import com.just.utils.StringUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;



/**
 * Created by zhai on 16/6/21.
 * SDUtils解析类
 */
@AutoService(Processor.class)
public class SDcardProcessor extends AbstractProcessor {

    private static final String CLASS_NAME = "SDCardUtil";

    private static final String ANNOTATION = "@" + SDCardRootFile.class.getSimpleName();

    //******Field**************************************************************
    private static final String SDCARD_ROOT_FILE_NAME = "mRootFileName";

    //******Method**************************************************************
    private static final String METHOD_INIT_FILE = "initFile";
    private static final String METHOD_GET_SDCARD = "getSDCard%sPath";
    private static final String METHOD_GET_SDCARD_PATH = "getSDCardPath";
    private static final String METHOD_IS_SDCARD_EXIST_REAL = "isSDCardExistReal";
    private static final String METHOD_CREATE_CACHE_FILE = "createCacheFile";
    private static final String METHOD_DELETE = "delete";
    private static final String METHOD_CLEARFILE = "clearFile";

    //******Class**************************************************************
    ClassName mEnvironmentClassName = ClassName.get("android.os", "Environment");
    ClassName mFileClassName = ClassName.get("java.io", "File");

    private Messager messager;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<String>();
        annotataions.add(SDCardRootFile.class.getCanonicalName());
        return annotataions;
    }


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        ArrayList<SDcardAnnotatedClass> annotatedClasses = new ArrayList<>();
        //SDCardRootFile

        for (Element element : roundEnv.getElementsAnnotatedWith(SDCardRootFile.class)) {
            //判断是否是public Field
            if (!ProcessorUtil.isFinalValidField(element, messager, ANNOTATION)) {
                return true;
            }
            VariableElement variableElement = (VariableElement) element;
            try {
                //解析
                annotatedClasses.add(buildAnnotVariabldSDcardClass(variableElement));
            } catch (IOException e) {
                String message = String.format("Couldn't processvariablass %s: .%s", variableElement,
                        e.getMessage());
                messager.printMessage(Diagnostic.Kind.ERROR, message, element);
            }
        }
        try {
            generate(annotatedClasses);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Couldn't generate class");
        }
        return true;
    }

    /**
     * package com.example;    // PackageElement
     * <p/>
     * public class Foo {        // TypeElement
     * <p/>
     * private int a;      // VariableElement
     * private Foo other;  // VariableElement
     * <p/>
     * public Foo () {}    // ExecuteableElement
     * <p/>
     * public void setA (  // ExecuteableElement
     * int newA   // TypeElement
     * ) {}
     * }
     *
     * @param annotatedClass
     * @return
     * @throws IOException
     */
    private SDcardAnnotatedClass buildAnnotVariabldSDcardClass(VariableElement annotatedClass)
            throws IOException {
        //        ArrayList<String> variableNames = new ArrayList<>();
        //        for (Element element : annotatedClass.getEnclosedElements()) {
        //            if (!(element instanceof VariableElement)) {
        //                continue;
        //            }
        //            VariableElement variableElement = (VariableElement) element;
        //            variableNames.add(variableElement.getSimpleName().toString());
        //        }
        return new SDcardAnnotatedClass(annotatedClass);
    }


    private void generate(List<SDcardAnnotatedClass> classList) throws IOException {
        if (null == classList || classList.size() == 0) {
            return;
        }
        for (int i = 0; i < classList.size(); i++) {
            String packageName = ProcessorUtil.getPackageName(processingEnv.getElementUtils(), classList.get(i).getQualifiedSuperClassName());
            TypeSpec generateClass = generateClass(classList.get(i));

            JavaFile javaFile = JavaFile.builder(packageName, generateClass).
                    build();

            javaFile.writeTo(processingEnv.getFiler());
        }
    }

    public TypeSpec generateClass(SDcardAnnotatedClass classes) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(classes.getAppRootPathName() + CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        builder.addField(makeCreateField(SDCARD_ROOT_FILE_NAME, classes.getAppRootPathName()));

        List<String> initfileMethodNames = new ArrayList<>();

        for (String fileName : classes.getFileNames()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "fileName=" + fileName);
            builder.addField(makeCreateField(fileName, fileName));

            MethodSpec methodSpec = makeFilePathMethod(fileName);
            builder.addMethod(methodSpec);
            initfileMethodNames.add(methodSpec.name);
        }

        builder.addStaticBlock(makeStaticb());
        builder.addMethod(makeCreateCacheFile(initfileMethodNames));
        builder.addMethod(makeIsDdcardExistRealMethod());
        builder.addMethod(makeGetSDCardPthMethod());
        builder.addMethod(maekInitFileMethod());
        builder.addMethod(makeDeleFileMethod());
        builder.addMethod(makeClearFileMethod());

        return builder.build();
    }

    /**
     * .initializer("$S + $L", "Lollipop v.", 5.0d)
     *
     * @param fieldName
     * @param value
     * @return
     */
    private FieldSpec makeCreateField(String fieldName, String value) {
        return FieldSpec.builder(String.class, fieldName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", value)
                .build();

    }

    private CodeBlock makeStaticb() {
        return CodeBlock.builder()
                .addStatement(METHOD_CREATE_CACHE_FILE + "()")
                .build();
    }

    private MethodSpec makeClearFileMethod() {
        return MethodSpec.methodBuilder(METHOD_CLEARFILE)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("String path=" + METHOD_GET_SDCARD_PATH + "()")
                .addStatement(METHOD_DELETE + "(new $T(path))", mFileClassName)
                .addStatement(METHOD_CREATE_CACHE_FILE + "()")
                .build();
    }

    private MethodSpec makeDeleFileMethod() {
        return MethodSpec.methodBuilder(METHOD_DELETE)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(mFileClassName, "file")
                .beginControlFlow("if(file.isFile())")
                .addStatement("file.delete();")
                .addStatement("return")
                .endControlFlow()
                .beginControlFlow("if(file.isDirectory())")
                .addStatement("File[] childFiles = file.listFiles()")
                .beginControlFlow("if (childFiles == null || childFiles.length == 0)")
                .addStatement("file.delete()")
                .addStatement("return")
                .endControlFlow()
                .beginControlFlow("for (int i = 0; i < childFiles.length; i++)")
                .addStatement(METHOD_DELETE + "(childFiles[i])")
                .endControlFlow()
                .addStatement("file.delete()")
                .endControlFlow()
                .build();
    }

    private MethodSpec makeCreateCacheFile(List<String> methodList) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(METHOD_CREATE_CACHE_FILE)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        for (String methodName : methodList) {
            builder.addStatement(METHOD_INIT_FILE + "(" + methodName + "())");
        }
        return builder.build();
    }

    private MethodSpec makeFilePathMethod(String fileName) {
        return MethodSpec.methodBuilder(String.format(METHOD_GET_SDCARD, StringUtils.capitalize(fileName)))
                .returns(String.class)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("String path = getSDCardPath() + File.separator + " + fileName)
                .addStatement("return path")
                .build();
    }


    private MethodSpec makeIsDdcardExistRealMethod() {
        return MethodSpec.methodBuilder(METHOD_IS_SDCARD_EXIST_REAL)
                .returns(boolean.class)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("boolean isExits = false")
                .addStatement("isExits = $T.getExternalStorageState().equals($T.MEDIA_MOUNTED)", mEnvironmentClassName, mEnvironmentClassName)
                .addStatement("return isExits")
                .build();
    }

    private MethodSpec makeGetSDCardPthMethod() {
        return MethodSpec.methodBuilder(METHOD_GET_SDCARD_PATH)
                .returns(String.class)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("String path = null")
                .beginControlFlow("if(isSDCardExistReal())")
                .addStatement("path = $T.getExternalStorageDirectory().toString() + $T.separator +"
                        + SDCARD_ROOT_FILE_NAME, mEnvironmentClassName, mFileClassName)
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("path = $T.getDataDirectory().toString() + $T.separator +"
                        + SDCARD_ROOT_FILE_NAME, mEnvironmentClassName, mFileClassName)
                .endControlFlow()
                .addStatement("return path")
                .build();
    }

    private MethodSpec maekInitFileMethod() {
        return MethodSpec.methodBuilder(METHOD_INIT_FILE)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(String.class, "path")
                .addStatement("$T file = new $T(path)", mFileClassName, mFileClassName)
                .beginControlFlow("if(file != null && !file.exists())")
                .addStatement("file.mkdirs();")
                .endControlFlow()
                .build();
    }


}