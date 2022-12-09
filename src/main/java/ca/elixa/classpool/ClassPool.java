package ca.elixa.classpool;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.*;

/**
 * A directory of classes
 * Given a package, scan the package and instantiate each of those classes. Map those instances by string
 * This effectively allows us to turn a string into a class. Instances of that class are held in memory,
 * but in the case of actions, that is a perfectly acceptable alternative to an enormous switch statement.
 *
 * This, in theory, is a threadsafe resource, so long as the returned objects are handled in a threadsafe manner.
 *
 * In any case, this will do for now.
 * @param <T> the base type that we are searching for (Ex: Action). T has to have a 0 argument constructor, which can (should)
 *           be private.
 */
public class ClassPool<T> {

    private Map<String, T> index = new HashMap<>();

    protected final Class<T> baseType;

    /**
     * Initialize the ClassPool
     * @param path
     * @param type
     */
    public ClassPool(String path, Class<T> type){

        this.baseType = type;

        if(path != null){
            //using the classloader, locate the package we are searching for.
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL url = loader.getResource(path.replace(".", "/"));

            File[] rawClasses = new File(url.getFile()).listFiles();

            //index the classes.
            indexClasses(path, baseType, rawClasses);
        }
    }

    /**
     * This method is recursive. It will search all sub-packages.
     * @param path - the dot delimited path of the package we are searching.
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


                    //if the class exists, has a valid superclass AND has the indexed annotation, index it
                    if(loadedClass.isAnnotationPresent(Indexed.class) && baseType.isAssignableFrom(loadedClass)) {

                        Constructor<? extends T> cons = (Constructor<? extends T>) loadedClass.getDeclaredConstructor();
                        //this means we can disallow the objects from being instantiated normally, via private constructors
                        cons.setAccessible(true);

                        T instance = cons.newInstance();

                        String className = loadedClass.getName().replace(path + ".", "");

                        index.put(className, instance);
                        System.out.println("Indexing " + className + " for group " + baseType.getName());
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Fatal error while indexing " + path);
        }
    }

    /**
     *
     * @return an immutable set of strings representing the types stored in this pool
     */
    public Set<String> getStoredTypes(){
        return Collections.unmodifiableSet(index.keySet());
    }

    /**
     * Return a stored object. Will throw an error instead of returning null.
     * @param key
     * @return
     */
    public T get(String key){

        T result = index.get(key);

        if(result == null)
            throw new IllegalArgumentException("No class found for name " + key + " for base type " + baseType.getName());

        return result;
    }
}
