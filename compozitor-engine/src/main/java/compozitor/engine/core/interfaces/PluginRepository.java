package compozitor.engine.core.interfaces;

import compozitor.generator.core.interfaces.CodeGenerationCategory;
import compozitor.generator.core.interfaces.TemplateRepository;
import compozitor.processor.core.interfaces.AnnotationRepository;
import compozitor.processor.core.interfaces.FieldModel;
import compozitor.processor.core.interfaces.Logger;
import compozitor.processor.core.interfaces.MethodModel;
import compozitor.processor.core.interfaces.ProcessingContext;
import compozitor.processor.core.interfaces.TypeModel;
import compozitor.template.core.interfaces.TemplateContextData;
import compozitor.template.core.interfaces.TemplateEngine;
import compozitor.template.core.interfaces.TemplateEngineBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;

class PluginRepository {
  private final Collection<TemplateEnginePlugin> templateEnginePlugins;
  private final Collection<TypeModelPlugin<?>> typeModelPlugins;
  private final Collection<FieldModelPlugin<?>> fieldModelPlugins;
  private final Collection<MethodModelPlugin<?>> methodModelPlugins;
  private final Collection<TemplatePlugin> templatePlugins;

  PluginRepository() {
    this.templateEnginePlugins = new ArrayList<>();
    this.typeModelPlugins = new ArrayList<>();
    this.fieldModelPlugins = new ArrayList<>();
    this.methodModelPlugins = new ArrayList<>();
    this.templatePlugins = new ArrayList<>();
  }

  static PluginRepository create() {
    return new PluginRepository();
  }

  void load(ClassLoader classLoader, CodeGenerationCategory category, Logger logger) {
    logger.info("Loading plugins for category {0}", category.getClass().getName());

    ServiceLoader.load(TemplateEnginePlugin.class, classLoader).forEach(plugin -> {
      this.accept(plugin, category, (accepted) -> {
        logger.info("Loading template engine plugin {0}", plugin.getClass().getCanonicalName());
        this.templateEnginePlugins.add(plugin);
      });
    });

    ServiceLoader.load(TypeModelPlugin.class, classLoader).forEach(plugin -> {
      this.accept(plugin, category, (accepted) -> {
        logger.info("Loading type model plugin {0}", plugin.getClass().getCanonicalName());
        this.typeModelPlugins.add(plugin);
      });
    });

    ServiceLoader.load(FieldModelPlugin.class, classLoader).forEach(plugin -> {
      this.accept(plugin, category, (accepted) -> {
        logger.info("Loading field model plugin {0}", plugin.getClass().getCanonicalName());
        this.fieldModelPlugins.add(plugin);
      });
    });

    ServiceLoader.load(MethodModelPlugin.class, classLoader).forEach(plugin -> {
      this.accept(plugin, category, (accepted) -> {
        logger.info("Loading method model plugin {0}", plugin.getClass().getCanonicalName());
        this.methodModelPlugins.add(plugin);
      });
    });

    ServiceLoader.load(TemplatePlugin.class, classLoader).forEach(plugin -> {
      this.accept(plugin, category, (accepted) -> {
        logger.info("Loading template plugin {0}", plugin.getClass().getCanonicalName());
        this.templatePlugins.add(plugin);
      });
    });
  }

  private void accept(CodeGenerationCategoryPlugin plugin, CodeGenerationCategory category, Consumer<CodeGenerationCategoryPlugin> accepted) {
    if (plugin.category().equals(category)) {
      accepted.accept(plugin);
    }
  }

  public TemplateEngine templateEngine() {
    TemplateEngineBuilder builder = TemplateEngineBuilder.create().addClassLoader(this.getClass().getClassLoader()).withClasspathTemplateLoader();

    this.templateEnginePlugins.forEach(plugin -> {
      plugin.accept(builder);
    });

    return builder.build();
  }

  public TemplateRepository templates(TemplateEngine templateEngine) {
    TemplateRepository templateRepository = TemplateRepository.create();
    this.templatePlugins.forEach(plugin -> {
      plugin.accept(templateEngine, templateRepository);
    });

    return templateRepository;
  }

  public <T extends TemplateContextData<T>> Collection<T> getMetaModel(ProcessingContext context, AnnotationRepository annotationRepository, TypeModel model) {
    List<T> metaModelList = new ArrayList<>();

    this.typeModelPlugins.forEach(plugin -> {
      plugin.accept(context, annotationRepository);

      metaModelList.addAll((Collection) plugin.collect(context, model));
      T accepted = (T) plugin.accept(context, model);
      if(accepted != null) {
        metaModelList.add(accepted);
      }
    });

    return metaModelList;
  }

  public <T extends TemplateContextData<T>> Collection<T> getMetaModel(ProcessingContext context, AnnotationRepository annotationRepository, FieldModel model) {
    List<T> metaModelList = new ArrayList<>();

    this.fieldModelPlugins.forEach(plugin -> {
      plugin.accept(context, annotationRepository);

      metaModelList.addAll((Collection) plugin.collect(context, model));
      T accepted = (T) plugin.accept(context, model);
      if(accepted != null) {
        metaModelList.add(accepted);
      }
    });

    return metaModelList;
  }

  public <T extends TemplateContextData<T>> Collection<T> getMetaModel(ProcessingContext context, AnnotationRepository annotationRepository, MethodModel model) {
    List<T> metaModelList = new ArrayList<>();

    this.methodModelPlugins.forEach(plugin -> {
      plugin.accept(context, annotationRepository);

      metaModelList.addAll((Collection) plugin.collect(context, model));
      T accepted = (T) plugin.accept(context, model);
      if(accepted != null) {
        metaModelList.add(accepted);
      }
    });

    return metaModelList;
  }

  public void releaseResources(ProcessingContext context){
    this.typeModelPlugins.forEach(plugin -> {
      plugin.release(context);
    });

    this.fieldModelPlugins.forEach(plugin -> {
      plugin.release(context);
    });

    this.methodModelPlugins.forEach(plugin -> {
      plugin.release(context);
    });
  }
}
