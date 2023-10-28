package cl.clillo.lighting.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonUtils {

    private static final ObjectMapper jsonMapper;

    static {
        jsonMapper = new ObjectMapper();
        jsonMapper.findAndRegisterModules();
    }

    private JsonUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");

    }

    public static <T> T convertTo(JsonNode node, Class<T> clazz) {
        try {
            return jsonMapper.treeToValue(node, clazz);
        } catch (Exception exception) {
            String message = MessageFormat.format("Error mapping node to class. Clazz: {0}. Node: {1}. Error: {2}", clazz.getName(), node, exception.getMessage());
            throw new RuntimeJsonMappingException(message);
        }
    }

    public static <T> T convertTo(JsonNode node, TypeReference<T> type) {
        try {
            return jsonMapper.readValue(jsonMapper.treeAsTokens(node), type);
        } catch (Exception exception) {
            throw new RuntimeJsonMappingException(exception.getMessage());
        }
    }

    public static <T> Map<String, T> mapValue(final JsonNode clientResponse, final String field){
        if (!clientResponse.has(field))
            return new HashMap<>();
        return jsonMapper.convertValue(clientResponse.get(field), new TypeReference<>() {});
    }

    public static <T> T convertTo(String json, Class<T> clazz) {
        try {
            return jsonMapper.readValue(json, clazz);
        } catch (Exception exception) {
            throw new RuntimeJsonMappingException(exception.getMessage());
        }
    }

    public static <T> String convertTo(T object) {
        try {
            return jsonMapper.writeValueAsString(object);
        } catch (Exception exception) {
            throw new RuntimeJsonMappingException(exception.getMessage());
        }
    }

    public static <T> T convertTo(JsonNode clientResponse, final String field, Class<T> clazz) {
        if (!clientResponse.has(field))
            return null;

        try {
            return jsonMapper.treeToValue(clientResponse.get(field), clazz);
        } catch (Exception exception) {
            String message = MessageFormat.format("Error mapping node to class. Clazz: {0}. Node: {1}. Error: {2}", clazz.getName(), clientResponse, exception.getMessage());
            throw new RuntimeJsonMappingException(message);
        }
    }

    public static <T> List<T> listToObject(final JsonNode clientResponse, final String field, Class<T> clazz){
        if (!clientResponse.has(field))
            return List.of();

        final List<T> response = new ArrayList<>();
        final Iterator<JsonNode> list = clientResponse.get(field).elements();

        while (list.hasNext())
            response.add(convertTo(list.next(), clazz));

        return response;
    }

    public static String stringValue(final JsonNode clientResponse, final String field) {
        return clientResponse.has(field) ? clientResponse.get(field).asText() : null;
    }

    public static long longValue(final JsonNode clientResponse, final String field) {
        return clientResponse.has(field) ? clientResponse.get(field).asLong() : 0;
    }

    public static BigDecimal bigDecimalValue(final JsonNode clientResponse, final String field) {
        return clientResponse.has(field) ? clientResponse.get(field).decimalValue() : BigDecimal.ZERO;
    }

    public static List<String> stringListValue(final JsonNode clientResponse, final String field) {
        if (!clientResponse.has(field))
            return new ArrayList<>();

        List<String> controlsList = new ArrayList<>();
        for (JsonNode controlNode : clientResponse.get(field)) {
            controlsList.add(controlNode.asText());
        }

        return controlsList;
    }

    public static List<Map<String, Object>> listValue(final JsonNode clientResponse, final String field){
        if (!clientResponse.has(field))
            return null;

        return jsonMapper.convertValue(clientResponse.get(field), new TypeReference<>() {});
    }

    public static List<String> mapToList(final Map<String, Object> map){
        if (map==null || map.isEmpty())
            return List.of();

        final List<String> result = new ArrayList<>();
        final Deque<Map<String, Object>> toProcess = new ArrayDeque<>();
        toProcess.add(map);

        while (!toProcess.isEmpty()){
            final Map<String, Object> node = toProcess.pop();
            if (node.isEmpty())
                continue;

            for(Map.Entry<String, Object> n: node.entrySet()){
                final String currentKey = n.getKey();
                if (n.getValue() instanceof Map) {
                    Map<String,Object> value = (Map<String, Object>) n.getValue();
                    Map<String,Object> tmp = new HashMap<>();
                    for(Map.Entry<String, Object> nValue: value.entrySet())
                        tmp.put(currentKey+"."+nValue.getKey(), nValue.getValue());

                    toProcess.push(tmp);
                }
                else
                    result.add(currentKey+"="+n.getValue());
            }
        }

        return result;
    }

    public static boolean booleanValue(final JsonNode clientResponse, final String field){
        return clientResponse.has(field) && clientResponse.get(field).asBoolean();
    }
}
