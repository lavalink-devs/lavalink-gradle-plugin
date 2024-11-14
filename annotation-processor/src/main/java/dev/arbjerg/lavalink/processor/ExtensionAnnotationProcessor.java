package dev.arbjerg.lavalink.processor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes({"org.pf4j.Extension"})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class ExtensionAnnotationProcessor extends AbstractProcessor {
    private List<CharSequence> metaAnnotations = new ArrayList<>(List.of("org.pf4j.Extension"));
    private List<CharSequence> extensions = new ArrayList<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var extension = processingEnv.getElementUtils().getTypeElement("org.pf4j.Extension");
        var metaAnnotations = roundEnv.getElementsAnnotatedWith(extension)
                .stream()
                .filter(element -> element.getKind() == ElementKind.ANNOTATION_TYPE)
                .map(element -> {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Found new extension meta class: " + element.getSimpleName());
                    return processingEnv.getElementUtils().getBinaryName((TypeElement) element);
                })
                .toList();

        this.metaAnnotations.addAll(metaAnnotations);

        var extensions = roundEnv.getRootElements()
                .stream()
                .filter(this::checkAnnotations)
                .map(element -> {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Found new extension: " + element.getSimpleName());
                    return processingEnv.getElementUtils().getBinaryName((TypeElement) element).toString();
                })
                .toList();
        this.extensions.addAll(extensions);

        if (roundEnv.processingOver()) {
            writeStorage();
            return false;
        }
        return false;
    }

    private boolean checkAnnotations(Element element) {
        return element.getAnnotationMirrors().stream()
                .map(annotation -> processingEnv.getElementUtils().getBinaryName((TypeElement) annotation.getAnnotationType().asElement()))
                .anyMatch(annotation -> metaAnnotations.contains(annotation.toString()));
    }

    private void writeStorage() {
        try (var resource = new BufferedWriter(processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/extensions.idx").openWriter())) {
            for (CharSequence className : extensions) {
                resource.write(className.toString());
                resource.newLine();
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
