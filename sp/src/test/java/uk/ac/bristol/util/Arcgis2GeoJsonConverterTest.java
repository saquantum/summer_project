package uk.ac.bristol.util;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class Arcgis2GeoJsonConverterTest {
    @Value("${mock-data.warnings}")
    private String WARNINGS_FILE_PATH;
    @Value("${mock-data.js}")
    private String JS_CONVERTER_FILE_PATH;

    private InputStream getClasspathStream(String path) throws IOException {
        return new ClassPathResource(path).getInputStream();
    }

    @Test
    public void arcgisToGeoJsonConverterTest() throws IOException {
        String output = Arcgis2GeoJsonConverter.arcgisToGeoJSON(getClasspathStream(WARNINGS_FILE_PATH), getClasspathStream(JS_CONVERTER_FILE_PATH));
        assertEquals("{\n" +
                "  \"type\": \"FeatureCollection\",\n" +
                "  \"features\": [\n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"Polygon\",\n" +
                "        \"coordinates\": [\n" +
                "          [\n" +
                "            [\n" +
                "              1.4337,\n" +
                "              52.6697\n" +
                "            ],\n" +
                "            [\n" +
                "              0.4504,\n" +
                "              52.6531\n" +
                "            ],\n" +
                "            [\n" +
                "              -0.3516,\n" +
                "              52.4594\n" +
                "            ],\n" +
                "            [\n" +
                "              -0.7635,\n" +
                "              52.4493\n" +
                "            ],\n" +
                "            [\n" +
                "              -1.1646,\n" +
                "              52.5229\n" +
                "            ],\n" +
                "            [\n" +
                "              -2.0435,\n" +
                "              52.6964\n" +
                "            ],\n" +
                "            [\n" +
                "              -2.8235,\n" +
                "              52.4727\n" +
                "            ],\n" +
                "            [\n" +
                "              -3.2629,\n" +
                "              52.3488\n" +
                "            ],\n" +
                "            [\n" +
                "              -3.6145,\n" +
                "              52.1335\n" +
                "            ],\n" +
                "            [\n" +
                "              -4.3726,\n" +
                "              51.7338\n" +
                "            ],\n" +
                "            [\n" +
                "              -4.7296,\n" +
                "              51.2034\n" +
                "            ],\n" +
                "            [\n" +
                "              -4.6252,\n" +
                "              50.7086\n" +
                "            ],\n" +
                "            [\n" +
                "              -4.3451,\n" +
                "              50.3104\n" +
                "            ],\n" +
                "            [\n" +
                "              -3.7408,\n" +
                "              50.1065\n" +
                "            ],\n" +
                "            [\n" +
                "              -2.5873,\n" +
                "              50.1276\n" +
                "            ],\n" +
                "            [\n" +
                "              -1.1865,\n" +
                "              50.2683\n" +
                "            ],\n" +
                "            [\n" +
                "              -0.9119,\n" +
                "              50.2964\n" +
                "            ],\n" +
                "            [\n" +
                "              -0.5164,\n" +
                "              50.3595\n" +
                "            ],\n" +
                "            [\n" +
                "              -0.1538,\n" +
                "              50.4365\n" +
                "            ],\n" +
                "            [\n" +
                "              0.3296,\n" +
                "              50.5832\n" +
                "            ],\n" +
                "            [\n" +
                "              1.1426,\n" +
                "              50.903\n" +
                "            ],\n" +
                "            [\n" +
                "              1.3293,\n" +
                "              50.9999\n" +
                "            ],\n" +
                "            [\n" +
                "              1.4832,\n" +
                "              51.1104\n" +
                "            ],\n" +
                "            [\n" +
                "              1.8567,\n" +
                "              51.4814\n" +
                "            ],\n" +
                "            [\n" +
                "              2.0105,\n" +
                "              51.761\n" +
                "            ],\n" +
                "            [\n" +
                "              2.0544,\n" +
                "              51.9036\n" +
                "            ],\n" +
                "            [\n" +
                "              2.0764,\n" +
                "              52.12\n" +
                "            ],\n" +
                "            [\n" +
                "              2.0435,\n" +
                "              52.3957\n" +
                "            ],\n" +
                "            [\n" +
                "              1.7963,\n" +
                "              52.5797\n" +
                "            ],\n" +
                "            [\n" +
                "              1.4337,\n" +
                "              52.6697\n" +
                "            ]\n" +
                "          ]\n" +
                "        ]\n" +
                "      },\n" +
                "      \"properties\": {\n" +
                "        \"OBJECTID\": 1609,\n" +
                "        \"weathertype\": \"THUNDERSTORM\",\n" +
                "        \"warninglevel\": \"YELLOW\",\n" +
                "        \"warningheadline\": \"Heavy showers and thunderstorms may lead to some disruption to transport and infrastructure.\",\n" +
                "        \"validfromdate\": 1749283200000,\n" +
                "        \"validtodate\": 1749315600000,\n" +
                "        \"GlobalID\": \"b7aac979-7579-41f8-95b9-853995a67e39\",\n" +
                "        \"warningImpact\": \"2/Low\",\n" +
                "        \"warningLikelihood\": \"3/Likely\",\n" +
                "        \"warningVersion\": \"2.0\",\n" +
                "        \"affectedAreas\": \"East Midlands (Leicestershire, Northamptonshire);\\n\\nEast of England (Bedford, Cambridgeshire, Central Bedfordshire, Essex, Hertfordshire, Luton, Norfolk, Southend-on-Sea, Suffolk, Thurrock);\\n\\nLondon & South East England (Bracknell Forest, Brighton and Hove, Buckinghamshire, East Sussex, Greater London, Hampshire, Isle of Wight, Kent, Medway, Milton Keynes, Oxfordshire, Portsmouth, Reading, Slough, Southampton, Surrey, West Berkshire, West Sussex, Windsor and Maidenhead, Wokingham);\\n\\nSouth West England (Bath and North East Somerset, Bournemouth Christchurch and Poole, Bristol, Cornwall, Devon, Dorset, Gloucestershire, North Somerset, Plymouth, Somerset, South Gloucestershire, Swindon, Torbay, Wiltshire);\\n\\nWales (Blaenau Gwent, Bridgend, Caerphilly, Cardiff, Carmarthenshire, Merthyr Tydfil, Monmouthshire, Neath Port Talbot, Newport, Powys, Rhondda Cynon Taf, Swansea, Torfaen, Vale of Glamorgan);\\n\\nWest Midlands (Herefordshire, Shropshire, Staffordshire, Warwickshire, West Midlands Conurbation, Worcestershire)\",\n" +
                "        \"whatToExpect\": \"Probably some damage to a few buildings and structures from lightning strikes.\\n\\nThere is a good chance driving conditions will be affected by spray, standing water and/or hail, leading to longer journey times by car and bus.\\n\\nDelays to train services are possible.\\n\\nSome short term loss of power and other services is likely\",\n" +
                "        \"warningFurtherDetails\": \"Frequent heavy showers and thunderstorms are expected for much of Saturday before fading from the west during the mid to late afternoon. \\n\\n10-15 mm of rain could fall in less than an hour, whilst some places could see 30-40 mm of rain over several hours from successive showers and thunderstorms. Frequent lightning, hail and strong, gusty winds will be additional hazards.\\n\\nWhat Should I Do?\\n\\nConsider if your location is at risk of flash flooding. If so, consider preparing a flood plan and an emergency flood kit. \\n\\nGive yourself the best chance of avoiding delays by checking road conditions if driving, or bus and train timetables, amending your travel plans if necessary.\\n\\nPeople cope better with power cuts when they have prepared for them in advance. It’s easy to do; consider gathering torches and batteries, a mobile phone power pack and other essential items.\\n\\nIf you find yourself outside and hear thunder, protect yourself by finding a safe enclosed shelter (such as a car). Do not shelter under or near trees, or other structures which may be struck by lightning. If you are on an elevated area move to lower ground.\\n\\nBe prepared for weather warnings to change quickly: when a weather warning is issued, the Met Office recommends staying up to date with the weather forecast in your area.\",\n" +
                "        \"warningUpdateDescription\": \"Warning re-issued due to a technical error.\",\n" +
                "        \"issuedDate\": 1749200616000,\n" +
                "        \"issuedDateString\": \"06-06-2025 10:03:36 AM BST\",\n" +
                "        \"validFromDateString\": \"07-06-2025 09:00:00 AM BST\",\n" +
                "        \"validToDateString\": \"07-06-2025 06:00:00 PM BST\",\n" +
                "        \"modifiedDateString\": \"06-06-2025 10:08:29 AM BST\"\n" +
                "      },\n" +
                "      \"id\": 1609\n" +
                "    }\n" +
                "  ]\n" +
                "}", output);
    }
}
