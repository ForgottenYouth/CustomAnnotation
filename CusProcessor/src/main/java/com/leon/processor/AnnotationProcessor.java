package com.leon.processor;

import com.leon.annotation.BindView;
import com.leon.annotation.ViewOnClick;
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
            for (TypeElement typeElement : set) {
                Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(typeElement);
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

            ClassName t = ClassName.bestGuess(subKeys[1]);
            List<MethodSpec> methods = new ArrayList<>();

            //方法体
            if (t.equals(BindView.class)) {
                //创建构造函数
                MethodSpec.Builder constructMethodBuilder = MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(t, "activity");
                for (Element element : entry.getValue()) {
                    //注解上的额值
                    int resId = element.getAnnotation(BindView.class).viewId();
                    //添加方法体
                    constructMethodBuilder.addStatement("activity.$L = activity.findViewById($L)", element.getSimpleName(), resId);
                }
                methods.add(constructMethodBuilder.build());
            }
            if (t.equals(ViewOnClick.class)) {
//                /*
//                 * TODO 创建点击事件绑定函数
//                 */
//
////                int resId = ((ViewOnClick) element.getAnnotation(annotationTypeClass)).viewId();
//
                MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(t.toString() + "_viewOnclick")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(t, "activity", Modifier.FINAL)
                        .addParameter(Integer.TYPE, "viewId", Modifier.FINAL)
                        .returns(void.class);
                String stateMent = String.format("activity.findViewById(%d).setOnClickListener(new View.OnClickListener() {" +
                        "\n @Override" +
                        "\n public void onClick(View v) {" +
                        "\n activity.onClick(%d);" +
                        "\n }" +
                        "\n })", 123, 456);
                methodSpecBuilder.addStatement(stateMent);
                methods.add(methodSpecBuilder.build());
            }

            TypeSpec typeSpec = TypeSpec.classBuilder(String.format(subKeys[1] + "_customButterKnife"))//设置类名
                    .addMethods(methods)
                    .addModifiers(Modifier.PUBLIC).build();//添加修饰符

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