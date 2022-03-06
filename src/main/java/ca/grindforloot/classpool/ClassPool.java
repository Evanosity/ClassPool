package ca.grindforloot.classpool;

import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A directory of classes
 * Given a package, scan the package and instantiate each of those classes. Map those instances by string
 * This effectively allows us to turn a string into a class. Instances of that class have to be held in memory,
 * but in the case of actions, that is a perfectly acceptable alternative to an enormous switch statement.
 *
 * This, in theory, is a threadsafe resource, so long as the returned objects are handled in a threadsafe manner.
 *
 * In any case, this will do for now.
 * @param <T> the base type that we are searching for (Ex: Action). T has to have no constructor.
 */
public class ClassPool<T> {

    private Map<String, T> index = new HashMap<>();

    //I really have to test this.
    public ClassPool(String path, Class<T> baseType){
        try{
            Reflections reflections = new Reflections(path);
            Set<Class<? extends T>> clazzes = reflections.getSubTypesOf(baseType);

            for(Class<? extends T> clazz : clazzes){

                if(!clazz.isAnnotationPresent(Indexed.class))
                    continue;

                Constructor<? extends T> cons = clazz.getDeclaredConstructor();
                cons.setAccessible(true);

                T instance = cons.newInstance();

                String name = clazz.getName().replace(path + ".", "");

                index.put(name, instance);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Fatal error while indexing " + path);
        }
    }

    /**
     * Return the object
     * @param key
     * @return
     */
    public T get(String key){
        return index.get(key);
    }
}
