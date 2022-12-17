package ca.elixa.classpool;

/**
 * The default implementation of Classpool, which indexes the given types by their class name
 * @param <T>
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

    @Override
    public String indexMethod(T instance, Class<?> loadedClass, String path) {
        return loadedClass.getName().replace(path + ".", "");
    }
}
