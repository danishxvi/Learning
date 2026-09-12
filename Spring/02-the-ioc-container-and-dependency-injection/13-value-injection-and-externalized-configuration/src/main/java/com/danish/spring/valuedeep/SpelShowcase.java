package com.danish.spring.valuedeep;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

// Every earlier @Value in this stack used the simple "${key}" form. @Value ALSO accepts
// full Spring Expression Language (SpEL) inside "#{...}" - a small expression language
// that can do arithmetic, call static methods, and read another bean's properties.
@Component
public class SpelShowcase {

    // Plain property placeholder - what every earlier lesson used.
    @Value("${app.allowed-currencies}")
    private String allowedCurrenciesRaw;

    // SpEL: split a comma-separated property STRING into a List at injection time. The
    // inner "${app.allowed-currencies}" is resolved to text FIRST, then the outer
    // #{...} SpEL expression's .split(",") runs on that resolved text.
    @Value("#{'${app.allowed-currencies}'.split(',')}")
    private List<String> allowedCurrencies;

    // SpEL arithmetic - evaluated once, at injection time, not on every access.
    @Value("#{60 * 60 * 24}")
    private long secondsPerDay;

    // SpEL calling a static method via the T() (Type) operator.
    @Value("#{T(java.lang.Math).PI}")
    private double pi;

    // SpEL reading an OS environment variable directly (systemEnvironment), separate
    // from application.yml entirely - useful for things that must never live in a
    // committed config file, like reading a path Windows/Linux both set by default.
    @Value("#{systemProperties['os.name']}")
    private String operatingSystem;

    // SpEL referencing ANOTHER BEAN'S PROPERTY by bean name - "appInfo" is AppInfo's
    // default bean name, ".version" calls its getVersion() getter. This is how one
    // bean's configuration can be derived from another's, entirely declaratively.
    @Value("#{appInfo.version}")
    private String versionCopiedFromAppInfo;

    public void printAll() {
        System.out.println("  allowedCurrenciesRaw (plain ${..})   = " + allowedCurrenciesRaw);
        System.out.println("  allowedCurrencies (SpEL .split)      = " + allowedCurrencies);
        System.out.println("  secondsPerDay (SpEL arithmetic)      = " + secondsPerDay);
        System.out.println("  pi (SpEL static field via T())       = " + pi);
        System.out.println("  operatingSystem (SpEL systemProps)   = " + operatingSystem);
        System.out.println("  versionCopiedFromAppInfo (bean ref)  = " + versionCopiedFromAppInfo);
    }
}
