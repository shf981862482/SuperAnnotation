# SuperAnnotation
annotation
http://blog.csdn.net/qq_28195645/article/details/52097626

在app开发中，难免要做一些sdcard的操作
比如：判断sdcard存在，生成相应目录， 删除文件等；

我们可以通过注解来自动生成如下的文件

外层build.gradle
```
    dependencies {
        classpath 'com.android.tools.build:gradle:2.1.2'
        classpath 'com.frogermcs.androiddevmetrics:androiddevmetrics-plugin:0.4'
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
```

app build.radle
```
apply plugin: 'com.android.application'
//注解处理器
apply plugin: 'com.neenbedankt.android-apt'

dependencies {
    compile fileTree(dir: 'libs', include: ['**.*'])
    testCompile 'junit:junit:4.12'
    compile 'com.android.support:appcompat-v7:23.4.0'
    //注解处理器
    compile 'com.google.auto.service:auto-service:1.0-rc2'
    compile 'com.squareup:javapoet:1.7.0'
//    apt "com.mmwork:compiler:0.3.0"
//    compile 'com.mmwork:annotaion:0.2.1'
    apt project(':processor')
    compile project(':annotation')
}
```

#SDCardUtil

```
public final class SuperSDCardUtil {
  public static final String mRootFileName = "Super";

  public static final String img = "img";

  public static final String data = "data";

  public static final String temp = "temp";

  public static final String msg = "msg";

  public static final String sound = "sound";

  public static final String soundLocal = "soundLocal";

  public static final String download = "download";

  static {
    createCacheFile();
  }

  public static String getSDCardImgPath() {
    String path = getSDCardPath() + File.separator + img;
    return path;
  }

  public static String getSDCardDataPath() {
    String path = getSDCardPath() + File.separator + data;
    return path;
  }

  public static String getSDCardTempPath() {
    String path = getSDCardPath() + File.separator + temp;
    return path;
  }

  public static String getSDCardMsgPath() {
    String path = getSDCardPath() + File.separator + msg;
    return path;
  }

  public static String getSDCardSoundPath() {
    String path = getSDCardPath() + File.separator + sound;
    return path;
  }

  public static String getSDCardSoundLocalPath() {
    String path = getSDCardPath() + File.separator + soundLocal;
    return path;
  }

  public static String getSDCardDownloadPath() {
    String path = getSDCardPath() + File.separator + download;
    return path;
  }

  public static void createCacheFile() {
    initFile(getSDCardImgPath());
    initFile(getSDCardDataPath());
    initFile(getSDCardTempPath());
    initFile(getSDCardMsgPath());
    initFile(getSDCardSoundPath());
    initFile(getSDCardSoundLocalPath());
    initFile(getSDCardDownloadPath());
  }

  public static boolean isSDCardExistReal() {
    boolean isExits = false;
    isExits = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    return isExits;
  }

  public static String getSDCardPath() {
    String path = null;
    if(isSDCardExistReal()) {
      path = Environment.getExternalStorageDirectory().toString() + File.separator +mRootFileName;
    }
    else {
      path = Environment.getDataDirectory().toString() + File.separator +mRootFileName;
    }
    return path;
  }

  public static void initFile(String path) {
    File file = new File(path);
    if(file != null && !file.exists()) {
      file.mkdirs();;
    }
  }

  public static void delete(File file) {
    if(file.isFile()) {
      file.delete();;
      return;
    }
    if(file.isDirectory()) {
      File[] childFiles = file.listFiles();
      if (childFiles == null || childFiles.length == 0) {
        file.delete();
        return;
      }
      for (int i = 0; i < childFiles.length; i++) {
        delete(childFiles[i]);
      }
      file.delete();
    }
  }

  public static void clearFile() {
    String path=getSDCardPath();
    delete(new File(path));
    createCacheFile();
  }
}
```

那么可见以上的方法足够用了、但是如果我们切换到了别的项目，如果产品说图片文件目录需要是imgsbchanpin，难道要复制一份java文件，改一下常量所对应的目录名称吗？
no,no,no 作为懒人我们要想办法搞一套自动化的东西

#自动成代码
我们会想起一些android框架、比如dagger2,databinding等
dagger2的原理就是是在编译时根据annotation来生成class文件

##配置好环境
在根目录下的build.gradle添加apt

```
buildscript {
    dependencies {
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    }
}
```

app下的build.gradle添加注解处理器插件 javapoet等

```
//注解处理器
apply plugin: 'com.neenbedankt.android-apt'
dependencies {

    //注解处理器
    compile 'com.google.auto.service:auto-service:1.0-rc2'
    compile 'com.squareup:javapoet:1.7.0'

}
```

##自定义注解
我新建了一个java的module
![这里写图片描述](http://img.blog.csdn.net/20160802202756382)
因为annotation和javapoet暂时支持jdk1.7，如果你用的1.8需要在生成的java module中的
build.gradle添加如下代码

```
apply plugin: 'java'
sourceCompatibility = JavaVersion.VERSION_1_7
targetCompatibility = JavaVersion.VERSION_1_7
dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
}
```

然后创建自己annotation

```
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface SDCardRootFile {
    //filenames
    String[] fileNames() default {};
}

fileNames是你要生成的目录名字  img sound 等
```
[深入理解自定义annotation](http://www.cnblogs.com/peida/archive/2013/04/24/3036689.html)
> @Target说明了Annotation所修饰的对象范围：Annotation可被用于 packages、types（类、接口、枚举、Annotation类型）、类型成员（方法、构造方法、成员变量、枚举值）、方法参数和本地变量（如循环变量、catch参数）。在Annotation类型的声明中使用了target可更加明晰其修饰的目标。
> 
> @Retention定义了该Annotation被保留的时间长短：某些Annotation仅出现在源代码中，而被编译器丢弃；而另一些却被编译在class文件中；编译在class文件中的Annotation可能会被虚拟机忽略，而另一些在class被装载时将被读取（请注意并不影响class的执行，因为Annotation与class在使用上是被分离的）。使用这个meta-Annotation可以对 Annotation的“生命周期”限制。


##AbstractProcessor
###AbstractProcessor来通过自定义的annotation来生成代码
```
public class SDcardProcessor extends AbstractProcessor {  
  
    @Override  
    public synchronized void init(ProcessingEnvironment env){ }  
  
    @Override  
    public boolean process(Set<? extends TypeElement> annoations, RoundEnvironment env) { }  
  
    @Override  
    public Set<String> getSupportedAnnotationTypes() { }  
  
    @Override  
    public SourceVersion getSupportedSourceVersion() { }  
  
}  
```

> init(ProcessingEnvironment env):
> 每一个注解处理器类都必须有一个空的构造函数。然而，这里有一个特殊的init()方法，它会被注解处理工具调用，并输入ProcessingEnviroment参数。ProcessingEnviroment提供很多有用的工具类Elements,Types和Filer。
> 
> ``` public boolean process(Set<? extends TypeElement> annoations,
> RoundEnvironment env) ```
> 
> 这相当于每个处理器的主函数main()。
> 在这里写扫描、评估和处理注解的代码，以及生成Java文件。输入参数RoundEnviroment，可以让查询出包含特定注解的被注解元素。
> 
> getSupportedAnnotationTypes():
> 这里必须指定，这个注解处理器是注册给哪个注解的。注意，它的返回值是一个字符串的集合，包含本处理器想要处理的注解类型的合法全称。换句话说，在这里定义你的注解处理器注册到哪些注解上。
> getSupportedSourceVersion():
> 用来指定你使用的Java版本。通常这里返回SourceVersion.latestSupported()。然而，如果有足够的理由只支持Java
> 6的话，也可以返回SourceVersion.RELEASE_6。推荐使用前者。

再new一个java的module，并将自定义annotation的module引入
build.gradle如下

```
apply plugin: 'java'
sourceCompatibility = JavaVersion.VERSION_1_7
targetCompatibility = JavaVersion.VERSION_1_7
dependencies {
//    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.google.auto.service:auto-service:1.0-rc2'
    compile 'com.squareup:javapoet:1.7.0'
    compile project(':annotation')
}
```

###新建几个类
SDcardProcessor  处理器
SDcardAnnotatedClass 用来存放每个注解的实体类
ProcessorUtil  处理器工具类，获取包名等
StringUtils  实现驼峰明面
代码分别如下

```
package com.just.sdcardProcessor;

import com.google.auto.service.AutoService;
import com.just.annotations.SDCardRootFile;
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
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

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
    public SourceVersion getSupportedSourceVersion() {
//        super.getSupportedSourceVersion();
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
    }

    /**
     *
     * @param annotations
     * @param roundEnv  注释处理工具框架将提供一个注解处理器实现了这个接口的对象,以便处理器可以查询信息的注释处理
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        ArrayList<SDcardAnnotatedClass> annotatedClasses = new ArrayList<>();
        //SDCardRootFile
//        roundEnv.getElementsAnnotatedWith(SDCardRootFile.class)  // 获得被该注解声明的元素
        for (Element element : roundEnv.getElementsAnnotatedWith(SDCardRootFile.class)) {
            //if public Field
            if (!ProcessorUtil.isFinalValidField(element, messager, ANNOTATION)) {
                return true;
            }
            VariableElement variableElement = (VariableElement) element;
            try {
                //paser
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

```

```
package com.just.sdcardProcessor;

import com.just.annotations.SDCardRootFile;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

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

```

```
package com.just.utils;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

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

```

```
package com.just.utils;

/**
 * Created by zhai on 16/6/22.
 */
public class StringUtils {
    public StringUtils() {
    }

    public static String decapitalize(String name) {
        if(name != null && name.length() != 0) {
            if(name.length() > 1 && Character.isUpperCase(name.charAt(1)) && Character.isUpperCase(name.charAt(0))) {
                return name;
            } else {
                char[] chars = name.toCharArray();
                chars[0] = Character.toLowerCase(chars[0]);
                return new String(chars);
            }
        } else {
            return name;
        }
    }

    public static String capitalize(String name) {
        if(name != null && name.length() != 0) {
            char[] chars = name.toCharArray();
            chars[0] = Character.toUpperCase(chars[0]);
            return new String(chars);
        } else {
            return name;
        }
    }
}
```

##app下的build.gradle以来这两个module

```
dependencies {
    apt project(':processor')
    compile project(':annotation')
}
```
如果有jdk版本问题需要加上如下代码

```
compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_7
        targetCompatibility JavaVersion.VERSION_1_7
}
```

###新建一个class类，用来注解

```
/**
 * Created by walkingMen on 2016/8/1.
 */
public class Constant implements Serializable {
    //=================SD文件路径===========================================
    public static final String DISK_IMAGE_PHOTO_PATH = "img";

    public static final String DISK_DATA = "data";

    public static final String DISK_TAKE_PHOTO_PATH = "temp";

    public static final String DISK_MSG_CACHE_PATH = "msg";

    public static final String DISK_SOUND_PATH = "sound";

    public static final String DISK_MSG_RECORD_VOICE_PATH = "soundLocal";

    public static final String DISK_DOWNLOAD_PATH = "download";


    //缓存路径
    @SDCardRootFile(fileNames = {DISK_IMAGE_PHOTO_PATH, DISK_DATA, DISK_TAKE_PHOTO_PATH,
            DISK_MSG_CACHE_PATH, DISK_SOUND_PATH, DISK_MSG_RECORD_VOICE_PATH, DISK_DOWNLOAD_PATH})
    public static final String CACHE_ROOT_DIR_NAME = "Super";
}
```

好了，大功告成，clean project rebuild project
![这里写图片描述](http://img.blog.csdn.net/20160802205047762)

略微等待一下，会在app/build/generated/source/apt下生成相应的***SDCardUtil
![这里写图片描述](http://img.blog.csdn.net/20160802205237419)


##jcenter gradle地址
ok、开发完成，我已经将annotation和processor提交到了jcenter,地址如下

```
compile 'com.sun_multi:annotaion:0.0.3'
apt'com.sun_multi:compiler:0.0.2'
```

##demo github地址
https://github.com/shf981862482/SuperAnnotation
欢迎star和fork







#插曲
开发的时候，突然出现编译没自动生成Sdcardutils的情况，经过两天的死磕终于解决了

下面是错误日志

```
FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:compileTrunkDebugJavaWithJavac'.
> Compilation failed; see the compiler error output for details.

* Try:
Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output.
```


后来在我新建的项目中专门测试这个bug 报了如下错误

```
错误: 服务配置文件不正确, 或构造处理程序对象

javax.annotation.processing.Processor: Provider com.mm.processor.sdcardProcessor.SDcardProcessor could not be instantiated: java.lang.VerifyError: Expecting a stackmap frame at branch target 24
  Exception Details:
    Location:
      com/squareup/javapoet/TypeName.<init>(Ljava/util/List;)V @12: ifeq
    Reason:
      Expected stackmap frame at this location.
    Bytecode:
      0x0000000: 2a01 2bb7 0003 b201 bbb6 01be 9900 0cb2
      0x0000010: 01c3 1301 c5b6 01ca b201 bbb6 01be 9900
      0x0000020: 0cb2 01c3 1301 c5b6 01ca b1            
  时抛出异常错误
Error:Execution failed for task ':app:compileDebugJavaWithJavac'.
> Compilation failed; see the compiler error output for details.
```

```
gradlew compileDebug --stacktrace
```

我推测错误是因为jdk版本导致的
通过命令

```
gradlew compileDebug --stacktrace
```

获取编译的详情日志如下

```
* What went wrong:
Unable to start the daemon process.
This problem might be caused by incorrect configuration of the daemon.
For example, an unrecognized jvm option is used.
Please refer to the user guide chapter on the daemon at https://docs.gradle.org/2.10/userguide/gradle_daemon.html
Please read the following process output to find out more:
-----------------------
FATAL ERROR in native method: JDWP No transports initialized, jvmtiError=AGENT_ERROR_TRANSPORT_INIT(197)
ERROR: transport error 202: bind failed: Address already in use
ERROR: JDWP Transport dt_socket failed to initialize, TRANSPORT_INIT(510)
JDWP exit error AGENT_ERROR_TRANSPORT_INIT(197): No transports initialized [debugInit.c:750]

* Exception is:
org.gradle.api.GradleException: Unable to start the daemon process.
This problem might be caused by incorrect configuration of the daemon.
For example, an unrecognized jvm option is used.
Please refer to the user guide chapter on the daemon at https://docs.gradle.org/2.10/userguide/gradle_daemon.html
Please read the following process output to find out more:
-----------------------
```

然后我去stackoverflow搜Unable to start the daemon process.
http://stackoverflow.com/questions/20471311/android-studio-unable-to-start-the-daemon-process

通过删除了user下的.gradle 重新编译，终于成功了
猜测可能是gradle的缓存在作怪
