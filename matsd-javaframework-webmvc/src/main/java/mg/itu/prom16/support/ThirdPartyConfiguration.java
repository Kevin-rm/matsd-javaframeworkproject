package mg.itu.prom16.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import mg.matsd.javaframework.security.base.Security;
import mg.matsd.javaframework.validation.base.ValidatorFactory;

public class ThirdPartyConfiguration {
    public static final String MANAGED_INSTANCE_ID      = "_matsd_third_party_configuration";
    public static final String JACKSON_OBJECT_MAPPER_ID = "_jackson_objectmapper";
    public static final String VALIDATOR_FACTORY_ID     = "default_validator_factory";
    public static final String SECURITY_ID              = "_security";

    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        return objectMapper;
    }

    public ValidatorFactory validatorFactory() {
        return ValidatorFactory.buildDefault();
    }

    public Security security() {
        return new Security();
    }
}
