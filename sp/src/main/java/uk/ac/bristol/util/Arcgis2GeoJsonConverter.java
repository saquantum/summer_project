package uk.ac.bristol.util;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Arcgis2GeoJsonConverter {

    private Arcgis2GeoJsonConverter() {
    }

    public static String arcgisToGeoJSON(InputStream arcgisJSON, InputStream JSConverter) throws IOException {
        String arcgisJson = new String(arcgisJSON.readAllBytes(), StandardCharsets.UTF_8);
        String jsCode = new String(JSConverter.readAllBytes(), StandardCharsets.UTF_8);

        // context needs to be closed, put it into try()
        try (Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .build()) {

            context.eval("js", jsCode);

            Value bindings = context.getBindings("js");
            Value converter = bindings.getMember("arcgisToGeoJSON");
            Value parse = bindings.getMember("JSON").getMember("parse");
            Value stringify = bindings.getMember("JSON").getMember("stringify");

            return stringify.execute(
                    converter.execute(
                            parse.execute(arcgisJson)
                    )
                    , null, 2).asString();
        }
    }
}
