package codes.biscuit.skyblockaddons.shader;

import java.util.Objects;
import java.util.function.Supplier;

public class Uniform<T> {

    private final UniformType<T> uniformType;
    private final Supplier<T> uniformValuesSupplier;
    private final String name;

    private int uniformID;
    private T previousUniformValue;

    public Uniform(Shader shader, UniformType<T> uniformType, String name, Supplier<T> uniformValuesSupplier) {
        this.uniformType = uniformType;
        this.uniformValuesSupplier = uniformValuesSupplier;
        this.name = name;

        init(shader, name);
    }

    private void init(Shader shader, String name) {
        uniformID = ShaderHelper.getInstance().glGetUniformLocation(shader.getProgram(), name);
    }

    public void update() {
        T newUniformValue = uniformValuesSupplier.get();
        if (!Objects.deepEquals(previousUniformValue, newUniformValue)) {
            if (uniformType == UniformType.FLOAT) {
                ShaderHelper.getInstance().glUniform1f(uniformID, (Float) newUniformValue);

            } else if (uniformType == UniformType.VEC3) {
                Float[] values = (Float[]) newUniformValue;
                ShaderHelper.getInstance().glUniform3f(uniformID, values[0], values[1], values[2]);
            }

            previousUniformValue = newUniformValue;
        }
    }
}
