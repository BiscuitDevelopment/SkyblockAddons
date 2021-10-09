package codes.biscuit.skyblockaddons.utils.gson;

/**
 * Use this interface in order to mark a class that needs to be
 * initialized. The method this interface provides will be
 * called after:
 * <ol>
 *     <li>The object's constructor has been called</li>
 *     <li>Gson has filled in all the fields</li>
 * </ol>
 *
 * Use the method provided as a hook to do any processing after
 * Gson finishes deserialization.
 */
public interface GsonInitializable {

    void gsonInit();
}
