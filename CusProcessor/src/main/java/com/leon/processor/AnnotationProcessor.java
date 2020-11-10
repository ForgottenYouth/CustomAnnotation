package com.leon.processor;

import com.leon.annotation.BindView;
import com.leon.annotation.ViewOnClick;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
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

@SupportedAnnotationTypes({"com.leon.annotation.BindView", "com.leon.annotation.ViewOnClick"})
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
        if (set != null && set.size() > 0) {
            //set中存放的是注解处理器支持并解析的注解类型
            for (TypeElement typeElement : set) {
                Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(typeElement);
                for (Element element : elements) {
                    //获取注解所属的类名
                    String className = element.getEnclosingElement().getSimpleName().toString();
                    String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();

                    //使用annotationTypeName , packageName ,ClassName 拼接为mapkey ,中间用-连接，方便split来区分
                    if (!mPackageClassSet.containsKey(typeElement.toString() + "-" + packageName + "-" + className)) {
                        List<Element> subelements = new ArrayList<>();
                        subelements.add(element);
                        mPackageClassSet.put(typeElement.toString() + "-" + packageName + "-" + className, subelements);
                    } else {
                        mPackageClassSet.get(typeElement.toString() + "-" + packageName + "-" + className).add(element);
                    }
                }
            }

            generateCodeByJavaPoet();
        }
        return false;
    }

    private void generateCodeByJavaPoet() {

        Iterator entries = mPackageClassSet.entrySet().iterator();
        List<MethodSpec> methods = new ArrayList<>();
        while (entries.hasNext()) {
            //取出hashmap的元素
            Map.Entry<String, ArrayList<Element>> entry = (Map.Entry<String, ArrayList<Element>>) entries.next();

            //解析key ,包名和类名
            String[] subKeys = entry.getKey().split("-");

            ClassName t = ClassName.bestGuess(subKeys[2]);
            if (subKeys[0].equals(BindView.class.getTypeName())) {
                //创建绑定view的方法
                MethodSpec.Builder bindMethodBuilder = MethodSpec.methodBuilder("BindViewById")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(t, "activity");
                for (Element element : entry.getValue()) {
                    //注解上的额值
                    int resId = element.getAnnotation(BindView.class).viewId();
                    //添加方法体
                    bindMethodBuilder.addStatement("activity.$L = activity.findViewById($L)", element.getSimpleName(), resId);
                }
                methods.add(bindMethodBuilder.build());
            }
            if (subKeys[0].equals(ViewOnClick.class.getTypeName())) {
                //创建绑定点击事件的方法
                MethodSpec.Builder onClickMethodSpecBuilder = MethodSpec.methodBuilder("BindViewOnClick")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(t, "activity", Modifier.FINAL)
                        .returns(void.class);

                for (Element element : entry.getValue()) {
                    int resId = element.getAnnotation(ViewOnClick.class).viewId();
                    String stateMent = String.format("activity.findViewById(%d).setOnClickListener(new android.view.View.OnClickListener() {" +
                            "\n @Override" +
                            "\n public void onClick(android.view.View v) {" +
                            "\n activity.onClick(%d);" +
                            "\n }" +
                            "\n })", resId, resId);
                    onClickMethodSpecBuilder.addStatement(stateMent);
                }
                methods.add(onClickMethodSpecBuilder.build());
            }

            if (!entries.hasNext()) {
                TypeSpec typeSpec = TypeSpec.classBuilder(String.format(subKeys[2] + "_customButterKnife"))//设置类名
                        .addMethods(methods)
                        .addModifiers(Modifier.PUBLIC).build();//添加修饰符

                //通过包名和TypeSpec（类）生成一个java文件
                JavaFile build = JavaFile.builder(subKeys[1], typeSpec).build();
                try {
                    //写入到filer中
                    build.writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}