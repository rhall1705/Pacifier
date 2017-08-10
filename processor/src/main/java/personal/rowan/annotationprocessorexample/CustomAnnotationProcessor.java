package personal.rowan.annotationprocessorexample;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;

@SupportedAnnotationTypes("personal.rowan.annotationprocessorexample.Extra")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class CustomAnnotationProcessor extends AbstractProcessor {

    private static final ClassName classIntent = ClassName.get("android.content", "Intent");
    private static final ClassName classContext = ClassName.get("android.content", "Context");
    private static final ClassName classBundle = ClassName.get("android.os", "Bundle");

    private Filer filer;
    private Elements elements;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        elements = processingEnvironment.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Map<Element, List<Element>> activityExtraMap = new HashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(Extra.class)) {
            Element activity = element.getEnclosingElement();
            if (activityExtraMap.containsKey(activity)) {
                activityExtraMap.get(activity).add(element);
            } else {
                List<Element> extraList = new ArrayList<>();
                extraList.add(element);
                activityExtraMap.put(activity, extraList);
            }
        }
        for (Element activity : activityExtraMap.keySet()) {
            String activityName = activity.getSimpleName().toString();
            String packageName = elements.getPackageOf(activity).getQualifiedName().toString();
            ClassName activityClass = ClassName.get(packageName, activityName);

            List<Element> extras = activityExtraMap.get(activity);
            MethodSpec.Builder newIntentBuilder = MethodSpec.methodBuilder("new" + activityName + "Intent")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(classIntent)
                    .addParameter(classContext, "context")
                    .addStatement("Intent intent = new $T($L, $L)", classIntent, "context", activityClass + ".class");

            for (Element extra : extras) {
                String extraName = extra.getSimpleName().toString();
                newIntentBuilder.addParameter(ClassName.get(extra.asType()), extraName)
                        .addStatement("intent.putExtra($S, $L)", extraName, extraName);
            }

            MethodSpec newIntentMethod = newIntentBuilder.addStatement("return intent").build();

            MethodSpec.Builder setterBuilder = MethodSpec.methodBuilder("bind")
                    .addModifiers(Modifier.STATIC)
                    .returns(TypeName.VOID)
                    .addParameter(activityClass, "activity")
                    .addStatement("$T extras = activity.getIntent().getExtras()", classBundle);

            for (Element extra : extras) {
                String extraName = extra.getSimpleName().toString();
                setterBuilder.addStatement("activity.$L = extras." + bundleGetter(extra) + "($S)", extraName, extraName);
            }

            TypeSpec factoryClass = TypeSpec.classBuilder(activityName + "Extras")
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(newIntentMethod)
                    .addMethod(setterBuilder.build())
                    .build();

            JavaFile javaFile = JavaFile.builder(packageName, factoryClass).build();
            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private String bundleGetter(Element element) {
        TypeName typeName = ClassName.get(element.asType());
        String typeString = typeName.toString();
        if (typeName.isPrimitive()) {
            return "get" + typeString.substring(0, 1).toUpperCase() + typeString.substring(1);
        } else if ("java.lang.String".equals(typeString)) {
            return "getString";
        } else {
            // should there be a better base case for this?
            return "getSerializable";
        }

    }

}
