package mg.itu.prom16.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@JsonResponse
@Controller
public @interface RestController { }
