package dev.arbjerg.lavalink.processor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes(LavalinkAnnotationProcessor.SPRING_CONFIGURATION)
public class LavalinkAnnotationProcessor extends AbstractProcessor {

    public static final String SPRING_CONFIGURATION = "org.springframework.context.annotation.Configuration";
    private TypeElement springConfigurationElement;
    private final List<CharSequence> configurationClasses = new ArrayList<>();

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        springConfigurationElement = processingEnv.getElementUtils().getTypeElement(SPRING_CONFIGURATION);
        super.init(processingEnv);
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            writeStorage();
            return false;
        }
        var newConfigurations = roundEnv.getElementsAnnotatedWith(springConfigurationElement)
                .stream()
                .filter(element -> element.getKind() == ElementKind.CLASS)
                .map(clazz -> processingEnv.getElementUtils().getBinaryName((TypeElement) clazz))
                .toList();
        processingEnv.getMessager().printMessage(Kind.NOTE, "Found the following new configurations: " + newConfigurations);
        configurationClasses.addAll(newConfigurations);
        return false;
    }

    private void writeStorage() {
        try (var resource = new BufferedWriter(processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/configurations.idx").openWriter())) {
            for (CharSequence className : configurationClasses) {
                resource.write(className.toString());
                resource.newLine();
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
