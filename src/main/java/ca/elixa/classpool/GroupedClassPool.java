package ca.elixa.classpool;


import java.util.*;

/**
 * This is a classpool that acts as a pool of classpools.
 * @param <T>
 */
public class GroupedClassPool<T> extends ClassPoolString<T> {

    List<ClassPoolString<T>> poolOfPools;

    /**
     * Initialize the ClassPool
     *
=     * @param type
     */
    public GroupedClassPool(Class<T> type, ClassPoolString<T>...pools) {
        super(null, type);

        poolOfPools = new ArrayList<>();

        if(pools != null && pools.length != 0)
            for(ClassPoolString<T> p : pools)
                addPool(p);
    }

    /**
     * Add a classpool to the grouped pool.
     * @param p
     * @throws IllegalArgumentException if a duplicate type is present
     */
    public void addPool(ClassPoolString<T> p){
        Set<String> currentlyStoredTypes = getStoredTypes();

        for(String s : p.getStoredTypes())
            if(currentlyStoredTypes.contains(s))
                throw new IllegalArgumentException("Attempted to add two entities with the same identifier(" + s + "to a grouped class pool");

        poolOfPools.add(p);
    }

    /**
     * Return a combined set of all the grouped pools.
     * @return
     */
    @Override
    public Set<String> getStoredTypes(){
        Set<String> result = new HashSet<>();

        for(ClassPoolString<T> p : poolOfPools)
            result.addAll(p.getStoredTypes());

        return result;
    }

    /**
     * Get a
     * @param key
     * @return the resulting entity
     */
    @Override
    public T get(String key){
        for(ClassPoolString<T> p : poolOfPools){

            T result = null;

            try{
                result = p.get(key);
            }
            catch(IllegalArgumentException e){
                continue;
            }

            return result;
        }

        throw new IllegalArgumentException("No class found for name " + key + " for base type " + baseType.getName());
    }
}
