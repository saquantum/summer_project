package uk.ac.bristol.util;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Arcgis2GeoJsonConverter {

    private Arcgis2GeoJsonConverter() {
    }

    public static String arcgisToGeoJSON(String converterJSPath, String arcgisJSONPath) throws IOException {
        String jsCode = Files.readString(Paths.get(converterJSPath));
        String arcgisJson = Files.readString(Paths.get(arcgisJSONPath));

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
