package cucumber.cukeulator.test;

public final class TypeRegistryConfiguration  {

    private TypeRegistryConfiguration() {
    }

    @io.cucumber.java.ParameterType("[0-9]")
    public static int digit(String param) {
        return Integer.parseInt(param);
    }

    @io.cucumber.java.ParameterType("[+–x\\/=]")
    public static char operator(String param) {
        return param.charAt(0);
    }
}
