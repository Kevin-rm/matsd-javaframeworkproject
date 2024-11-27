package mg.itu.prom16.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import mg.matsd.javaframework.validation.base.ValidatorFactory;

public class ThirdPartyConfiguration {
    public static final String MANAGED_INSTANCE_ID      = "_matsd_app_extension_configuration";
    public static final String JACKSON_OBJECT_MAPPER_ID = "_jackson_objectmapper";

    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        return objectMapper;
    }

    public ValidatorFactory validatorFactory() {
        return ValidatorFactory.buildDefault();
    }
}
