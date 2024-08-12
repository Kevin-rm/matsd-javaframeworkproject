package mg.matsd.javaframework.core.managedinstances.factory;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.exceptions.InvalidPackageException;
import mg.matsd.javaframework.core.managedinstances.*;
import mg.matsd.javaframework.core.utils.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ManagedInstanceFactory {
    private static final String PACKAGE_NAME_REGEX = "^[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$";

    protected ManagedInstanceDefinitionRegistry managedInstanceDefinitionRegistry;
    @Nullable
    protected String componentScanBasePackage;
    protected Map<String, Object> singletonsMap;
    private   boolean componentScanPerformed = false;

    protected ManagedInstanceFactory() {
        managedInstanceDefinitionRegistry = new ManagedInstanceDefinitionRegistry(this);
        singletonsMap = new HashMap<>();
    }

    public ManagedInstanceFactory setComponentScanBasePackage(@Nullable String componentScanBasePackage) {
        Assert.notBlank(componentScanBasePackage, true,
            "Le nom de package des \"component\" à scanner ne peut pas être vide");
        Assert.state(isValidPackageName(componentScanBasePackage),
            () -> new InvalidPackageException(
                String.format("Le nom de package des \"component\" \"%s\" à scanner n'est pas valide", componentScanBasePackage)
            )
        );

        this.componentScanBasePackage = componentScanBasePackage.strip();
        return this;
    }

    public String[] getRegisteredManagedInstancesNames() {
        List<ManagedInstance> managedInstances = managedInstanceDefinitionRegistry.getManagedInstances();

        String[] names = new String[managedInstances.size()];
        for (int i = 0; i < names.length; i++)
            names[i] = managedInstances.get(i).getClazz().getName();

        return names;
    }

    public Object getManagedInstance(String id) throws NoSuchManagedInstanceException {
        validateId(id);

        return getManagedInstance(
            managedInstanceDefinitionRegistry.getManagedInstanceById(id)
        );
    }

    public Object getManagedInstance(Class<?> managedInstanceClass) throws NoSuchManagedInstanceException {
        Assert.notNull(managedInstanceClass, "La classe de la \"ManagedInstance\" ne peut pas être \"null\"");

        return getManagedInstance(
            managedInstanceDefinitionRegistry.getManagedInstanceByClass(managedInstanceClass)
        );
    }

    public Boolean containsManagedInstance(String id) {
        validateId(id);

        return managedInstanceDefinitionRegistry.containsManagedInstance(id);
    }

    public void registerManagedInstance(ManagedInstance managedInstance) {
        managedInstanceDefinitionRegistry.registerManagedInstance(managedInstance);
    }

    public void registerManagedInstance(String id, String clazz, String scope) {
        managedInstanceDefinitionRegistry.registerManagedInstance(id, clazz, scope);
    }

    public Boolean isSingleton(String id) throws NoSuchManagedInstanceException {
        validateId(id);

        return managedInstanceDefinitionRegistry.getManagedInstanceById(id).getScope() == Scope.SINGLETON;
    }

    public Boolean isPrototype(String id) throws NoSuchManagedInstanceException {
        validateId(id);

        return managedInstanceDefinitionRegistry.getManagedInstanceById(id).getScope() == Scope.PROTOTYPE;
    }

    public Class<?> getManagedInstanceClass(String id) throws NoSuchManagedInstanceException {
        validateId(id);

        return managedInstanceDefinitionRegistry.getManagedInstanceById(id).getClazz();
    }

    public boolean isCurrentlyInCreation(String id) {
        validateId(id);

        return managedInstanceDefinitionRegistry.isCurrentlyInCreation(id);
    }

    public void scanComponents() {
        if (componentScanBasePackage == null || componentScanPerformed) return;

        ManagedInstanceDefinitionScanner.doScanComponents(managedInstanceDefinitionRegistry, componentScanBasePackage);
        componentScanPerformed = true;
    }

    public boolean hasPerformedComponentScan() {
        return componentScanPerformed;
    }

    private Object getManagedInstance(ManagedInstance managedInstance) {
        String managedInstanceId = managedInstance.getId();

        if (isCurrentlyInCreation(managedInstanceId))
            throw new ManagedInstanceCurrentlyInCreationException(managedInstanceId);
        managedInstanceDefinitionRegistry.resolveDependencies(managedInstance);

        if (
            isSingleton(managedInstanceId) &&
            singletonsMap.containsKey(managedInstanceId)
        ) return singletonsMap.get(managedInstanceId);

        Object instance = ManagedInstanceUtils.instantiate(managedInstance, this);
        if (isSingleton(managedInstanceId))
            singletonsMap.put(managedInstanceId, instance);

        return instance;
    }

    private static void validateId(String id) {
        Assert.notNull(id, "L'identifiant ne peut pas être vide ou \"null\"");
    }

    private static boolean isValidPackageName(@Nullable String packageName) {
        return packageName != null && packageName.matches(PACKAGE_NAME_REGEX);
    }
}
