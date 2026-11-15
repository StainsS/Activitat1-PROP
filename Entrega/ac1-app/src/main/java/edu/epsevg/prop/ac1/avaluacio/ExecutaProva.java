package edu.epsevg.prop.ac1.avaluacio;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Dispatcher perquè {@code corrector/corrector.py} pugui invocar, en un procés Java nou
 * i aïllat, un mètode estàtic concret de {@link Proves} — o descobrir-los tots amb
 * {@code --list} sense haver de mantenir la llista sincronitzada a mà.
 * <p>
 * Ús: {@code java ... ExecutaProva <nomDelMetode>} executa {@code Proves.<nomDelMetode>()};
 * {@code java ... ExecutaProva --list} imprimeix, un per línia, el nom de tots els
 * mètodes {@code public static} sense arguments de {@link Proves}.
 */
public final class ExecutaProva {
    private ExecutaProva() {}

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("--list")) {
            llistarProves();
            return;
        }
        if (args.length != 1) {
            System.err.println("Ús: ExecutaProva <nomDelMetode> | --list");
            System.exit(2);
            return;
        }
        try {
            Method m = Proves.class.getMethod(args[0]);
            m.invoke(null);
        } catch (InvocationTargetException e) {
            System.err.println("ERROR executant '" + args[0] + "': " + e.getCause());
            e.getCause().printStackTrace();
            System.exit(1);
        } catch (ReflectiveOperationException e) {
            System.err.println("ERROR: no existeix el mètode Proves." + args[0] + "(): " + e);
            System.exit(1);
        }
    }

    private static void llistarProves() {
        Arrays.stream(Proves.class.getMethods())
                .filter(m -> Modifier.isStatic(m.getModifiers())
                        && m.getParameterCount() == 0
                        && m.getDeclaringClass() == Proves.class)
                .map(Method::getName)
                .sorted(Comparator.naturalOrder())
                .forEach(System.out::println);
    }
}
