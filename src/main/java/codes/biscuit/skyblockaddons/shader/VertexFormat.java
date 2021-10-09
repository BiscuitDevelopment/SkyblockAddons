package codes.biscuit.skyblockaddons.shader;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
public enum VertexFormat {

    POSITION(VertexFormatElement.POSITION),
    POSITION_COLOR(VertexFormatElement.POSITION, VertexFormatElement.COLOR),
    ;

    private List<VertexFormatElement> vertexFormatElements;

    VertexFormat(VertexFormatElement... vertexFormatElements) {
        this.vertexFormatElements = Collections.unmodifiableList(Arrays.asList(vertexFormatElements));
    }
}
