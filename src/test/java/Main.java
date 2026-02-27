import net.minecraft.core.registries.BuiltInRegistries;

public class Main {

    public static void main(String[] args) {
        BuiltInRegistries.ATTRIBUTE.keySet().forEach(System.out::println);
    }
}
