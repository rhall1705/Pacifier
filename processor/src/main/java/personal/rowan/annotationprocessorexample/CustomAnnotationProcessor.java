package personal.rowan.annotationprocessorexample;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("personal.rowan.annotationprocessorexample.CustomAnnotation")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class CustomAnnotationProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Map<String, List<String>> activityExtraMap = new HashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(CustomAnnotation.class)) {
            String activityName = element.getEnclosingElement().getSimpleName().toString();
            String extraName = element.getSimpleName().toString();
            if (activityExtraMap.containsKey(activityName)) {
                activityExtraMap.get(activityName).add(extraName);
            } else {
                List<String> extraList = new ArrayList<>();
                extraList.add(extraName);
                activityExtraMap.put(activityName, extraList);
            }
        }
        for (String activityName : activityExtraMap.keySet()) {
            List<String> extraNames = activityExtraMap.get(activityName);
            MethodSpec.Builder builder = MethodSpec.methodBuilder("new" + activityName + "Intent")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

            for (String extraName : extraNames) {
                builder.addParameter(String.class, extraName);
            }

            TypeSpec factoryClass = TypeSpec.classBuilder(activityName + "IntentFactory")
                    .addModifiers(Modifier.PUBLIC)
                    .build();
        }


        /*StringBuilder builder = new StringBuilder()
                .append("package personal.rowan.annotationprocessorexample.generated;\n")
                .append("public class GeneratedClass {\n\n") // open class
                .append("public String getMessage() {\n") // open method
                .append("return \"");


        // for each javax.lang.model.element.Element annotated with the CustomAnnotation
        for (Element element : roundEnv.getElementsAnnotatedWith(CustomAnnotation.class)) {
            String objectType = element.getSimpleName().toString() + " : " + element.getEnclosingElement().getSimpleName().toString();


            // this is appending to the return statement
            builder.append(objectType).append(" says hello! ");
        }


        builder.append("\";\n") // end return
                .append("}\n") // close method
                .append("}"); // close class


        try { // write the file
            JavaFileObject source = processingEnv.getFiler().createSourceFile("personal.rowan.annotationprocessorexample.generated.GeneratedClass");


            Writer writer = source.openWriter();
            writer.write(builder.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            // Note: calling e.printStackTrace() will print IO errors
            // that occur from the file already existing after its first run, this is normal
        }*/


        return true;
    }
}
