package ca.elixa.classpool;

/**
 * The default implementation of {@link ClassPool} , which indexes the given types by their class name
 * @param <T> the type we are storing
 */
public class ClassPoolString<T> extends ClassPool<T, String> {

    /**
     * Initialize the ClassPool
     *
     * @param path
     * @param type
     */
    public ClassPoolString(String path, Class<T> type) {
        super(path, type);
    }

    /**
     * Index based on the qualified name of the class.
     * @param instance the instance of type T
     * @param loadedClass the runtime class of instance
     * @param path the path for loadedClass
     * @return
     */
    @Override
    public String indexHandler(T instance, Class<?> loadedClass, String path) {
        return loadedClass.getName().replace(path + ".", "");
    }
}
