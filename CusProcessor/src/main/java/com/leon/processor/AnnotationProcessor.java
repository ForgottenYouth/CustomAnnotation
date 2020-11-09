package com.leon.processor;

import com.leon.annotation.BindView;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    private static final String TAG = AnnotationProcessor.class.getSimpleName();
    private Filer filer;
    private Elements elementUtils;

    private HashMap<String, List<Element>> mPackageClassSet = new HashMap<>();//存放包名+类名的集合

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //取出所有使用Bindview注解的元素
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        for (Element element : elements) {
            //获取注解所属的类名
            String className = element.getEnclosingElement().getSimpleName().toString();
            String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();

            if (!mPackageClassSet.containsKey(packageName + "-" + className)) {
                List<Element> subelements = new ArrayList<>();
                subelements.add(element);
                mPackageClassSet.put(packageName + "-" + className, subelements);
            } else {
                mPackageClassSet.get(packageName + "-" + className).add(element);
            }
        }

        generateCodeByJavaPoet(BindView.class);
        return false;
    }

    private <T extends Annotation> void generateCodeByJavaPoet(Class<T> annotationTypeClass) {

        Iterator entries = mPackageClassSet.entrySet().iterator();
        while (entries.hasNext()) {
            //取出hashmap的元素
            Map.Entry<String, ArrayList<Element>> entry = (Map.Entry<String, ArrayList<Element>>) entries.next();

            //解析key ,包名和类名
            String[] subKeys = entry.getKey().split("-");

            ClassName t = ClassName.bestGuess(subKeys[1]);

            //方法--修饰符、名称
            MethodSpec.Builder methodSpecBuilder = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(t, "activity");

            //方法体
            String typeName = annotationTypeClass.getTypeName();
            String disName = BindView.class.getTypeName();
            if (annotationTypeClass.equals(BindView.class)) {
                for (Element element : entry.getValue()) {
                    //注解上的额值
                    int resId = ((BindView)element.getAnnotation(annotationTypeClass)).viewId();
                    //添加方法体
                    methodSpecBuilder.addStatement("activity.$L = activity.findViewById($L)", element.getSimpleName(), resId);
                }
            }

            //创建类
            TypeSpec typeSpec = TypeSpec.classBuilder(subKeys[1] + "_ViewBinding")//设置类名
                    .addModifiers(Modifier.PUBLIC)//添加修饰符
                    .addMethod(methodSpecBuilder.build())
                    .build();

            //通过包名和TypeSpec（类）生成一个java文件
            JavaFile build = JavaFile.builder(subKeys[0], typeSpec).build();
            try {
                //写入到filer中
                build.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}