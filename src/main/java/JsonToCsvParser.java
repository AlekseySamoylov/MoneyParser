import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonToCsvParser {

  public static void main(String[] args) {
    // Input JSON file path
    String jsonFilePath = "payload1.json";
    // Output CSV file path
    String csvFilePath = "output.csv";

    try {
      // Read JSON file
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));

      // Get the payload array
      JsonNode payloadNode = rootNode.path("payload");

      // Create CSV file and write header
      FileWriter csvWriter = new FileWriter(csvFilePath);
      csvWriter.append("operationDate,value,category,brand,type\n");

      // Process each transaction in the payload
      for (JsonNode transaction : payloadNode) {
        // Extract values from JSON, handling potential nulls
        String type = getValueAsString(transaction, "type");
        String brandName = getNestedValueAsString(transaction, "brand", "name");
        String amountValue = getNestedValueAsString(transaction, "amount", "value");
        String spendingCategoryName = getNestedValueAsString(transaction, "spendingCategory", "name");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        long debitingTimeLong = getNestedValueAsLong(transaction, "operationTime", "milliseconds");
        String debitingTime = dateFormat.format(new Date(debitingTimeLong));

        // Write to CSV
        csvWriter.append(String.join(",",
            escapeCsv(debitingTime),
            escapeCsv(amountValue),
            escapeCsv(spendingCategoryName),
            escapeCsv(brandName),
            escapeCsv(type)
            ));
        csvWriter.append("\n");
      }

      csvWriter.close();
      System.out.println("CSV file created successfully at: " + csvFilePath);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String getValueAsString(JsonNode node, String fieldName) {
    return node.has(fieldName) ? node.get(fieldName).asText() : "";
  }

  private static String getNestedValueAsString(JsonNode node, String parentField, String childField) {
    if (node.has(parentField)) {
      JsonNode parentNode = node.get(parentField);
      return parentNode.has(childField) ? parentNode.get(childField).asText() : "";
    }
    return "";
  }

  private static long getNestedValueAsLong(JsonNode node, String parentField, String childField) {
    if (node.has(parentField)) {
      JsonNode parentNode = node.get(parentField);
      return parentNode.has(childField) ? parentNode.get(childField).asLong() : 0L;
    }
    return 0L;
  }

  private static String escapeCsv(String value) {
    if (value == null || value.isEmpty()) {
      return "";
    }
    // Escape quotes and wrap in quotes if contains comma
    String escaped = value.replace("\"", "\"\"");
    if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
      return "\"" + escaped + "\"";
    }
    return escaped;
  }
}
