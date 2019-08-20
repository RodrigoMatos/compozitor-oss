package compozitor.processor.core.interfaces;

import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create", access = AccessLevel.PACKAGE)
public class JavaModel {
  @Getter
  private final ProcessingContext context;
  
  private final Map<String, TypeModel> typeCache = new HashMap<>();

  public AnnotationModel getAnnotation(AnnotationMirror annotation) {
    return new AnnotationModel(context, annotation);
  }

  public TypeModel getClass(Element element) {
    if (!element.getKind().equals(ElementKind.CLASS)) {
      return null;
    }

    return this.getType((TypeElement) element);
  }

  public TypeModel getClass(TypeMirror element) {
    return this.getType(element);
  }
  
  public TypeModel getInterface(Element element) {
    if (!element.getKind().equals(ElementKind.INTERFACE)) {
      return null;
    }

    return this.getType((TypeElement) element);
  }

  public TypeModel getInterface(TypeMirror mirror) {
    return this.getType(mirror);
  }

  public TypeParameterModel getType(TypeParameterElement typeParameter) {
    TypeModel typeModel = this.getType((TypeElement) typeParameter.getEnclosingElement());

    return new TypeParameterModel(context, typeParameter, typeModel);
  }

  public TypeModel getType(TypeMirror type) {
    if (type instanceof PrimitiveType) {
      this.context.info("Type is primitive");
      return this.getType(this.context.boxedClass((PrimitiveType) type));
    }

    if (type.getKind().equals(TypeKind.ARRAY)) {
      this.context.info("Type is an array");
      String name = type.toString().replace("[]", "");
      return this.getType(this.context.getTypeElement(name));
    }

    Element element = this.context.asElement(type);
    if (element instanceof TypeParameterElement) {
      this.context.info("Type has typed parameters");
      return this.getType((TypeParameterElement) element);
    }

    if (type.getKind().equals(TypeKind.VOID)) {
      element = this.context.getTypeElement("java.lang.Void");
    }

    return this.getType((TypeElement) element);
  }

  public TypeModel getType(TypeElement type) {
    String typeName = type.getQualifiedName().toString();
    
    TypeModel typeModel = this.typeCache.get(typeName);
    if(typeModel != null) {
      return typeModel;
    }
    
    this.context.info("Building TypeModel for element {0}", typeName);
    PackageModel packageModel = this.getPackage(type);

    Annotations annotations = new Annotations(type.getAnnotationMirrors(), this);

    Modifiers modifiers = new Modifiers(type.getModifiers());

    Interfaces interfaces = new Interfaces(type.getInterfaces(), this);
    
    Methods methods = new Methods(ElementFilter.methodsIn(type.getEnclosedElements()), this);

    Fields fields = new Fields(ElementFilter.fieldsIn(type.getEnclosedElements()), this);
    
    TypeModel superType = null;
    
    if(type.getKind().equals(ElementKind.CLASS) && !typeName.startsWith("java")) {
      superType = this.getType(type.getSuperclass());
    }
    
    SimpleTypeModel simpleType = new SimpleTypeModel(this.context, type, packageModel, annotations, modifiers, superType,
        interfaces, fields, methods);
    
    this.typeCache.put(typeName, simpleType);
    
    return simpleType;
  }

  public FieldModel getField(Element element) {
    if (!element.getKind().equals(ElementKind.FIELD)) {
      return null;
    }

    return this.getField((VariableElement) element);
  }

  public FieldModel getField(VariableElement element) {
    Annotations annotations = new Annotations(element.getAnnotationMirrors(), this);

    TypeModel type = this.getType(element.asType());

    Modifiers modifiers = new Modifiers(element.getModifiers());

    return new FieldModel(this.context, element, annotations, modifiers, type);
  }

  public MethodModel getMethod(Element element) {
    if (!element.getKind().equals(ElementKind.METHOD)) {
      return null;
    }

    return this.getMethod((ExecutableElement) element);
  }

  public MethodModel getMethod(ExecutableElement element) {
    Annotations annotations = new Annotations(element.getAnnotationMirrors(), this);

    Modifiers modifiers = new Modifiers(element.getModifiers());

    TypeModel returnType = this.getType(element.getReturnType());

    Arguments arguments = new Arguments(element.getParameters(), this);

    return new MethodModel(context, element, annotations, modifiers, returnType, arguments);
  }

  public ArgumentModel getArgument(VariableElement element) {
    Annotations annotations = new Annotations(element.getAnnotationMirrors(), this);

    TypeMirror argumentType = element.asType();
    TypeModel type = this.getType(argumentType);

    return new ArgumentModel(this.context, element, annotations, type,
        argumentType.getKind().equals(TypeKind.ARRAY));
  }
  
  private PackageModel getPackage(TypeElement type) {
    this.context.info("Building package for type {0}", type);
    return new PackageModel(this.context, this.context.getPackageOf(type));
  }
}
