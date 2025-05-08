package mg.matsd.javaframework.di.managedinstances.factory;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;
import mg.matsd.javaframework.core.utils.StringUtils;
import mg.matsd.javaframework.di.exceptions.InvalidPackageException;
import mg.matsd.javaframework.di.exceptions.ManagedInstanceCurrentlyInCreationException;
import mg.matsd.javaframework.di.exceptions.NoSuchManagedInstanceException;
import mg.matsd.javaframework.di.managedinstances.ManagedInstance;
import mg.matsd.javaframework.di.managedinstances.ManagedInstanceUtils;
import mg.matsd.javaframework.di.managedinstances.Scope;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ManagedInstanceFactory {
    protected ManagedInstanceDefinitionRegistry managedInstanceDefinitionRegistry;
    @Nullable
    protected String componentScanBasePackage;
    protected Map<String, Object> singletonsMap;
    private   boolean componentScanPerformed = false;

    protected ManagedInstanceFactory() {
        managedInstanceDefinitionRegistry = new ManagedInstanceDefinitionRegistry(this);
        singletonsMap = new HashMap<>();

        defineCustomConfiguration();
    }

    public ManagedInstanceFactory setComponentScanBasePackage(String componentScanBasePackage) {
        Assert.notBlank(componentScanBasePackage, false,
            "Le nom de package des \"component\" à scanner ne peut pas être vide ou \"null\"");
        Assert.isTrue(StringUtils.isValidPackageName(componentScanBasePackage),
            () -> new InvalidPackageException(String.format("Le nom de package des components \"%s\" " +
                "à scanner n'est pas valide", componentScanBasePackage), componentScanBasePackage
            ));

        this.componentScanBasePackage = componentScanBasePackage.strip();
        return this;
    }

    public String[] getRegisteredManagedInstancesNames() {
        List<ManagedInstance> managedInstances = managedInstanceDefinitionRegistry.getManagedInstances();

        String[] names = new String[managedInstances.size()];
        Arrays.setAll(names, i -> managedInstances.get(i).getClazz().getName());

        return names;
    }

    public Object getManagedInstance(String id) throws NoSuchManagedInstanceException {
        validateId(id);

        return getManagedInstance(managedInstanceDefinitionRegistry.getManagedInstanceById(id));
    }

    @SuppressWarnings("unchecked")
    public <T> T getManagedInstance(Class<T> managedInstanceClass) throws NoSuchManagedInstanceException {
        Assert.notNull(managedInstanceClass, "La classe de la \"ManagedInstance\" ne peut pas être \"null\"");

        return (T) getManagedInstance(managedInstanceDefinitionRegistry.getManagedInstanceByClass(managedInstanceClass));
    }

    public boolean containsManagedInstance(String id) {
        validateId(id);
        return managedInstanceDefinitionRegistry.containsManagedInstance(id);
    }

    public boolean containsManagedInstance(Class<?> managedInstanceClass) {
        Assert.notNull(managedInstanceClass, "La classe de la \"ManagedInstance\" ne peut pas être \"null\"");
        return managedInstanceDefinitionRegistry.containsManagedInstance(managedInstanceClass);
    }

    public void registerManagedInstance(ManagedInstance... managedInstances) {
        Assert.notEmpty(managedInstances, "Le tableau de \"ManagedInstance\" ne peut pas être vide ou \"null\"");
        Assert.noNullElements(managedInstances, "Chaque \"ManagedInstance\" à enregistrer ne peut pas être \"null\"");

        Arrays.stream(managedInstances).forEachOrdered(m -> managedInstanceDefinitionRegistry.registerManagedInstance(m));
    }

    public void registerManagedInstance(
        @Nullable String id,
        Class<?> clazz,
        @Nullable Scope scope,
        @Nullable Boolean isLazy,
        String parentId,
        String factoryMethodName
    ) {
        Assert.notBlank(parentId, false, "L'identifiant du parent ne peut pas être vide ou \"null\"");
        Assert.notBlank(factoryMethodName, false, "Le nom de la factoryMethod ne peut pas être vide ou \"null\"");

        managedInstanceDefinitionRegistry.registerManagedInstance(id, clazz, scope, isLazy, parentId, factoryMethodName);
    }

    public void registerManagedInstance(
       @Nullable String id, String clazz, @Nullable String scope
    ) {
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

    protected void defineCustomConfiguration() { }

    protected Object getManagedInstanceForWebScope(ManagedInstance ignoredManagedInstance) {
        throw new UnsupportedOperationException("La méthode \"getManagedInstanceForWebScope\" n'est disponible que dans un contexte web");
    }

    protected void eagerInitSingletonManagedInstances() {
        managedInstanceDefinitionRegistry.getManagedInstances().stream()
            .filter(managedInstance -> !managedInstance.getLazy())
            .forEachOrdered(this::getManagedInstance);
    }

    private Object getManagedInstance(ManagedInstance managedInstance) {
        String managedInstanceId = managedInstance.getId();

        if (isCurrentlyInCreation(managedInstanceId))
            throw new ManagedInstanceCurrentlyInCreationException(managedInstanceId);
        if (isSingleton(managedInstanceId) &&
            singletonsMap.containsKey(managedInstanceId)
        ) return singletonsMap.get(managedInstanceId);

        managedInstanceDefinitionRegistry.resolveDependencies(managedInstance);
        if (managedInstance.getScope() == Scope.REQUEST || managedInstance.getScope() == Scope.SESSION)
            return getManagedInstanceForWebScope(managedInstance);

        Object instance = ManagedInstanceUtils.instantiate(managedInstance, this);
        if (isSingleton(managedInstanceId))
            singletonsMap.put(managedInstanceId, instance);

        return instance;
    }

    private static void validateId(String id) {
        Assert.notNull(id, "L'identifiant ne peut pas être vide ou \"null\"");
    }
}
