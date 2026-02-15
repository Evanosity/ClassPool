package ca.elixa.classpool;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Given a package, scan the package and instantiate each of those classes. The inheritors will handle how to index
 * those classes.
 *
 * <br><br>
 *
 * This effectively allows us to turn an identifier object of type I into an object of type T.
 *
 * <br><br>
 *
 * The pool itself is threadsafe, and the objects returned should be handled in a threadsafe manner.
 *
 * @param <T> the base type that we are searching for (Ex: Action). T has to have a 0 argument constructor, which can (should)
 *           be private.
 *
 * @param <I> the identifier that T is stored by.
 */
public abstract class ClassPool<T, I> {

    private Map<I, T> index = new HashMap<>();

    protected final Class<T> baseType;

    /**
     * Initialize the ClassPool
     * @param path
     * @param type
     */
    public ClassPool(String path, Class<T> type){

        this.baseType = type;

        String cp = System.getProperty("java.class.path");

        try{

            JarFile jarFile = new JarFile(cp);
            Enumeration<JarEntry> e = jarFile.entries();

            while (e.hasMoreElements()) {

                JarEntry je = e.nextElement();

                String jeName = je.getName();

                if (je.isDirectory() || !jeName.endsWith(".class")) {
                    continue;
                }

                // Convert the file path to a class name (e.g., "pkg/MyClass.class" to "pkg.MyClass")
                String className = jeName.substring(0, jeName.length() - 6);
                className = className.replace('/', '.');

                if(! className.startsWith(path))
                    continue;

                // Load the class
                Class<?> c = Class.forName(className);

                if(! c.isAssignableFrom(type) || ! c.isAnnotationPresent(Indexed.class))
                    continue;

                Constructor<? extends T> cons = (Constructor<? extends T>) c.getDeclaredConstructor();
                //make sure its accessible
                cons.setAccessible(true);

                var instance = cons.newInstance();

                var i = indexHandler(instance, c, path);

                index.put(i, instance);
            }

            jarFile.close(); // Important to close the JarFile (thanks gemini)

        }
        catch(Exception e){
            throw new RuntimeException("Fatal error while indexing " + path, e);
        }

    }

    /**
     * Handles how to index the instanced classes.
     * @param instance the instance of type T
     * @param loadedClass the runtime class of instance
     * @param path the path for loadedClass
     * @return the index for instance
     */
    public abstract I indexHandler(T instance, Class<?> loadedClass, String path);

    /**
     *
     * @return an immutable set of type I representing the types stored in this pool
     */
    public Set<I> getStoredTypes(){
        return Collections.unmodifiableSet(index.keySet());
    }

    /**
     * Return a stored object. Will throw an error instead of returning null.
     * @param key
     * @return see {@link java.util.HashMap#get(Object)}
     */
    public T get(I key){

        T result = index.get(key);

        if(result == null)
            throw new IllegalArgumentException("No class found for identifier " + key + " for base type " + baseType.getName());

        return result;
    }
}
