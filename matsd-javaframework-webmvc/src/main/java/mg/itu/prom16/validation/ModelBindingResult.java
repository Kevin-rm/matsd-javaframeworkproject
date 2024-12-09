package mg.itu.prom16.validation;

import jakarta.servlet.http.HttpServletRequest;
import mg.itu.prom16.http.FlashBag;
import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.validation.base.ValidationErrors;

import java.util.*;

public class ModelBindingResult {
    public static final String MANAGED_INSTANCE_ID     = "_matsd_model_binding_result";
    public static final String FIELD_ERRORS_KEY_PREFIX = "mg.itu.prom16.validation.ModelBindingResult.";

    private final List<GlobalError> globalErrors;
    private final Map<String, List<FieldError>> fieldErrorsMap;
    private final Map<String, ValidationErrors<?>> validationErrorsMap;

    public ModelBindingResult() {
        globalErrors        = new ArrayList<>();
        fieldErrorsMap      = new HashMap<>();
        validationErrorsMap = new HashMap<>();
    }

    public List<GlobalError> getGlobalErrors() {
        return globalErrors;
    }

    public Map<String, List<FieldError>> getFieldErrorsMap() {
        return fieldErrorsMap;
    }

    public Map<String, ValidationErrors<?>> getValidationErrorsMap() {
        return validationErrorsMap;
    }

    public void addGlobalError(Throwable throwable) {
        Assert.notNull(throwable, "L'argument throwable ne peut pas être \"null\"");

        globalErrors.add(new GlobalError(throwable));
        globalErrors.sort(Comparator.comparing(GlobalError::getCreatedAt));
    }

    public boolean hasGlobalErrors() {
        return !globalErrors.isEmpty();
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

    public ModelBindingResult addValidationErrors(final String modelName, final ValidationErrors<?> validationErrors) {
        validateModelName(modelName);
        Assert.notNull(validationErrors, "Les erreurs de validation ne peuvent pas être \"null\"");

        validationErrorsMap.put(modelName, validationErrors);
        validationErrors.getConstraintViolationMap().forEach((property, constraintViolations) -> {
            fieldErrorsMap.put(modelName + "." + property, constraintViolations.stream()
                .map(FieldError::new).toList());
        });

        return this;
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

    public boolean hasErrors() {
        return hasGlobalErrors() || hasAnyValidationErrors();
    }

    public void addToRequestAttributes(HttpServletRequest httpServletRequest) {
        Assert.notNull(httpServletRequest, "La requête ne peut pas être \"null\"");

        fieldErrorsMap.forEach((propertyPath, fieldErrors) ->
            httpServletRequest.setAttribute(ModelBindingResult.FIELD_ERRORS_KEY_PREFIX + propertyPath, fieldErrors));
    }

    public void addToFlashes(FlashBag flashBag) {
        Assert.notNull(flashBag, "L'argument flashBag ne peut pas être \"null\"");

        fieldErrorsMap.forEach((propertyPath, fieldErrors) ->
            flashBag.set(ModelBindingResult.FIELD_ERRORS_KEY_PREFIX + propertyPath, fieldErrors));
    }

    private static void validateModelName(final String modelName) throws IllegalArgumentException {
        Assert.notBlank(modelName, false, "Le nom du modèle ne peut pas être vide ou \"null\"");
    }
}
