package codes.biscuit.skyblockaddons.shader;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UniformType<T> {

    public static final UniformType<Float> FLOAT = new UniformType<>(1);
    public static final UniformType<Float[]> VEC3 = new UniformType<>(3);

    private int amount;
}
