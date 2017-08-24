package personal.rowan.pacifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

@SupportedAnnotationTypes({"personal.rowan.pacifier.Extra", "personal.rowan.pacifier.Argument"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class PacifierProcessor extends AbstractProcessor {

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
        List<Pair<String, ClassName>> activityPackagesAndClasses = processActivityExtras(roundEnv);
        List<Pair<String, ClassName>> fragmentPackagesAndClasses = processFragmentArgs(roundEnv);
        createUniversalClass(activityPackagesAndClasses, fragmentPackagesAndClasses);
        return true;
    }

    private void createUniversalClass(
            List<Pair<String, ClassName>> activityPackagesAndClasses,
            List<Pair<String, ClassName>> fragmentPackagesAndClasses) {

        TypeSpec.Builder universalClassBuilder =
                TypeSpec.classBuilder("Pacifier").addModifiers(Modifier.PUBLIC);

        for (Pair<String, ClassName> activityPackageClassPair : activityPackagesAndClasses) {
            ClassName activityClass = activityPackageClassPair.second();
            ClassName factoryClass =
                    ClassName.get(activityPackageClassPair.first(), activityClass.simpleName() + "Extras");

            MethodSpec universalSetter =
                    MethodSpec.methodBuilder("bind")
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .returns(TypeName.VOID)
                            .addParameter(activityClass, "activity")
                            .addStatement("$T.bind(activity)", factoryClass)
                            .build();
            universalClassBuilder.addMethod(universalSetter);
        }

        for (Pair<String, ClassName> fragmentPackageClassPair : fragmentPackagesAndClasses) {
            ClassName fragmentClass = fragmentPackageClassPair.second();
            ClassName factoryClass =
                    ClassName.get(fragmentPackageClassPair.first(), fragmentClass.simpleName() + "Arguments");

            MethodSpec universalSetter =
                    MethodSpec.methodBuilder("bind")
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .returns(TypeName.VOID)
                            .addParameter(fragmentClass, "fragment")
                            .addStatement("$T.bind(fragment)", factoryClass)
                            .build();
            universalClassBuilder.addMethod(universalSetter);
        }

        JavaFile javaFile =
                JavaFile.builder("pacifier", universalClassBuilder.build()).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Pair<String, ClassName>> processActivityExtras(RoundEnvironment roundEnv) {
        Map<Element, List<Pair<Element, String>>> activityExtraMap = new HashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(Extra.class)) {
            Element activity = element.getEnclosingElement();
            Extra annotation = element.getAnnotation(Extra.class);
            for (String path : annotation.value()) {
                if (activityExtraMap.containsKey(activity)) {
                    activityExtraMap.get(activity).add(Pair.create(element, path));
                } else {
                    List<Pair<Element, String>> extraList = new ArrayList<>();
                    extraList.add(Pair.create(element, path));
                    activityExtraMap.put(activity, extraList);
                }
            }
        }
        List<Pair<String, ClassName>> activityPackagesAndClasses = new ArrayList<>();
        for (Element activity : activityExtraMap.keySet()) {
            String activityName = activity.getSimpleName().toString();
            String packageName = elements.getPackageOf(activity).getQualifiedName().toString();
            ClassName activityClass = ClassName.get(packageName, activityName);
            activityPackagesAndClasses.add(Pair.create(packageName, activityClass));

            List<Pair<Element, String>> extraPaths = activityExtraMap.get(activity);
            Map<String, List<Element>> pathExtraMap = new HashMap<>();
            pathExtraMap.put(Extra.DEFAULT_PATH, new ArrayList<Element>());
            List<Element> defaultExtras = new ArrayList<>();
            // Used to bind extras to members
            Set<Element> allExtras = new HashSet<>();
            for (Pair<Element, String> argPath : extraPaths) {
                String path = argPath.second();
                Element extra = argPath.first();
                if (Extra.DEFAULT_PATH.equals(path)) {
                    defaultExtras.add(extra);
                } else {
                    if (pathExtraMap.containsKey(path)) {
                        pathExtraMap.get(path).add(extra);
                    } else {
                        List<Element> extraList = new ArrayList<>();
                        extraList.add(extra);
                        pathExtraMap.put(path, extraList);
                    }
                }
                allExtras.add(extra);
            }
            for (String path : pathExtraMap.keySet()) {
                if (!pathExtraMap.containsKey(path)) {
                    pathExtraMap.put(path, new ArrayList<Element>());
                }
                List<Element> fullExtras = new ArrayList<>();
                fullExtras.addAll(defaultExtras);
                fullExtras.addAll(pathExtraMap.get(path));
                pathExtraMap.put(path, fullExtras);
            }

            List<MethodSpec> newIntentMethods = new ArrayList<>();
            for (String path : pathExtraMap.keySet()) {
                List<Element> extras = pathExtraMap.get(path);

                MethodSpec.Builder newIntentBuilder =
                        MethodSpec.methodBuilder("newIntent")
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .returns(classIntent)
                                .addParameter(classContext, "context")
                                .addStatement(
                                        "Intent intent = new $T($L, $L)",
                                        classIntent,
                                        "context",
                                        activityClass + ".class");

                for (Element extra : extras) {
                    String extraName = extra.getSimpleName().toString();
                    newIntentBuilder
                            .addParameter(ClassName.get(extra.asType()), extraName)
                            .addStatement("intent.putExtra($S, $L)", extraName, extraName);
                }

                MethodSpec newIntentMethod = newIntentBuilder.addStatement("return intent").build();
                newIntentMethods.add(newIntentMethod);
            }

            MethodSpec.Builder setterBuilder =
                    MethodSpec.methodBuilder("bind")
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .returns(TypeName.VOID)
                            .addParameter(activityClass, "activity")
                            .addStatement("$T extras = activity.getIntent().getExtras()", classBundle);

            for (Element extra : allExtras) {
                String extraName = extra.getSimpleName().toString();
                String getter = bundleModifier(extra, true);
                setterBuilder.addStatement(
                        "if (extras.containsKey($S)) activity.$L = "
                                + castString(extra, getter)
                                + "extras."
                                + getter
                                + "($S)",
                        extraName,
                        extraName,
                        extraName);
            }

            TypeSpec.Builder factoryClassBuilder =
                    TypeSpec.classBuilder(activityName + "Extras").addModifiers(Modifier.PUBLIC);

            for (MethodSpec newInstanceMethod : newIntentMethods) {
                factoryClassBuilder.addMethod(newInstanceMethod);
            }
            TypeSpec factoryClass = factoryClassBuilder.addMethod(setterBuilder.build()).build();

            JavaFile javaFile = JavaFile.builder(packageName, factoryClass).build();
            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return activityPackagesAndClasses;
    }

    private List<Pair<String, ClassName>> processFragmentArgs(RoundEnvironment roundEnv) {
        Map<Element, List<Pair<Element, String>>> fragmentArgMap = new HashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(Argument.class)) {
            Element fragment = element.getEnclosingElement();
            Argument annotation = element.getAnnotation(Argument.class);
            for (String path : annotation.value()) {
                if (fragmentArgMap.containsKey(fragment)) {
                    fragmentArgMap.get(fragment).add(Pair.create(element, path));
                } else {
                    List<Pair<Element, String>> argList = new ArrayList<>();
                    argList.add(Pair.create(element, path));
                    fragmentArgMap.put(fragment, argList);
                }
            }
        }

        List<Pair<String, ClassName>> fragmentPackagesAndClasses = new ArrayList<>();
        for (Element fragment : fragmentArgMap.keySet()) {
            String fragmentName = fragment.getSimpleName().toString();
            String packageName = elements.getPackageOf(fragment).getQualifiedName().toString();
            ClassName fragmentClass = ClassName.get(packageName, fragmentName);
            fragmentPackagesAndClasses.add(Pair.create(packageName, fragmentClass));

            List<Pair<Element, String>> argPaths = fragmentArgMap.get(fragment);
            Map<String, List<Element>> pathArgMap = new HashMap<>();
            pathArgMap.put(Argument.DEFAULT_PATH, new ArrayList<Element>());
            List<Element> defaultArgs = new ArrayList<>();
            // Used to bind arguments to members
            Set<Element> allArgs = new HashSet<>();
            for (Pair<Element, String> argPath : argPaths) {
                String path = argPath.second();
                Element arg = argPath.first();
                if (Argument.DEFAULT_PATH.equals(path)) {
                    defaultArgs.add(arg);
                } else {
                    if (pathArgMap.containsKey(path)) {
                        pathArgMap.get(path).add(arg);
                    } else {
                        List<Element> argList = new ArrayList<>();
                        argList.add(arg);
                        pathArgMap.put(path, argList);
                    }
                }
                allArgs.add(arg);
            }
            for (String path : pathArgMap.keySet()) {
                if (!pathArgMap.containsKey(path)) {
                    pathArgMap.put(path, new ArrayList<Element>());
                }
                List<Element> fullArgs = new ArrayList<>();
                fullArgs.addAll(defaultArgs);
                fullArgs.addAll(pathArgMap.get(path));
                pathArgMap.put(path, fullArgs);
            }

            List<MethodSpec> argsMethods = new ArrayList<>();
            List<MethodSpec> newInstanceMethods = new ArrayList<>();
            for (String path : pathArgMap.keySet()) {
                List<Element> args = pathArgMap.get(path);

                String argsMethodName = "args" + path;
                MethodSpec.Builder argsBuilder =
                        MethodSpec.methodBuilder(argsMethodName)
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .returns(classBundle)
                                .addStatement("Bundle args = new $T()", classBundle);

                for (Element arg : args) {
                    String argName = arg.getSimpleName().toString();
                    argsBuilder
                            .addParameter(ClassName.get(arg.asType()), argName)
                            .addStatement("args." + bundleModifier(arg, false) + "($S, $L)", argName, argName);
                }

                MethodSpec argsMethod = argsBuilder.addStatement("return args").build();
                argsMethods.add(argsMethod);

                String newInstanceMethodName = "newInstance" + path;
                MethodSpec.Builder newInstanceBuilder =
                        MethodSpec.methodBuilder(newInstanceMethodName)
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .returns(fragmentClass)
                                .addStatement("$T fragment = new $T()", fragmentClass, fragmentClass);

                String argsString = "";
                for (Element arg : args) {
                    String argName = arg.getSimpleName().toString();
                    newInstanceBuilder.addParameter(ClassName.get(arg.asType()), argName);
                    argsString += argName + ", ";
                }
                if (!argsString.isEmpty()) {
                    argsString = argsString.substring(0, argsString.length() - 2);
                }
                MethodSpec newInstanceMethod =
                        newInstanceBuilder
                                .addStatement("fragment.setArguments(" + argsMethodName + "(" + argsString + "))")
                                .addStatement("return fragment")
                                .build();
                newInstanceMethods.add(newInstanceMethod);
            }

            MethodSpec.Builder setterBuilder =
                    MethodSpec.methodBuilder("bind")
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .returns(TypeName.VOID)
                            .addParameter(fragmentClass, "fragment")
                            .addStatement("$T args = fragment.getArguments()", classBundle);

            for (Element arg : allArgs) {
                String argName = arg.getSimpleName().toString();
                String getter = bundleModifier(arg, true);
                setterBuilder.addStatement(
                        "if (args.containsKey($S)) fragment.$L = "
                                + castString(arg, getter)
                                + "args."
                                + getter
                                + "($S)",
                        argName,
                        argName,
                        argName);
            }

            TypeSpec.Builder factoryClassBuilder =
                    TypeSpec.classBuilder(fragmentName + "Arguments").addModifiers(Modifier.PUBLIC);

            for (MethodSpec argsMethod : argsMethods) {
                factoryClassBuilder.addMethod(argsMethod);
            }
            for (MethodSpec newInstanceMethod : newInstanceMethods) {
                factoryClassBuilder.addMethod(newInstanceMethod);
            }
            TypeSpec factoryClass = factoryClassBuilder.addMethod(setterBuilder.build()).build();

            JavaFile javaFile = JavaFile.builder(packageName, factoryClass).build();
            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return fragmentPackagesAndClasses;
    }

    private static String bundleModifier(Element element, boolean get) {
        String prefix = get ? "get" : "put";
        TypeName typeName = ClassName.get(element.asType());
        String typeString = typeName.toString();
        if (typeName.isPrimitive()) {
            return prefix + typeString.substring(0, 1).toUpperCase() + typeString.substring(1);
        } else if ("java.lang.String".equals(typeString)) {
            return prefix + "String";
        } else {
            // should there be a better base case for this?
            return prefix + "Serializable";
        }
    }

    private static String castString(Element element, String getter) {
        if ("getSerializable".equals(getter)) {
            return "(" + ClassName.get(element.asType()).toString() + ") ";
        } else {
            return "";
        }
    }

}
