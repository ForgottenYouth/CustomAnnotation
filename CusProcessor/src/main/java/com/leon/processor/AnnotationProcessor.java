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

    private HashMap<String, List<Element>> mPackageClassSet = new HashMap<>();//存放解析的注解节点信息   注解名-包名-类名  作为key

    private HashMap<String, TypeSpec> mFilerTypeSpec = new HashMap<>();//存放生成对应文件的type info    包名-类名  作为key

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
                        /*
                        * TODO 相同注解的存在在一起，便于做方法体时使用
                        */
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
        while (entries.hasNext()) {
            //取出hashmap的元素
            Map.Entry<String, ArrayList<Element>> entry = (Map.Entry<String, ArrayList<Element>>) entries.next();

            //解析key ,包名和类名
            String[] subKeys = entry.getKey().split("-");

            ClassName t = ClassName.bestGuess(subKeys[2]);

            //用来存放创建的方法，因为如果同时存在多个注解，可以穿记得方法会比较多
            List<MethodSpec> methods = new ArrayList<>();
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
                    int[] resId = element.getAnnotation(ViewOnClick.class).viewId();
                    for (int i = 0; i < resId.length; i++) {
                        String stateMent = String.format("activity.findViewById(%d).setOnClickListener(new android.view.View.OnClickListener() {" +
                                "\n @Override" +
                                "\n public void onClick(android.view.View v) {" +
                                "\n activity.onClick(%d);" +
                                "\n }" +
                                "\n })", resId[i], resId[i]);
                        onClickMethodSpecBuilder.addStatement(stateMent);
                    }
                }
                methods.add(onClickMethodSpecBuilder.build());
            }


            /*
             * TODO 根据类名和包名进行分组存放，便于在不各自不同的文件中存放对应的代码
             */
            String filerTypeKey = entry.getKey().substring(entry.getKey().indexOf("-") + 1);

            if (mFilerTypeSpec.containsKey(filerTypeKey)) {
                TypeSpec typeSpec = mFilerTypeSpec.get(filerTypeKey).toBuilder().addMethods(methods).build();
                mFilerTypeSpec.put(filerTypeKey, typeSpec);
            } else {
                TypeSpec typeSpec = TypeSpec.classBuilder(String.format(subKeys[2] + "_customButterKnife"))//设置类名
                        .addModifiers(Modifier.PUBLIC).build();//添加修饰符
                typeSpec = typeSpec.toBuilder().addMethods(methods).build();
                mFilerTypeSpec.put(filerTypeKey, typeSpec);
            }

            if (!entries.hasNext()) {
                //通过包名-类名和TypeSpec（类）生成一个java文件
                Set<String> keySet = mFilerTypeSpec.keySet();
                for (String key : keySet) {
                    String[] split = key.split("-");
                    JavaFile build = JavaFile.builder(split[0], mFilerTypeSpec.get(key)).build();
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
}