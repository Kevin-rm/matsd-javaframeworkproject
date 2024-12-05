package mg.itu.prom16.validation;

import mg.itu.prom16.support.WebApplicationContainer;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.validation.base.ConstraintViolation;
import mg.matsd.javaframework.validation.base.ValidationErrors;

import java.util.*;

public class ModelBindingResult {
    public static final String MANAGED_INSTANCE_ID = "_matsd_model_binding_result";
    public static final String STORAGE_KEY = WebApplicationContainer.WEB_SCOPED_MANAGED_INSTANCES_KEY_PREFIX + MANAGED_INSTANCE_ID;

    private final List<GlobalError> globalErrors;
    private final Map<String, ValidationErrors<?>> validationErrorsMap;

    public ModelBindingResult() {
        globalErrors = new ArrayList<>();
        validationErrorsMap = new HashMap<>();
    }

    public List<GlobalError> getGlobalErrors() {
        return globalErrors;
    }

    public Map<String, ValidationErrors<?>> getValidationErrorsMap() {
        return validationErrorsMap;
    }

    public void addGlobalError(Throwable throwable) {
        Assert.notNull(throwable, "L'argument throwable ne peut pas être \"null\"");

        globalErrors.add(new GlobalError(throwable));
        globalErrors.sort(Comparator.comparing(GlobalError::getCreatedAt));
    }

    public int getGlobalErrorsCount() {
        return globalErrors.size();
    }

    @Nullable
    public GlobalError getFirstGlobalError() {
        return globalErrors.isEmpty() ? null : globalErrors.get(0);
    }

    @Nullable
    public GlobalError getLastGlobalError() {
        return globalErrors.isEmpty() ? null : globalErrors.get(globalErrors.size() - 1);
    }

    public void addValidationErrors(final String modelName, final ValidationErrors<?> validationErrors) {
        validateModelName(modelName);
        Assert.notNull(validationErrors, "Les erreurs de validation ne peuvent pas être \"null\"");

        validationErrorsMap.put(modelName, validationErrors);
    }

    public boolean hasAnyValidationErrors() {
        return !validationErrorsMap.isEmpty();
    }

    public boolean hasValidationErrors(final String modelName) {
        validateModelName(modelName);

        return validationErrorsMap.containsKey(modelName);
    }

    @Nullable
    public ValidationErrors<?> getValidationErrors(final String modelName) {
        validateModelName(modelName);

        return validationErrorsMap.get(modelName);
    }

    public boolean hasConstraintViolations(final String modelName, final String propertyPath) {
        validateModelName(modelName);
        Assert.notBlank(propertyPath, false, "Le chemin vers la propriété ne peut pas être vide ou \"null\"");

        ValidationErrors<?> validationErrors = validationErrorsMap.get(modelName);
        return validationErrors != null && validationErrors.hasConstraintViolations(propertyPath);
    }

    @Nullable
    public List<ConstraintViolation<?>> getConstraintViolations(final String modelName, final String propertyPath) {
        validateModelName(modelName);
        Assert.notBlank(propertyPath, false, "Le chemin vers la propriété ne peut pas être vide ou \"null\"");

        ValidationErrors<?> validationErrors = validationErrorsMap.get(modelName);
        return validationErrors == null ? null :
            Collections.unmodifiableList(validationErrors.getConstraintViolations(propertyPath));
    }

    public boolean hasGlobalErrors() {
        return !globalErrors.isEmpty();
    }

    public boolean hasErrors() {
        return hasGlobalErrors() || hasAnyValidationErrors();
    }

    private static void validateModelName(final String modelName) throws IllegalArgumentException {
        Assert.notBlank(modelName, false, "Le nom du modèle ne peut pas être vide ou \"null\"");
    }
}
