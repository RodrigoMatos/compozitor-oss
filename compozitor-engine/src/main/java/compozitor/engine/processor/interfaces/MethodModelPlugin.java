package compozitor.engine.processor.interfaces;

import compozitor.engine.core.interfaces.EngineCategory;
import compozitor.processor.core.interfaces.MethodModel;
import compozitor.template.core.interfaces.TemplateContextData;

public interface MethodModelPlugin<T extends TemplateContextData<T>> {
  <T extends TemplateContextData<T>> T accept(MethodModel fieldModel);

  EngineCategory engineType();
}