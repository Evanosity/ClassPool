package ca.elixa.classpool;

import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;

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

            URL[] urls = { new URL("jar:file:" + cp + "!/") };
            URLClassLoader cl = URLClassLoader.newInstance(urls);

            while (e.hasMoreElements()) {
                JarEntry je = e.nextElement();
                if (je.isDirectory() || !je.getName().endsWith(".class")) {
                    continue;
                }

                // Convert the file path to a class name (e.g., "pkg/MyClass.class" to "pkg.MyClass")
                String className = je.getName().substring(0, je.getName().length() - ".class".length());
                className = className.replace('/', '.');

                // Load the class
                Class c = cl.loadClass(className);
                System.out.println("Loaded class: " + c.getName());
            }
            jarFile.close(); // Important to close the JarFile
        }
        catch(Exception e){

        }

    }

    /**
     * This method is recursive. It will search all sub-packages.
     * @param path - the dot delimited path of the package we are searching.
     * @param baseType - the base class we are searching for
     * @param files - the files we are searching
     */
    private void indexClasses(String path, Class<T> baseType, File... files) {
        try{
            for(File file : files){
                String fileName = file.getName();

                //if the given file is a directory, index all the classes inside of it.
                if(file.isDirectory())
                    indexClasses(path + "." + fileName, baseType, file.listFiles());

                //otherwise, if its a class file, load er up.
                else if(fileName.endsWith(".class")){
                    String fullClassName = path + "." + fileName.substring(0, fileName.length() - 6);

                    Class<?> loadedClass = Class.forName(fullClassName);

                    //if the indexed annotation isn't present, ignore the class
                    if(! loadedClass.isAnnotationPresent(Indexed.class))
                        continue;

                    //if the loaded class isn't a child class of the base type, ignore the class
                    if(! baseType.isAssignableFrom(loadedClass))
                        continue;



                    //index it
                    //I i = indexHandler(instance, loadedClass, path);
                    //index.put(i, instance);
                }
            }
        }
        catch(Exception e){
            throw new RuntimeException("Fatal error while indexing " + path, e);
        }
    }

    private T instantiate(Class<T> clazz){

        try{
            //Get the constructor for the class
            Constructor<? extends T> cons = (Constructor<? extends T>) clazz.getDeclaredConstructor();
            //make sure its accessible
            cons.setAccessible(true);

            return cons.newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
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
