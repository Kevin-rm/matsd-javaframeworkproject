package mg.itu.prom16.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import mg.matsd.javaframework.di.annotations.IfClassPresent;
import mg.matsd.javaframework.validation.base.ValidatorFactory;

public class ThirdPartyConfiguration {
    public static final String MANAGED_INSTANCE_ID      = "_matsd_third_party_configuration";
    public static final String JACKSON_OBJECT_MAPPER_ID = "_jackson_objectmapper";
    public static final String VALIDATOR_FACTORY_ID     = "default_validator_factory";

    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        return objectMapper;
    }

    @IfClassPresent("mg.matsd.javaframework.validation.base.ValidatorFactory")
    public ValidatorFactory validatorFactory() {
        return ValidatorFactory.buildDefault();
    }
}
