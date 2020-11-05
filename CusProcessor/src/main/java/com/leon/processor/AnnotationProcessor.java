package com.leon.processor;

import com.leon.annotation.BindView;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

@SupportedAnnotationTypes("com.leon.annotation.BindView")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AnnotationProcessor extends AbstractProcessor {

    private Filer filer;

    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        //取出所有使用Bindview的注解
        for (Element element : roundEnvironment.getElementsAnnotatedWith(BindView.class)) {
            generateCodeByJavaPoet(element);
        }
        return false;
    }


    private void generateCodeByJavaPoet(Element element) {

        //获取注解所属的类名
        String className = element.getEnclosingElement().getSimpleName().toString();

        String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();

        //注解上的额值
        int resId = element.getAnnotation(BindView.class).viewId();

        ClassName t = ClassName.bestGuess(className);

        //方法
        MethodSpec.Builder methodSpecBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(t, "activity");
        methodSpecBuilder.addStatement("activity.$L = activity.findViewById($L)", element.getSimpleName(), resId);
        MethodSpec methodSpec = methodSpecBuilder.build();

        //创建类
        TypeSpec typeSpec = TypeSpec.classBuilder(className+ "_ViewBinding")//设置类名
                .addModifiers(Modifier.PUBLIC)//添加修饰符
                .addMethod(methodSpec)//添加方法
                .build();

        //通过包名和TypeSpec（类）生成一个java文件
        JavaFile build = JavaFile.builder(packageName, typeSpec).build();
        try {
            //写入到filer中
            build.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}