package compozitor.engine.core.interfaces;

import compozitor.processor.core.interfaces.AnnotationRepository;
import compozitor.processor.core.interfaces.MethodModel;
import compozitor.processor.core.interfaces.ProcessingContext;
import compozitor.template.core.interfaces.TemplateContextData;

import java.util.ArrayList;
import java.util.Collection;

public interface MethodModelPlugin<T extends TemplateContextData<T>> extends CodeGenerationCategoryPlugin {
  default void accept(ProcessingContext context, AnnotationRepository annotationRepository){}

  default T accept(ProcessingContext context, MethodModel methodModel){
    return null;
  }

  default Collection<T> collect(ProcessingContext context, MethodModel methodModel){
    return new ArrayList<>();
  }

  default void release(ProcessingContext context){}
}
